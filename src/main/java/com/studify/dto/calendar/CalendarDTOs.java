package com.studify.dto.calendar;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.studify.entity.AcademicEvent;
import com.studify.entity.StudySession;
import com.studify.entity.Task;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class CalendarDTOs {

    /**
     * Item unificado do calendário. sourceType diferencia a origem para o front-end.
     * Campos não aplicáveis ao tipo ficam null — @JsonInclude(NON_NULL) elimina do JSON.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record CalendarItem(
            /** ID sintético único: "event-12", "class-5-2025-04-08", "task-7", "session-3" */
            String id,

            /** ID real na tabela de origem */
            Long sourceId,

            /** ACADEMIC_EVENT | RECURRING_CLASS | TASK | STUDY_SESSION */
            SourceType sourceType,

            String title,

            /** EXAM | ASSIGNMENT | PRESENTATION | REMINDER | OTHER | CLASS | TASK_DUE | STUDY_SESSION */
            String eventType,

            LocalDateTime startDateTime,
            LocalDateTime endDateTime,

            /** true para tasks com dueDate (exibidas como evento de dia inteiro) */
            boolean allDay,

            String location,
            Long subjectId,
            String subjectName,
            String subjectColor,

            /** Preenchido apenas para TASK */
            Task.Priority priority,

            /** Preenchido para TASK e STUDY_SESSION */
            String status,

            Map<String, Object> metadata
    ) {}

    public record CalendarResponse(
            LocalDate start,
            LocalDate end,
            List<CalendarItem> items
    ) {}

    public enum SourceType {
        ACADEMIC_EVENT, RECURRING_CLASS, TASK, STUDY_SESSION
    }
}
