package com.studify.dto.subject;

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
            String color
    ) {}

    public record UpdateRequest(
            @Size(min = 1, max = 100, message = "Nome deve ter até 100 caracteres")
            String name,

            @Size(max = 500, message = "Descrição deve ter até 500 caracteres")
            String description,

            @Pattern(regexp = "^#([A-Fa-f0-9]{6})$", message = "Cor deve ser um HEX válido")
            String color
    ) {}

    public record Response(
            Long id,
            String name,
            String description,
            String color,
            Integer totalStudyMinutes,
            Long totalSessions,
            LocalDateTime createdAt
    ) {}

    public record SimpleResponse(
            Long id,
            String name,
            String color
    ) {}
}
