package org.example.titanorchestrator.repository;

import org.example.titanorchestrator.domain.TaskDefinition;
import org.example.titanorchestrator.domain.TaskInstance;
import org.example.titanorchestrator.domain.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface TaskInstanceRepository extends JpaRepository<TaskInstance, UUID> {
    List<TaskInstance> findAllByStatus(TaskStatus status);

    List<TaskInstance> findAllByWorkflowInstanceIdAndTaskDefinitionIn(UUID id, Set<TaskDefinition> childrenDefinitions);
}
