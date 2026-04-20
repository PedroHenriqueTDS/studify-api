package com.studify.dto.studysession;

import com.studify.entity.StudySession;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public class StudySessionDTOs {

    public record CreateRequest(
            @NotNull(message = "ID da matéria é obrigatório")
            Long subjectId,

            @NotNull(message = "Hora de início é obrigatória")
            LocalDateTime startTime,

            LocalDateTime endTime,

            String notes
    ) {}

    public record UpdateRequest(
            LocalDateTime startTime,
            LocalDateTime endTime,
            String notes,
            StudySession.SessionStatus status
    ) {}

    public record FinishRequest(
            @NotNull(message = "Hora de término é obrigatória")
            LocalDateTime endTime,

            String notes
    ) {}

    public record Response(
            Long id,
            Long subjectId,
            String subjectName,
            String subjectColor,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Integer durationMinutes,
            String notes,
            StudySession.SessionStatus status,
            LocalDateTime createdAt
    ) {}

    public record SummaryResponse(
            Integer totalMinutes,
            Integer totalHours,
            Long totalSessions,
            Double averageMinutesPerSession
    ) {}
}
