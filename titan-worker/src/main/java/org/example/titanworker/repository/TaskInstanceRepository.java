package org.example.titanworker.repository;

import org.example.titanworker.domain.TaskDefinition;
import org.example.titanworker.domain.TaskInstance;
import org.example.titanworker.domain.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface TaskInstanceRepository extends JpaRepository<TaskInstance, UUID> {
    List<TaskInstance> findAllByStatus(TaskStatus status);

    List<TaskInstance> findAllByWorkflowInstanceIdAndTaskDefinitionIn(UUID id, Set<TaskDefinition> childrenDefinitions);
}
