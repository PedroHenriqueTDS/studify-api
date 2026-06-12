package com.studify.dto.report;

import java.time.LocalDate;
import java.util.List;

public class ReportDTOs {

    public record WeeklyReport(
            LocalDate weekStart,
            LocalDate weekEnd,
            StudyStats studyStats,
            TaskStats taskStats,
            AttendanceStats attendanceStats,
            List<GradeGoalProgress> gradeGoalProgress,
            StreakInfo streakInfo,
            SubjectHighlight topSubject  // null se não houve estudo na semana
    ) {}

    public record StudyStats(
            int minutesThisWeek,
            int minutesLastWeek,
            int deltaMinutes,
            Double deltaPercentage,   // null se semana anterior = 0
            int daysWithStudy,
            int daysWithoutStudy,
            List<DayStudyMinutes> byDay
    ) {}

    public record DayStudyMinutes(
            LocalDate date,
            int minutes
    ) {}

    public record TaskStats(
            long completedThisWeek,
            long pendingTotal,
            long overdueTotal
    ) {}

    public record AttendanceStats(
            long absencesThisWeek,
            long presencesThisWeek
    ) {}

    public record GradeGoalProgress(
            Long goalId,
            String title,
            String subjectName,
            Double targetGrade,
            Double currentAverage,
            String status
    ) {}

    public record StreakInfo(
            int currentStreak,
            int longestStreak
    ) {}

    public record SubjectHighlight(
            Long subjectId,
            String subjectName,
            String subjectColor,
            int minutesThisWeek
    ) {}
}
