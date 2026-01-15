package org.example.titanorchestrator.repository;

import org.example.titanorchestrator.domain.TaskDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TaskDefinitionRepository extends JpaRepository<TaskDefinition, UUID> {
}
