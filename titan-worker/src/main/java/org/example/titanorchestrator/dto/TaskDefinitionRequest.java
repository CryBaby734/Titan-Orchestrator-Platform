package org.example.titanorchestrator.dto;

import java.util.Map;

public record TaskDefinitionRequest(
                String alias,
                String name,
                String taskType,
                Map<String, Object> payload,
                Integer retryCount) {
}
