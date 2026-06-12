package com.studify.dto.sessionnote;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class SessionNoteDTOs {

    public record CreateRequest(
            @NotBlank(message = "Conteúdo é obrigatório")
            @Size(max = 50000, message = "Conteúdo não pode ultrapassar 50.000 caracteres")
            String content
    ) {}

    public record UpdateRequest(
            @NotBlank(message = "Conteúdo é obrigatório")
            @Size(max = 50000, message = "Conteúdo não pode ultrapassar 50.000 caracteres")
            String content
    ) {}

    public record Response(
            Long id,
            Long sessionId,
            String content,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {}
}
