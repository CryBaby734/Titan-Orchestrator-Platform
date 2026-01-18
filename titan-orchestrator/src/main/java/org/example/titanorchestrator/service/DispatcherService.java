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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DispatcherService {

    private final TaskInstanceRepository repository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String QUEUE_NAME = "titan_tasks_queue";

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void dispatch(){
        List<TaskInstance> readyTasks = repository.findAllByStatus(TaskStatus.READY);

        if(readyTasks.isEmpty()){
            return;
        }
        log.info("Found {} tasks ready for dispatch", readyTasks.size());

        for(TaskInstance task : readyTasks) {
            TaskMessage message = new TaskMessage(
                    task.getId(),
                    task.getTaskDefinition().getTaskType().name(),
                    task.getTaskDefinition().getPayload()
            );

            try {
                redisTemplate.opsForList().leftPush(QUEUE_NAME, message);
                task.setStatus(TaskStatus.IN_PROGRESS);
                task.setStartedAt(java.time.LocalDateTime.now());
                repository.save(task);

                log.info("Task {} dispatched to Redis", task.getId());
            }catch (Exception e){
                log.error("Failed to dispatch task {}", task.getId(), e);
            }
        }
    }
}
