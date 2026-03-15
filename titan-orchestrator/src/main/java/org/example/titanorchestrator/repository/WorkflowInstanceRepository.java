package org.example.titanorchestrator.repository;


import org.example.titanorchestrator.domain.WorkflowInstance;
import org.example.titanorchestrator.dto.WorkflowStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WorkflowInstanceRepository extends JpaRepository<WorkflowInstance, UUID>{

    List<WorkflowInstance> findAllByStatus(WorkflowStatus status);
}
