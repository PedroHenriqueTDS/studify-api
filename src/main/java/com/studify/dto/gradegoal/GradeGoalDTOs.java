package com.studify.dto.gradegoal;

import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.List;

public class GradeGoalDTOs {

    // ─── GradeGoal requests ───────────────────────────────────────────────────

    public record CreateRequest(
            @NotBlank(message = "Título é obrigatório")
            @Size(max = 100, message = "Título deve ter no máximo 100 caracteres")
            String title,

            @NotNull(message = "Matéria é obrigatória")
            Long subjectId,

            @NotNull(message = "Nota-alvo é obrigatória")
            @DecimalMin(value = "0.0", message = "Nota mínima é 0.0")
            @DecimalMax(value = "10.0", message = "Nota máxima é 10.0")
            Double targetGrade,

            @NotNull(message = "Peso total é obrigatório")
            @DecimalMin(value = "0.01", inclusive = true, message = "Peso total deve ser maior que zero")
            Double totalWeight
    ) {}

    public record UpdateRequest(
            @Size(max = 100, message = "Título deve ter no máximo 100 caracteres")
            String title,

            @DecimalMin(value = "0.0", message = "Nota mínima é 0.0")
            @DecimalMax(value = "10.0", message = "Nota máxima é 10.0")
            Double targetGrade,

            @DecimalMin(value = "0.01", inclusive = true, message = "Peso total deve ser maior que zero")
            Double totalWeight
    ) {}

    // ─── GradeRecord requests ─────────────────────────────────────────────────

    public record CreateRecordRequest(
            @NotBlank(message = "Título é obrigatório")
            @Size(max = 100, message = "Título deve ter no máximo 100 caracteres")
            String title,

            @NotNull(message = "Nota é obrigatória")
            @DecimalMin(value = "0.0", message = "Nota mínima é 0.0")
            @DecimalMax(value = "10.0", message = "Nota máxima é 10.0")
            Double grade,

            @NotNull(message = "Peso é obrigatório")
            @DecimalMin(value = "0.01", inclusive = true, message = "Peso deve ser maior que zero")
            Double weight
    ) {}

    public record UpdateRecordRequest(
            @Size(max = 100, message = "Título deve ter no máximo 100 caracteres")
            String title,

            @DecimalMin(value = "0.0", message = "Nota mínima é 0.0")
            @DecimalMax(value = "10.0", message = "Nota máxima é 10.0")
            Double grade,

            @DecimalMin(value = "0.01", inclusive = true, message = "Peso deve ser maior que zero")
            Double weight
    ) {}

    // ─── Responses ────────────────────────────────────────────────────────────

    public record GoalResponse(
            Long id,
            String title,
            Long subjectId,
            String subjectName,
            String subjectColor,
            Double targetGrade,
            Double totalWeight,
            LocalDateTime createdAt
    ) {}

    public record RecordResponse(
            Long id,
            String title,
            Double grade,
            Double weight,
            LocalDateTime createdAt
    ) {}

    public record SummaryResponse(
            Long gradeGoalId,
            String title,
            Long subjectId,
            String subjectName,
            Double targetGrade,
            Double totalWeight,
            Double weightRecorded,
            Double weightRemaining,
            Double currentAverage,
            Double neededGrade,     // null se não há peso restante
            String status,          // ON_TRACK / AT_RISK / FAILED / ACHIEVED
            List<RecordResponse> records
    ) {}
}
