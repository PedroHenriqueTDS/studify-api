package com.studify.dto.attendance;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class AttendanceDTOs {

    public record CreateRequest(
            @NotNull(message = "ID da aula recorrente é obrigatório")
            Long recurringClassId,

            @NotNull(message = "Data é obrigatória")
            LocalDate date,

            boolean present
    ) {}

    public record Response(
            Long id,
            Long recurringClassId,
            String recurringClassTitle,
            Long subjectId,
            String subjectName,
            LocalDate date,
            boolean present,
            LocalDateTime createdAt
    ) {}

    public record SubjectAttendanceSummary(
            Long subjectId,
            String subjectName,
            String subjectColor,
            int totalClasses,
            int currentAbsences,
            int currentPresences,
            int totalMarked,
            double absencePercentage,
            Integer maxAbsencePercentage,
            String status // SAFE, WARNING, DANGER, EXCEEDED
    ) {}

    public record GlobalAttendanceSummary(
            int totalSubjects,
            int subjectsWarning,
            int subjectsDanger,
            int subjectsExceeded,
            List<SubjectAttendanceSummary> subjectSummaries
    ) {}
}
