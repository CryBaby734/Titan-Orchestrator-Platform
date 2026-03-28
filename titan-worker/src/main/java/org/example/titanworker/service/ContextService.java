package org.example.titanworker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.titanworker.domain.TaskInstance;
import org.example.titanworker.domain.TaskStatus;
import org.example.titanworker.repository.TaskInstanceRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContextService {

   private final TaskInstanceRepository taskInstanceRepository;
   private final ObjectMapper objectMapper;

   private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{(.*?)\\}");
   public Map<String, Object> resolvePayload(Map<String, Object> originalPayload, UUID workflowInstanceId) {
      Map<String, JsonNode> context = loadWorkflowContext(workflowInstanceId);
      // Запускаем рекурсивный поиск по всему JSON
      return (Map<String, Object>) resolveRecursive(originalPayload, context);
   }

   private Object resolveRecursive(Object object, Map<String,JsonNode> context) {
      if(object instanceof String) {
         return resolveString((String) object, context);
      }else if(object instanceof Map) {
         Map<String, Object> newMap = new HashMap<>();
         Map<?,?> orginalMap = (Map<?, ?>) object;
         for (Map.Entry<?, ?> entry : orginalMap.entrySet()) {
            newMap.put((String) entry.getKey(), resolveRecursive(entry.getValue(), context));
         }
         return newMap;
      }else if (object instanceof List) {
         List<Object> newList = new ArrayList<>();
         for(Object item : (List<?>) object) {
            newList.add(resolveRecursive(item, context));
         }
         return newList;
      }
      return object;
   }

   private String resolveString(String text, Map<String, JsonNode> context) {
      Matcher matcher = PLACEHOLDER_PATTERN.matcher(text);
      StringBuilder sb = new StringBuilder();

      while (matcher.find()) {
         String key = matcher.group(1);
         String replacement = extractValueFromContext(key, context);
         matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
      }
      matcher.appendTail(sb);
      return sb.toString();
   }

   private String extractValueFromContext(String key, Map<String, JsonNode> context) {
      try{
         String[] parts = key.split("\\.");
         if( parts.length < 3 || !"output".equals(parts[1])) {
            log.warn("Invalid placeholder format: {}. Expected: TaskName.output.fieldName", key);
            return key;
         }

         String taskName = parts[0];

         JsonNode taskOutput =context.get(taskName);

         if(taskOutput == null) {
            log.warn("Task '{}' output not found in context (maybe it failed or has not run?)", taskName);
            return "null";
         }

         JsonNode current = taskOutput;
         for(int i = 2; i < parts.length; i++) {
            current = current.path(parts[i]);
         }

         if(current.isMissingNode()) {
            return "null";
         }
         return current.asText();
      }catch(Exception e) {
         log.error("Error resolving placeholder {}", key, e);
         return "error";
      }
   }

   private Map<String, JsonNode> loadWorkflowContext(UUID workflowInstanceId) {
      List<TaskInstance> tasks = taskInstanceRepository.findByWorkflowInstanceId(workflowInstanceId);

      Map<String, JsonNode> context = new HashMap<>();

      for(TaskInstance task : tasks) {
         if(task.getStatus() == TaskStatus.SUCCESS && task.getOutput() != null) {
            try{
               JsonNode outputNode = objectMapper.readTree(task.getOutput());

               String contextKey = task.getTaskDefinition().getAlias() != null
                       ? task.getTaskDefinition().getAlias()
                       : task.getTaskDefinition().getName();
               context.put(contextKey, outputNode);
            }catch(Exception e) {
               log.error("Error resolving task output {}", task.getTaskDefinition().getName(), e);
            }
         }
      }
      return context;
   }
}
