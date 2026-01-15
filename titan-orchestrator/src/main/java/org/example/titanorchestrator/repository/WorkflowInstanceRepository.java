package org.example.titanorchestrator.repository;


import org.example.titanorchestrator.domain.WorkflowInstance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WorkflowInstanceRepository extends JpaRepository<WorkflowInstance, UUID>{
}
