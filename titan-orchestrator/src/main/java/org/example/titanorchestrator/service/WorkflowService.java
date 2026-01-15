package org.example.titanorchestrator.service;

import lombok.RequiredArgsConstructor;
import org.example.titanorchestrator.domain.TaskDefinition;
import org.example.titanorchestrator.domain.TaskInstance;
import org.example.titanorchestrator.domain.TaskStatus;
import org.example.titanorchestrator.domain.WorkflowInstance;
import org.example.titanorchestrator.dto.TaskDefinitionRequest;
import org.example.titanorchestrator.repository.TaskDefinitionRepository;
import org.example.titanorchestrator.repository.TaskInstanceRepository;
import org.example.titanorchestrator.repository.WorkflowInstanceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkflowService {

    private final TaskDefinitionRepository taskDefinitionRepository;
    private final WorkflowInstanceRepository workflowInstanceRepository;
    private final TaskInstanceRepository taskInstanceRepository;

    @Transactional
    public UUID startWorkflow(List<UUID> taskDefinitionIds) {
        WorkflowInstance workflow = new WorkflowInstance();
        workflow.setStatus(TaskStatus.IN_PROGRESS);
        workflow = workflowInstanceRepository.save(workflow);

        List<TaskDefinition> definitions = taskDefinitionRepository.findAllById(taskDefinitionIds);

        List<TaskInstance> instances = new ArrayList<>();

        for(TaskDefinition def : definitions) {
            TaskInstance instance = new TaskInstance();
            instance.setTaskDefinition(def);
            instance.setWorkflowInstance(workflow);

            if(def.getPreviousTasks().isEmpty()) {
                instance.setStatus(TaskStatus.READY);
            } else {
                instance.setStatus(TaskStatus.PENDING);
            }
            instances.add(instance);
        }
        taskInstanceRepository.saveAll(instances);
        return workflow.getId();
    }
}
