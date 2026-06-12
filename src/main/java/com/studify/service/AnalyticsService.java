package com.studify.service;

import com.studify.dto.analytics.AnalyticsDTOs;
import com.studify.entity.Goal;
import com.studify.entity.StudySession;
import com.studify.entity.User;
import com.studify.exception.BusinessException;
import com.studify.repository.GoalRepository;
import com.studify.repository.StudySessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private static final int MAX_WEEKS = 52;

    private final StudySessionRepository studySessionRepository;
    private final GoalRepository goalRepository;

    // ── Horas por matéria no período ─────────────────────────────────────────

    @Transactional(readOnly = true)
    public AnalyticsDTOs.HoursBySubjectResponse getHoursBySubject(User user, LocalDate start, LocalDate end) {
        if (start.isAfter(end)) throw new BusinessException("Data de início deve ser anterior à data de fim.");
        if (start.plusDays(365).isBefore(end)) throw new BusinessException("Período máximo permitido: 365 dias.");

        List<StudySession> sessions = studySessionRepository.findCompletedInRange(
                user.getId(), start.atStartOfDay(), end.plusDays(1).atStartOfDay());

        // Agrupar por matéria, somar minutos
        Map<Long, Integer> minutesBySubject = new LinkedHashMap<>();
        Map<Long, StudySession> sampleBySubject = new LinkedHashMap<>();

        for (StudySession s : sessions) {
            if (s.getSubject() == null) continue;
            Long sid = s.getSubject().getId();
            minutesBySubject.merge(sid, s.getDurationMinutes(), Integer::sum);
            sampleBySubject.putIfAbsent(sid, s);
        }

        List<AnalyticsDTOs.SubjectHours> subjects = minutesBySubject.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                .map(e -> {
                    StudySession sample = sampleBySubject.get(e.getKey());
                    return new AnalyticsDTOs.SubjectHours(
                            e.getKey(),
                            sample.getSubject().getName(),
                            sample.getSubject().getColor(),
                            e.getValue()
                    );
                })
                .toList();

        return new AnalyticsDTOs.HoursBySubjectResponse(start, end, subjects);
    }

    // ── Evolução semanal ─────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public AnalyticsDTOs.WeeklyEvolutionResponse getWeeklyEvolution(User user, int weeks) {
        int capped = Math.min(weeks, MAX_WEEKS);
        ZoneId userZone = ZoneId.of(user.getTimezone());

        LocalDate today = LocalDate.now(userZone);
        LocalDate currentWeekMonday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate rangeStart = currentWeekMonday.minusWeeks(capped - 1);

        List<StudySession> sessions = studySessionRepository.findCompletedInRange(
                user.getId(), rangeStart.atStartOfDay(), today.plusDays(1).atStartOfDay());

        // Indexar minutos por LocalDate (no timezone do usuário)
        Map<LocalDate, Integer> minutesByDay = new HashMap<>();
        for (StudySession s : sessions) {
            LocalDate day = s.getStartTime()
                    .atZone(ZoneId.of("UTC"))
                    .withZoneSameInstant(userZone)
                    .toLocalDate();
            minutesByDay.merge(day, s.getDurationMinutes(), Integer::sum);
        }

        // Construir uma entrada por semana (com zeros explícitos)
        List<AnalyticsDTOs.WeekEntry> data = new ArrayList<>();
        LocalDate weekStart = rangeStart;
        for (int i = 0; i < capped; i++) {
            LocalDate weekEnd = weekStart.plusDays(6);
            int totalMinutes = 0;
            LocalDate d = weekStart;
            while (!d.isAfter(weekEnd)) {
                totalMinutes += minutesByDay.getOrDefault(d, 0);
                d = d.plusDays(1);
            }
            data.add(new AnalyticsDTOs.WeekEntry(weekStart, weekEnd, totalMinutes));
            weekStart = weekStart.plusWeeks(1);
        }

        return new AnalyticsDTOs.WeeklyEvolutionResponse(capped, data);
    }

    // ── Metas vs. Horas Reais ────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public AnalyticsDTOs.GoalsVsActualResponse getGoalsVsActual(User user) {
        List<Goal> goals = goalRepository.findByUserId(user.getId(), Pageable.unpaged()).getContent();

        List<AnalyticsDTOs.GoalVsActual> items = goals.stream()
                .filter(g -> g.getSubject() != null)
                .map(g -> {
                    int targetMinutes = (int) Math.round(g.getTargetHours() * 60);
                    int actualMinutes = studySessionRepository
                            .sumDurationByUserIdAndSubjectId(user.getId(), g.getSubject().getId());
                    double pct = targetMinutes > 0
                            ? Math.min(Math.round((actualMinutes * 100.0 / targetMinutes) * 10) / 10.0, 100.0)
                            : 0.0;

                    return new AnalyticsDTOs.GoalVsActual(
                            g.getId(),
                            g.getSubject().getId(),
                            g.getSubject().getName(),
                            g.getSubject().getColor(),
                            targetMinutes,
                            actualMinutes,
                            pct
                    );
                })
                .toList();

        return new AnalyticsDTOs.GoalsVsActualResponse(items);
    }

    // ── Produtividade por dia da semana ──────────────────────────────────────

    @Transactional(readOnly = true)
    public AnalyticsDTOs.ProductivityByWeekdayResponse getProductivityByWeekday(User user) {
        ZoneId userZone = ZoneId.of(user.getTimezone());
        LocalDateTime oneYearAgo = LocalDate.now(userZone).minusYears(1).atStartOfDay();

        List<StudySession> sessions = studySessionRepository.findCompletedInRange(
                user.getId(), oneYearAgo, LocalDateTime.now());

        // Agrupar por dia da semana (ISO: 1=segunda … 7=domingo)
        Map<Integer, List<Integer>> minutesByDow = new TreeMap<>();
        for (int i = 1; i <= 7; i++) minutesByDow.put(i, new ArrayList<>());

        for (StudySession s : sessions) {
            int dow = s.getStartTime()
                    .atZone(ZoneId.of("UTC"))
                    .withZoneSameInstant(userZone)
                    .getDayOfWeek()
                    .getValue(); // 1=MONDAY…7=SUNDAY
            minutesByDow.get(dow).add(s.getDurationMinutes());
        }

        // Mapear nome dos dias em pt-BR
        List<AnalyticsDTOs.WeekdayProductivity> days = new ArrayList<>();
        for (int dow = 1; dow <= 7; dow++) {
            List<Integer> mins = minutesByDow.get(dow);
            int total = mins.stream().mapToInt(Integer::intValue).sum();
            double avg = mins.isEmpty() ? 0.0 : Math.round((total * 10.0 / mins.size())) / 10.0;
            String label = DayOfWeek.of(dow).getDisplayName(TextStyle.FULL, new Locale("pt", "BR"));
            String capitalized = label.substring(0, 1).toUpperCase() + label.substring(1);
            days.add(new AnalyticsDTOs.WeekdayProductivity(dow, capitalized, avg, total, mins.size()));
        }

        return new AnalyticsDTOs.ProductivityByWeekdayResponse(days);
    }

    // ── Melhores horários ─────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public AnalyticsDTOs.BestHoursResponse getBestHours(User user) {
        ZoneId userZone = ZoneId.of(user.getTimezone());
        LocalDateTime oneYearAgo = LocalDate.now(userZone).minusYears(1).atStartOfDay();

        List<StudySession> sessions = studySessionRepository.findCompletedInRange(
                user.getId(), oneYearAgo, LocalDateTime.now());

        // Agrupar por hora de início (no timezone do usuário)
        Map<Integer, List<Integer>> minutesByHour = new TreeMap<>();
        for (int h = 0; h < 24; h++) minutesByHour.put(h, new ArrayList<>());

        for (StudySession s : sessions) {
            int hour = s.getStartTime()
                    .atZone(ZoneId.of("UTC"))
                    .withZoneSameInstant(userZone)
                    .getHour();
            minutesByHour.get(hour).add(s.getDurationMinutes());
        }

        List<AnalyticsDTOs.HourProductivity> hours = new ArrayList<>();
        for (int h = 0; h < 24; h++) {
            List<Integer> mins = minutesByHour.get(h);
            int total = mins.stream().mapToInt(Integer::intValue).sum();
            double avg = mins.isEmpty() ? 0.0 : Math.round((total * 10.0 / mins.size())) / 10.0;
            String label = String.format("%02d:00", h);
            hours.add(new AnalyticsDTOs.HourProductivity(h, label, avg, total, mins.size()));
        }

        return new AnalyticsDTOs.BestHoursResponse(hours);
    }
}
