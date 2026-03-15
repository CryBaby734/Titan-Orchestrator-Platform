package org.example.titanworker.executor;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.titanworker.domain.TaskDefinition;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class LlmTaskExecutor implements TaskExecutor {

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Override
    public boolean canExecute(String type) {
        // Теперь интерфейс совпадает, проверяем просто по строке!
        return "LLM_PROMPT".equals(type);
    }

    @Override
    public String execute(TaskDefinition taskDefinition) {
        try {
            Map<String, Object> payload = taskDefinition.getPayload();

            String prompt = (String) payload.get("prompt");
            String model = payload.containsKey("model") ? (String) payload.get("model") : "llama3";
            String host = payload.containsKey("host") ? (String) payload.get("host") : "http://host.docker.internal:11434";

            log.info("Sending prompt to LLM [{}]: {}", model, prompt);

            String requestBody = String.format(
                    "{\"model\": \"%s\", \"prompt\": \"%s\", \"stream\": false}",
                    model,
                    prompt.replace("\"", "\\\"").replace("\n", " ")
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(host + "/api/generate"))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofMinutes(5))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                // Если статус не 200, выбрасываем RuntimeException (оно не требует throws Exception в методе)
                throw new RuntimeException("LLM API failed! Status: " + response.statusCode());
            }

            return response.body();

        } catch (Exception e) {
            // Оборачиваем любую сетевую ошибку в RuntimeException
            throw new RuntimeException("Failed to execute LLM task: " + e.getMessage(), e);
        }
    }
}