package com.studify.dto.academicevent;

import com.studify.entity.AcademicEvent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class AcademicEventDTOs {

    public record CreateRequest(
            @NotBlank(message = "Título é obrigatório")
            @Size(max = 255, message = "Título deve ter até 255 caracteres")
            String title,

            String description,

            @NotNull(message = "Tipo do evento é obrigatório")
            AcademicEvent.EventType type,

            @NotNull(message = "Data/hora de início é obrigatória")
            LocalDateTime startDateTime,

            LocalDateTime endDateTime,

            @Size(max = 255)
            String location,

            Long subjectId
    ) {}

    public record UpdateRequest(
            @Size(max = 255)
            String title,

            String description,

            AcademicEvent.EventType type,

            LocalDateTime startDateTime,

            LocalDateTime endDateTime,

            @Size(max = 255)
            String location,

            Long subjectId
    ) {}

    public record Response(
            Long id,
            String title,
            String description,
            AcademicEvent.EventType type,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            String location,
            Long subjectId,
            String subjectName,
            String subjectColor,
            LocalDateTime createdAt
    ) {}
}
