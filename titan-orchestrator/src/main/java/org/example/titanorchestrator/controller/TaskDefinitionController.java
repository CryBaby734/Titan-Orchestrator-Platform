package org.example.titanorchestrator.controller;

import lombok.RequiredArgsConstructor;
import org.example.titanorchestrator.dto.TaskDefinitionRequest;
import org.example.titanorchestrator.service.TaskDefinitionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskDefinitionController {


    private final TaskDefinitionService service;


    @PostMapping
    public ResponseEntity<UUID> createTask (@RequestBody TaskDefinitionRequest request){
        UUID taskId = service.createTask(request);
        return ResponseEntity.ok(taskId);
    }

    @PostMapping("/{parentId}/link/{childId}")
    public ResponseEntity<Void> linkTasks(
            @PathVariable UUID parentId,
            @PathVariable UUID childId
    ) {
        service.linkTasks(parentId, childId);
        return ResponseEntity.ok().build();
    }
}
