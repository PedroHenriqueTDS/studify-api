package com.studify.dto.task;

import com.studify.entity.Task;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TaskDTOs {

    public record CreateRequest(
            @NotBlank(message = "Título é obrigatório")
            @Size(max = 200, message = "Título deve ter até 200 caracteres")
            String title,

            @Size(max = 1000)
            String description,

            Long subjectId,

            Task.Priority priority,

            LocalDate dueDate
    ) {}

    public record UpdateRequest(
            @Size(max = 200)
            String title,

            @Size(max = 1000)
            String description,

            Long subjectId,

            Task.Priority priority,

            Task.TaskStatus status,

            LocalDate dueDate
    ) {}

    public record Response(
            Long id,
            String title,
            String description,
            Long subjectId,
            String subjectName,
            Task.Priority priority,
            Task.TaskStatus status,
            LocalDate dueDate,
            LocalDateTime completedAt,
            LocalDateTime createdAt
    ) {}
}
