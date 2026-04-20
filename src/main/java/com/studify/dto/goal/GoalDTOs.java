package com.studify.dto.goal;

import com.studify.entity.Goal;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class GoalDTOs {

    public record CreateRequest(
            @NotBlank(message = "Título é obrigatório")
            @Size(max = 200, message = "Título deve ter até 200 caracteres")
            String title,

            @Size(max = 1000, message = "Descrição deve ter até 1000 caracteres")
            String description,

            Long subjectId,

            @NotNull(message = "Meta de horas é obrigatória")
            @Positive(message = "Meta de horas deve ser positiva")
            Double targetHours,

            LocalDate deadline
    ) {}

    public record UpdateRequest(
            @Size(max = 200)
            String title,

            @Size(max = 1000)
            String description,

            Long subjectId,

            @Positive(message = "Meta de horas deve ser positiva")
            Double targetHours,

            LocalDate deadline,

            Goal.GoalStatus status
    ) {}

    public record Response(
            Long id,
            String title,
            String description,
            Long subjectId,
            String subjectName,
            Double targetHours,
            Double currentHours,
            Double progressPercentage,
            LocalDate deadline,
            Goal.GoalStatus status,
            LocalDateTime createdAt
    ) {}
}
