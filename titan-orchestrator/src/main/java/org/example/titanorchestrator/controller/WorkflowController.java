package org.example.titanorchestrator.controller;

import lombok.RequiredArgsConstructor;
import org.example.titanorchestrator.service.WorkflowService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workflows")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowService workflowService;


    @PostMapping("/start")
    public ResponseEntity<UUID> startWorkflow (@RequestBody List<UUID> taskDefinitionIds) {
        UUID workflowId = workflowService.startWorkflow(taskDefinitionIds);
        return ResponseEntity.ok(workflowId);
    }
}
