package org.example.titanorchestrator.service;

import lombok.RequiredArgsConstructor;
import org.example.titanorchestrator.domain.TaskDefinition;
import org.example.titanorchestrator.domain.Tasktype;
import org.example.titanorchestrator.dto.BatchTaskRequest;
import org.example.titanorchestrator.dto.BatchWorkflowRequest;
import org.example.titanorchestrator.dto.TaskDefinitionRequest;
import org.example.titanorchestrator.repository.TaskDefinitionRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class TaskDefinitionService {

    private final TaskDefinitionRepository repository;


    @Transactional
    public UUID createTask(TaskDefinitionRequest request){

        TaskDefinition task = new TaskDefinition();
        task.setName(request.name());

        task.setTaskType(Tasktype.valueOf(request.taskType()));

        task.setPayload(request.payload());

        if (request.retryCount() != null) {
            task.setRetryCount(request.retryCount());
        }


        TaskDefinition savedTask = repository.save(task);

        return savedTask.getId();
    }

    @Transactional
    public void linkTasks(UUID parentId, UUID childId) {
        TaskDefinition parent = repository.findById(parentId)
                .orElseThrow(() -> new IllegalArgumentException("Parent not found"));

        TaskDefinition child = repository.findById(childId)
                .orElseThrow(() -> new IllegalArgumentException("Child not found"));


        parent.getNextTasks().add(child);

        repository.save(parent);

    }


    @Transactional
    public List<UUID> createBatch(BatchWorkflowRequest request){

        Map<String, TaskDefinition> aliasToEntityMap = new HashMap<>();
        List<UUID> createdIds = new ArrayList<>();


        for (BatchTaskRequest req : request.tasks()) {
            TaskDefinition task = new TaskDefinition();
            task.setName(req.name());
            task.setTaskType(Tasktype.valueOf(req.type()));
            task.setPayload(req.payload());

            task = repository.save(task);

            aliasToEntityMap.put(req.alias(), task);
            createdIds.add(task.getId());
        }

        for(BatchTaskRequest req : request.tasks()) {

            if(req.dependsOn() != null && !req.dependsOn().isEmpty()) {

                TaskDefinition child = aliasToEntityMap.get(req.alias());

                for(String parentAlias : req.dependsOn()) {
                    TaskDefinition parent = aliasToEntityMap.get(parentAlias);

                    if(parent == null) {
                        throw new IllegalArgumentException("Parent not found" + parentAlias);
                    }


                    parent.getNextTasks().add(child);
                    repository.save(parent);
                }

            }
        }
        return createdIds;
    }

}
