package org.example.titanworker.executor;


import org.example.titanworker.domain.TaskDefinition;

public interface TaskExecutor {


    boolean canExecute(String taskType);

    String execute(TaskDefinition taskDefinition);
}
