package org.example.titanworker.domain;

public enum TaskStatus {
    PENDING,     // Ждет, пока выполнятся родители
    READY,       // Родителей нет (или они SUCCESS), можно брать в работу
    IN_PROGRESS, // Воркер трудится
    SUCCESS,     // Готово
    FAILED       // Ошибка
}