package com.studify.dto.analytics;

import java.time.LocalDate;
import java.util.List;

public class AnalyticsDTOs {

    // ── GET /analytics/hours-by-subject ──────────────────────────────────────

    public record SubjectHours(
            Long subjectId,
            String subjectName,
            String subjectColor,
            int totalMinutes
    ) {}

    public record HoursBySubjectResponse(
            LocalDate start,
            LocalDate end,
            List<SubjectHours> subjects
    ) {}

    // ── GET /analytics/weekly-evolution ──────────────────────────────────────

    public record WeekEntry(
            LocalDate weekStart,
            LocalDate weekEnd,
            int totalMinutes
    ) {}

    public record WeeklyEvolutionResponse(
            int weeks,
            List<WeekEntry> data
    ) {}

    // ── GET /analytics/goals-vs-actual ───────────────────────────────────────

    public record GoalVsActual(
            Long goalId,
            Long subjectId,
            String subjectName,
            String subjectColor,
            int targetMinutes,
            int actualMinutes,
            double completionPercentage
    ) {}

    public record GoalsVsActualResponse(
            List<GoalVsActual> items
    ) {}

    // ── GET /analytics/productivity-by-weekday ────────────────────────────────

    public record WeekdayProductivity(
            int dayOfWeek,        // 1=Segunda … 7=Domingo (ISO)
            String label,         // "Segunda", "Terça" …
            double avgMinutes,
            int totalMinutes,
            int sessionCount
    ) {}

    public record ProductivityByWeekdayResponse(
            List<WeekdayProductivity> days
    ) {}

    // ── GET /analytics/best-hours ─────────────────────────────────────────────

    public record HourProductivity(
            int hour,             // 0–23
            String label,         // "00:00", "08:00" …
            double avgMinutes,
            int totalMinutes,
            int sessionCount
    ) {}

    public record BestHoursResponse(
            List<HourProductivity> hours
    ) {}
}
