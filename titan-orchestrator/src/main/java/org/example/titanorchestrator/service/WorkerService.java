package org.example.titanorchestrator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.titanorchestrator.domain.TaskDefinition;
import org.example.titanorchestrator.domain.TaskInstance;
import org.example.titanorchestrator.domain.TaskStatus;
import org.example.titanorchestrator.dto.TaskMessage;
import org.example.titanorchestrator.repository.TaskInstanceRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkerService {

    private final TaskInstanceRepository taskInstanceRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final org.springframework.context.ApplicationContext applicationContext;

    private static final String QUEUE_NAME = "titan_tasks_queue";

    @Scheduled(fixedDelay = 1000)
    public void pollQueue() {

        TaskMessage message = (TaskMessage) redisTemplate.opsForList().rightPop(QUEUE_NAME);

        if (message != null) {
            applicationContext.getBean(WorkerService.class).processTask(message);
        }
    }

    @org.springframework.transaction.annotation.Transactional
    public void processTask(TaskMessage message) {

        log.info("Woker picked up task: {}", message.taskInstanceId());

        Optional<TaskInstance> taskOpt = taskInstanceRepository.findById(message.taskInstanceId());
        if (taskOpt.isEmpty()) {
            log.info("Task instance {} not found in DB", message.taskInstanceId());
            return;
        }

        TaskInstance task = taskOpt.get();

        try {
            log.info("Executing task [{}] type [{}]....", task.getTaskDefinition().getName(), message.taskType());

            Thread.sleep(3000);

            log.info("Task completed successfully!");

            task.setStatus(TaskStatus.SUCCESS);
            task.setFinishedAt(LocalDateTime.now());
            taskInstanceRepository.save(task);

            handleDependencies(task);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            task.setStatus(TaskStatus.FAILED);
            taskInstanceRepository.save(task);
        } catch (Exception e) {
            log.error("Error executing task", e);
            task.setStatus(TaskStatus.FAILED);
            taskInstanceRepository.save(task);
        }
    }


    private void handleDependencies(TaskInstance parentTask) {

        Set<TaskDefinition> childrenDefinitions = parentTask.getTaskDefinition().getNextTasks();

        if(childrenDefinitions.isEmpty()) {
            return;
        }

        List<TaskInstance> childInstances = taskInstanceRepository.findAllByWorkflowInstanceIdAndTaskDefinitionIn(
                parentTask.getWorkflowInstance().getId(),
                childrenDefinitions
        );

        for (TaskInstance child : childInstances) {
        if(child.getStatus() != TaskStatus.PENDING) {
            continue;
        }

        if(areAllParentsFinished(child)) {
            child.setStatus(TaskStatus.READY);
            taskInstanceRepository.save(child);
            log.info(">>> Unblocked child task: [{}] -> READY", child.getTaskDefinition().getName());
        }
        }
    }


    private boolean areAllParentsFinished(TaskInstance childTask) {

        Set<TaskDefinition> parentDefinitions = childTask.getTaskDefinition().getPreviousTasks();

        List<TaskInstance> parentInstances = taskInstanceRepository.findAllByWorkflowInstanceIdAndTaskDefinitionIn(
                childTask.getWorkflowInstance().getId(), parentDefinitions);

        return parentInstances.stream()
                .allMatch(parentInstance -> parentInstance.getStatus() == TaskStatus.SUCCESS);
    }
}
