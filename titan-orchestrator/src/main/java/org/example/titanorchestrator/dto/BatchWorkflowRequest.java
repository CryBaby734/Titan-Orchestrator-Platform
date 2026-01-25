package org.example.titanorchestrator.dto;

import java.util.List;

public record BatchWorkflowRequest(
        String workflowName,
        List<BatchTaskRequest> tasks,
        boolean autoStart
) {
}
