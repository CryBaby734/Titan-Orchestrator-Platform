package org.example.titanorchestrator.dto;

import java.util.Map;
import java.util.UUID;

public record TaskMessage(
        UUID taskInstanceId,
        String taskType,
        Map<String, Object> payload
) {
}
