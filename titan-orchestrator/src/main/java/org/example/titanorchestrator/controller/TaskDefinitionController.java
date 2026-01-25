package org.example.titanorchestrator.controller;

import lombok.RequiredArgsConstructor;
import org.example.titanorchestrator.dto.BatchTaskRequest;
import org.example.titanorchestrator.dto.BatchWorkflowRequest;
import org.example.titanorchestrator.dto.TaskDefinitionRequest;
import org.example.titanorchestrator.service.TaskDefinitionService;
import org.example.titanorchestrator.service.WorkflowService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskDefinitionController {


    private final TaskDefinitionService service;
    private final WorkflowService workflowService;


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


    @PostMapping("/batch")
    public ResponseEntity<List<UUID>> createTaskBatch(@RequestBody BatchWorkflowRequest request){

        List<UUID> taskIds = service.createBatch(request);

        if(request.autoStart()) {
            workflowService.startWorkflow(taskIds);
        }
        return ResponseEntity.ok(taskIds);
    }
}
