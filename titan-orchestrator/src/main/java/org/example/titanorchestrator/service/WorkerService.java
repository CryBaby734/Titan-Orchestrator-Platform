package org.example.titanorchestrator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.titanorchestrator.domain.TaskInstance;
import org.example.titanorchestrator.domain.TaskStatus;
import org.example.titanorchestrator.dto.TaskMessage;
import org.example.titanorchestrator.repository.TaskInstanceRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

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
}
