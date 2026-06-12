package com.studify.service;

import com.studify.dto.calendar.CalendarDTOs;
import com.studify.entity.*;
import com.studify.repository.AcademicEventRepository;
import com.studify.repository.RecurringClassRepository;
import com.studify.repository.StudySessionRepository;
import com.studify.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CalendarService {

    private final AcademicEventRepository eventRepository;
    private final RecurringClassRepository classRepository;
    private final TaskRepository taskRepository;
    private final StudySessionRepository sessionRepository;

    /**
     * Endpoint principal: agrega todas as fontes de dados num único response.
     */
    @Transactional(readOnly = true)
    public CalendarDTOs.CalendarResponse getCalendar(User user, LocalDate start, LocalDate end) {
        LocalDateTime startDt = start.atStartOfDay();
        LocalDateTime endDt   = end.plusDays(1).atStartOfDay(); // exclusive upper bound

        List<CalendarDTOs.CalendarItem> items = new ArrayList<>();

        items.addAll(mapAcademicEvents(user.getId(), startDt, endDt));
        items.addAll(generateClassOccurrences(user.getId(), start, end));
        items.addAll(mapTasksDue(user.getId(), start, end));
        items.addAll(mapStudySessions(user.getId(), startDt, endDt));

        items.sort(Comparator.comparing(CalendarDTOs.CalendarItem::startDateTime,
                Comparator.nullsLast(Comparator.naturalOrder())));

        return new CalendarDTOs.CalendarResponse(start, end, items);
    }

    /**
     * Atalho: semana atual (seg–dom) no timezone do usuário.
     */
    @Transactional(readOnly = true)
    public CalendarDTOs.CalendarResponse getCurrentWeek(User user) {
        ZoneId zone = resolveZone(user.getTimezone());
        LocalDate today = LocalDate.now(zone);
        LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate sunday = monday.plusDays(6);
        return getCalendar(user, monday, sunday);
    }

    // ── Mapeadores por fonte ─────────────────────────────────────────────────

    private List<CalendarDTOs.CalendarItem> mapAcademicEvents(Long userId,
                                                               LocalDateTime start,
                                                               LocalDateTime end) {
        return eventRepository.findByPeriod(userId, start, end).stream()
                .map(e -> new CalendarDTOs.CalendarItem(
                        "event-" + e.getId(),
                        e.getId(),
                        CalendarDTOs.SourceType.ACADEMIC_EVENT,
                        e.getTitle(),
                        e.getType().name(),
                        e.getStartDateTime(),
                        e.getEndDateTime(),
                        false,
                        e.getLocation(),
                        e.getSubject() != null ? e.getSubject().getId() : null,
                        e.getSubject() != null ? e.getSubject().getName() : null,
                        e.getSubject() != null ? e.getSubject().getColor() : null,
                        null,
                        null,
                        Map.of()
                ))
                .toList();
    }

    /**
     * Geração on-the-fly das ocorrências de aulas recorrentes.
     * Itera dia a dia no intervalo e verifica se há aula ativa naquele dia da semana.
     */
    private List<CalendarDTOs.CalendarItem> generateClassOccurrences(Long userId,
                                                                       LocalDate start,
                                                                       LocalDate end) {
        List<RecurringClass> classes = classRepository.findActiveInPeriod(userId, start, end);
        List<CalendarDTOs.CalendarItem> occurrences = new ArrayList<>();

        for (LocalDate day = start; !day.isAfter(end); day = day.plusDays(1)) {
            final LocalDate currentDay = day;
            for (RecurringClass rc : classes) {
                if (rc.getDayOfWeek() == currentDay.getDayOfWeek()
                        && !currentDay.isBefore(rc.getStartDate())
                        && !currentDay.isAfter(rc.getEndDate())) {

                    LocalDateTime occurrenceStart = currentDay.atTime(rc.getStartTime());
                    LocalDateTime occurrenceEnd   = currentDay.atTime(rc.getEndTime());

                    occurrences.add(new CalendarDTOs.CalendarItem(
                            "class-" + rc.getId() + "-" + currentDay,
                            rc.getId(),
                            CalendarDTOs.SourceType.RECURRING_CLASS,
                            rc.getTitle(),
                            "CLASS",
                            occurrenceStart,
                            occurrenceEnd,
                            false,
                            rc.getLocation(),
                            rc.getSubject() != null ? rc.getSubject().getId() : null,
                            rc.getSubject() != null ? rc.getSubject().getName() : null,
                            rc.getSubject() != null ? rc.getSubject().getColor() : null,
                            null,
                            null,
                            Map.of()
                    ));
                }
            }
        }
        return occurrences;
    }

    private List<CalendarDTOs.CalendarItem> mapTasksDue(Long userId,
                                                          LocalDate start,
                                                          LocalDate end) {
        return taskRepository.findPendingByDueDateRange(userId, start, end).stream()
                .map(t -> new CalendarDTOs.CalendarItem(
                        "task-" + t.getId(),
                        t.getId(),
                        CalendarDTOs.SourceType.TASK,
                        t.getTitle(),
                        "TASK_DUE",
                        t.getDueDate().atStartOfDay(),
                        null,
                        true,
                        null,
                        t.getSubject() != null ? t.getSubject().getId() : null,
                        t.getSubject() != null ? t.getSubject().getName() : null,
                        t.getSubject() != null ? t.getSubject().getColor() : null,
                        t.getPriority(),
                        t.getStatus().name(),
                        Map.of()
                ))
                .toList();
    }

    private List<CalendarDTOs.CalendarItem> mapStudySessions(Long userId,
                                                               LocalDateTime start,
                                                               LocalDateTime end) {
        return sessionRepository.findByPeriod(userId, start, end).stream()
                .map(s -> {
                    String title = s.getSubject() != null
                            ? "Sessão de estudo — " + s.getSubject().getName()
                            : "Sessão de estudo";
                    return new CalendarDTOs.CalendarItem(
                            "session-" + s.getId(),
                            s.getId(),
                            CalendarDTOs.SourceType.STUDY_SESSION,
                            title,
                            "STUDY_SESSION",
                            s.getStartTime(),
                            s.getEndTime(),
                            false,
                            null,
                            s.getSubject() != null ? s.getSubject().getId() : null,
                            s.getSubject() != null ? s.getSubject().getName() : null,
                            s.getSubject() != null ? s.getSubject().getColor() : null,
                            null,
                            s.getStatus().name(),
                            Map.of()
                    );
                })
                .toList();
    }

    // ── Utilitário ───────────────────────────────────────────────────────────

    private ZoneId resolveZone(String timezone) {
        try {
            return ZoneId.of(timezone);
        } catch (DateTimeException e) {
            return ZoneId.of("America/Sao_Paulo");
        }
    }
}
