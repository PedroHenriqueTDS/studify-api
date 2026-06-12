package com.studify.dto.subject;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class SubjectDTOs {

    public record CreateRequest(
            @NotBlank(message = "Nome é obrigatório")
            @Size(min = 1, max = 100, message = "Nome deve ter até 100 caracteres")
            String name,

            @Size(max = 500, message = "Descrição deve ter até 500 caracteres")
            String description,

            @Pattern(regexp = "^#([A-Fa-f0-9]{6})$", message = "Cor deve ser um HEX válido (ex: #6366F1)")
            String color,

            @Min(value = 0, message = "Limite mínimo de faltas é 0%")
            @Max(value = 100, message = "Limite máximo de faltas é 100%")
            Integer maxAbsencePercentage
    ) {}

    public record UpdateRequest(
            @Size(min = 1, max = 100, message = "Nome deve ter até 100 caracteres")
            String name,

            @Size(max = 500, message = "Descrição deve ter até 500 caracteres")
            String description,

            @Pattern(regexp = "^#([A-Fa-f0-9]{6})$", message = "Cor deve ser um HEX válido")
            String color,

            @Min(value = 0, message = "Limite mínimo de faltas é 0%")
            @Max(value = 100, message = "Limite máximo de faltas é 100%")
            Integer maxAbsencePercentage
    ) {}

    public record Response(
            Long id,
            String name,
            String description,
            String color,
            Integer totalStudyMinutes,
            Long totalSessions,
            Integer maxAbsencePercentage,
            LocalDateTime createdAt
    ) {}

    public record SimpleResponse(
            Long id,
            String name,
            String color
    ) {}
}
