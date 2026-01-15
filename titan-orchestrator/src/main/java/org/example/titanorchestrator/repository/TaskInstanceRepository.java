package org.example.titanorchestrator.repository;

import org.example.titanorchestrator.domain.TaskInstance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TaskInstanceRepository extends JpaRepository<TaskInstance, UUID> {
}
