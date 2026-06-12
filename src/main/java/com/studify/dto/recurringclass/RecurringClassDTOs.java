package com.studify.dto.recurringclass;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

public class RecurringClassDTOs {

    public record CreateRequest(
            @NotBlank(message = "Título é obrigatório")
            @Size(max = 255, message = "Título deve ter até 255 caracteres")
            String title,

            @NotNull(message = "Dia da semana é obrigatório")
            DayOfWeek dayOfWeek,

            @NotNull(message = "Horário de início é obrigatório")
            LocalTime startTime,

            @NotNull(message = "Horário de fim é obrigatório")
            LocalTime endTime,

            @Size(max = 255)
            String location,

            @NotNull(message = "Data de início do semestre é obrigatória")
            LocalDate startDate,

            @NotNull(message = "Data de fim do semestre é obrigatória")
            LocalDate endDate,

            Long subjectId
    ) {}

    public record UpdateRequest(
            @Size(max = 255)
            String title,

            DayOfWeek dayOfWeek,

            LocalTime startTime,

            LocalTime endTime,

            @Size(max = 255)
            String location,

            LocalDate startDate,

            LocalDate endDate,

            Long subjectId,

            Boolean active
    ) {}

    public record Response(
            Long id,
            String title,
            DayOfWeek dayOfWeek,
            LocalTime startTime,
            LocalTime endTime,
            String location,
            LocalDate startDate,
            LocalDate endDate,
            boolean active,
            Long subjectId,
            String subjectName,
            String subjectColor,
            java.time.LocalDateTime createdAt
    ) {}
}
