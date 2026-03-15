package org.example.titanorchestrator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.titanorchestrator.domain.TaskInstance;
import org.example.titanorchestrator.domain.TaskStatus;
import org.example.titanorchestrator.domain.WorkflowInstance;
import org.example.titanorchestrator.dto.WorkflowStatus;
import org.example.titanorchestrator.repository.TaskInstanceRepository;
import org.example.titanorchestrator.repository.WorkflowInstanceRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowMonitorService {

    private final WorkflowInstanceRepository workflowInstanceRepository;
    private final TaskInstanceRepository taskInstanceRepository;


    @Scheduled(fixedRate = 5000)
    @Transactional
    public void monitorWorkflows(){
        List<WorkflowInstance> activeWorkflows = workflowInstanceRepository.findAllByStatus(WorkflowStatus.RUNNING);

        for (WorkflowInstance workflow : activeWorkflows) {
            checkandUpdateWorkflowStatus(workflow);
        }
    }

    private void checkandUpdateWorkflowStatus(WorkflowInstance workflow) {
        List<TaskInstance> tasks = taskInstanceRepository.findAllByWorkflowInstanceId(workflow.getId());

        if(tasks.isEmpty()){
            return;
        }

        boolean allSuccess = true;
        boolean anyFailed = false;

        for (TaskInstance task : tasks) {
            if(task.getStatus() == TaskStatus.FAILED){
                anyFailed = true;
                break;
            }

          if(task.getStatus() != TaskStatus.SUCCESS){
              allSuccess = false;
          }
        }

      if(anyFailed) {
          log.error(">>> Workflow [{}] FAILED because one or more tasks failed", workflow.getId());
          workflow.setStatus(WorkflowStatus.FAILED);
          workflowInstanceRepository.save(workflow);
      }

      if (allSuccess) {
          log.info(">>> Workflow [{}] COMPLETED successfully! All tasks are done.", workflow.getId());
          workflow.setStatus(WorkflowStatus.COMPLETED);
          workflowInstanceRepository.save(workflow);
      }
    }
}
