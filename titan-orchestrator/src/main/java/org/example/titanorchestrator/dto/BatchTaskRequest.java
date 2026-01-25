package org.example.titanorchestrator.dto;

import java.util.List;
import java.util.Map;

public record BatchTaskRequest(
        String alias,
        String name,
        String type,
        Map<String, Object> payload,
        List<String> dependsOn
) {
}
