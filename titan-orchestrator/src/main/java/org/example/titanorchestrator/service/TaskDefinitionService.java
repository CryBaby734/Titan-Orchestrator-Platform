package org.example.titanorchestrator.service;

import lombok.RequiredArgsConstructor;
import org.example.titanorchestrator.domain.TaskDefinition;
import org.example.titanorchestrator.domain.Tasktype;
import org.example.titanorchestrator.dto.TaskDefinitionRequest;
import org.example.titanorchestrator.repository.TaskDefinitionRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

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

}
