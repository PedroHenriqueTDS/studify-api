package com.studify.service;

import com.studify.dto.report.ReportDTOs;
import com.studify.dto.stats.StatsDTOs;
import com.studify.entity.AttendanceRecord;
import com.studify.entity.GradeGoal;
import com.studify.entity.GradeRecord;
import com.studify.entity.StudySession;
import com.studify.entity.User;
import com.studify.repository.AttendanceRecordRepository;
import com.studify.repository.GradeGoalRepository;
import com.studify.repository.GradeRecordRepository;
import com.studify.repository.StudySessionRepository;
import com.studify.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final StudySessionRepository studySessionRepository;
    private final TaskRepository taskRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final GradeGoalRepository gradeGoalRepository;
    private final GradeRecordRepository gradeRecordRepository;
    private final StatsService statsService;

    @Transactional(readOnly = true)
    public ReportDTOs.WeeklyReport getWeeklyReport(User user, LocalDate referenceDate) {
        ZoneId userZone = ZoneId.of(user.getTimezone());

        // Calcular semana (seg–dom) a partir da data de referência
        LocalDate weekStart = referenceDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd   = referenceDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        LocalDate prevWeekStart = weekStart.minusWeeks(1);
        LocalDate prevWeekEnd   = weekEnd.minusWeeks(1);

        // Converter limites para LocalDateTime (início e fim do dia em UTC para compatibilidade com startTime)
        LocalDateTime weekStartDt   = weekStart.atStartOfDay();
        LocalDateTime weekEndDt     = weekEnd.atTime(23, 59, 59);
        LocalDateTime prevStartDt   = prevWeekStart.atStartOfDay();
        LocalDateTime prevEndDt     = prevWeekEnd.atTime(23, 59, 59);

        // ── Study Stats ─────────────────────────────────────────────────────
        List<StudySession> thisWeekSessions = studySessionRepository.findByPeriod(
                user.getId(), weekStartDt, weekEndDt.plusSeconds(1));
        List<StudySession> lastWeekSessions = studySessionRepository.findByPeriod(
                user.getId(), prevStartDt, prevEndDt.plusSeconds(1));

        // Apenas sessões COMPLETED com durationMinutes preenchido
        List<StudySession> thisCompleted = thisWeekSessions.stream()
                .filter(s -> "COMPLETED".equals(s.getStatus().name()) && s.getDurationMinutes() != null)
                .toList();
        List<StudySession> lastCompleted = lastWeekSessions.stream()
                .filter(s -> "COMPLETED".equals(s.getStatus().name()) && s.getDurationMinutes() != null)
                .toList();

        int minutesThisWeek = thisCompleted.stream().mapToInt(StudySession::getDurationMinutes).sum();
        int minutesLastWeek = lastCompleted.stream().mapToInt(StudySession::getDurationMinutes).sum();
        int deltaMinutes    = minutesThisWeek - minutesLastWeek;
        Double deltaPercentage = minutesLastWeek > 0
                ? Math.round((deltaMinutes * 100.0 / minutesLastWeek) * 10) / 10.0
                : null;

        // Minutos por dia da semana atual
        Map<LocalDate, Integer> minutesByDay = new HashMap<>();
        for (StudySession s : thisCompleted) {
            LocalDate day = s.getStartTime()
                    .atZone(ZoneId.of("UTC"))
                    .withZoneSameInstant(userZone)
                    .toLocalDate();
            minutesByDay.merge(day, s.getDurationMinutes(), Integer::sum);
        }

        List<ReportDTOs.DayStudyMinutes> byDay = new ArrayList<>();
        LocalDate cursor = weekStart;
        while (!cursor.isAfter(weekEnd)) {
            byDay.add(new ReportDTOs.DayStudyMinutes(cursor, minutesByDay.getOrDefault(cursor, 0)));
            cursor = cursor.plusDays(1);
        }

        int daysWithStudy    = (int) minutesByDay.entrySet().stream().filter(e -> e.getValue() > 0).count();
        int daysWithoutStudy = 7 - daysWithStudy;

        // Top subject da semana
        Map<Long, Integer> minutesBySubject = new HashMap<>();
        Map<Long, StudySession> subjectSampleSession = new HashMap<>();
        for (StudySession s : thisCompleted) {
            if (s.getSubject() != null) {
                minutesBySubject.merge(s.getSubject().getId(), s.getDurationMinutes(), Integer::sum);
                subjectSampleSession.putIfAbsent(s.getSubject().getId(), s);
            }
        }

        ReportDTOs.SubjectHighlight topSubject = null;
        if (!minutesBySubject.isEmpty()) {
            Long topSubjectId = Collections.max(minutesBySubject.entrySet(), Map.Entry.comparingByValue()).getKey();
            StudySession sample = subjectSampleSession.get(topSubjectId);
            topSubject = new ReportDTOs.SubjectHighlight(
                    topSubjectId,
                    sample.getSubject().getName(),
                    sample.getSubject().getColor(),
                    minutesBySubject.get(topSubjectId)
            );
        }

        ReportDTOs.StudyStats studyStats = new ReportDTOs.StudyStats(
                minutesThisWeek, minutesLastWeek, deltaMinutes, deltaPercentage,
                daysWithStudy, daysWithoutStudy, byDay
        );

        // ── Task Stats ──────────────────────────────────────────────────────
        long completedThisWeek = taskRepository.countCompletedInPeriod(
                user.getId(), weekStartDt, weekEndDt);
        long pendingTotal  = taskRepository.countPending(user.getId());
        long overdueTotal  = taskRepository.countOverdue(user.getId(), LocalDate.now(userZone));

        ReportDTOs.TaskStats taskStats = new ReportDTOs.TaskStats(
                completedThisWeek, pendingTotal, overdueTotal);

        // ── Attendance Stats ─────────────────────────────────────────────────
        List<AttendanceRecord> weekAttendance = attendanceRecordRepository
                .findByUserIdAndDateBetween(user.getId(), weekStart, weekEnd);

        long absencesThisWeek  = weekAttendance.stream().filter(ar -> !ar.isPresent()).count();
        long presencesThisWeek = weekAttendance.stream().filter(AttendanceRecord::isPresent).count();

        ReportDTOs.AttendanceStats attendanceStats = new ReportDTOs.AttendanceStats(
                absencesThisWeek, presencesThisWeek);

        // ── Grade Goal Progress ──────────────────────────────────────────────
        List<GradeGoal> goals = gradeGoalRepository
                .findByUserIdAndDeletedAtIsNull(user.getId(), Pageable.unpaged())
                .getContent();

        List<ReportDTOs.GradeGoalProgress> gradeGoalProgress = goals.stream()
                .map(goal -> {
                    List<GradeRecord> records = gradeRecordRepository
                            .findAllActiveByGoalIdAndUserId(goal.getId(), user.getId());

                    double weightedSum   = records.stream().mapToDouble(r -> r.getGrade() * r.getWeight()).sum();
                    double weightRecorded = records.stream().mapToDouble(GradeRecord::getWeight).sum();
                    double weightRemaining = goal.getTotalWeight() - weightRecorded;
                    double currentAverage = weightRecorded > 0 ? weightedSum / weightRecorded : 0.0;

                    String status;
                    if (weightRemaining <= 0.0) {
                        status = currentAverage >= goal.getTargetGrade() ? "ACHIEVED" : "FAILED";
                    } else {
                        double needed = (goal.getTargetGrade() * goal.getTotalWeight() - weightedSum) / weightRemaining;
                        if (needed > 10.0)                         status = "FAILED";
                        else if (needed <= goal.getTargetGrade())  status = "ON_TRACK";
                        else                                       status = "AT_RISK";
                    }

                    return new ReportDTOs.GradeGoalProgress(
                            goal.getId(),
                            goal.getTitle(),
                            goal.getSubject().getName(),
                            goal.getTargetGrade(),
                            Math.round(currentAverage * 100.0) / 100.0,
                            status
                    );
                })
                .toList();

        // ── Streak ──────────────────────────────────────────────────────────
        StatsDTOs.StreakResponse streak = statsService.getStreak(user);
        ReportDTOs.StreakInfo streakInfo = new ReportDTOs.StreakInfo(
                streak.currentStreak(), streak.longestStreak());

        return new ReportDTOs.WeeklyReport(
                weekStart, weekEnd,
                studyStats, taskStats, attendanceStats,
                gradeGoalProgress, streakInfo, topSubject
        );
    }

    @Transactional(readOnly = true)
    public ReportDTOs.WeeklyReport getCurrentWeekReport(User user) {
        LocalDate today = LocalDate.now(ZoneId.of(user.getTimezone()));
        return getWeeklyReport(user, today);
    }
}
