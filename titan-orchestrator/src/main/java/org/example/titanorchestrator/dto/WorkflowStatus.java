package org.example.titanorchestrator.dto;

public enum WorkflowStatus {
    PENDING,    // Только создан, еще не запущен
    RUNNING,    // В процессе выполнения
    COMPLETED,  // Все задачи успешно завершены
    FAILED,     // Хотя бы одна задача упала
    CANCELLED   // Отменен пользователем
}