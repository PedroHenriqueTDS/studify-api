package com.studify.service;

import com.studify.dto.stats.StatsDTOs;
import com.studify.entity.StudySession;
import com.studify.entity.User;
import com.studify.repository.StudySessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final StudySessionRepository studySessionRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Transactional(readOnly = true)
    public StatsDTOs.StreakResponse getStreak(User user) {
        ZoneId userZone = ZoneId.of(user.getTimezone());
        LocalDate today = LocalDate.now(userZone);

        List<StudySession> sessions = studySessionRepository.findAllCompletedByUserId(user.getId());

        // Coletar dias únicos com sessão concluída, convertidos para o timezone do usuário
        Set<LocalDate> studyDays = sessions.stream()
                .map(s -> s.getStartTime().atZone(ZoneId.of("UTC")).withZoneSameInstant(userZone).toLocalDate())
                .collect(Collectors.toCollection(TreeSet::new));

        if (studyDays.isEmpty()) {
            return new StatsDTOs.StreakResponse(0, 0, 0, null);
        }

        LocalDate lastStudyDate = ((TreeSet<LocalDate>) studyDays).last();

        // Streak atual: começa do dia mais recente com sessão e conta para trás
        int currentStreak = 0;
        // O streak só é válido se a última sessão foi hoje ou ontem
        if (!lastStudyDate.isBefore(today.minusDays(1))) {
            LocalDate cursor = lastStudyDate;
            while (studyDays.contains(cursor)) {
                currentStreak++;
                cursor = cursor.minusDays(1);
            }
        }

        // Streak histórico: encontrar a maior sequência consecutiva em todos os dias
        int longestStreak = 0;
        int runningStreak = 0;
        LocalDate previous = null;

        for (LocalDate day : studyDays) {
            if (previous == null || day.equals(previous.plusDays(1))) {
                runningStreak++;
            } else {
                runningStreak = 1;
            }
            longestStreak = Math.max(longestStreak, runningStreak);
            previous = day;
        }

        // Dias estudados no mês corrente
        int daysStudiedThisMonth = (int) studyDays.stream()
                .filter(d -> d.getMonth() == today.getMonth() && d.getYear() == today.getYear())
                .count();

        return new StatsDTOs.StreakResponse(
                currentStreak,
                longestStreak,
                daysStudiedThisMonth,
                lastStudyDate
        );
    }

    @Transactional(readOnly = true)
    public StatsDTOs.HeatmapResponse getHeatmap(User user, int year) {
        ZoneId userZone = ZoneId.of(user.getTimezone());

        // Intervalo em UTC para cobrir o ano inteiro no timezone do usuário
        // Margem de 1 dia extra em cada lado para garantir cobertura em qualquer UTC offset
        LocalDateTime startOfYear = LocalDate.of(year, 1, 1).atStartOfDay().minusDays(1);
        LocalDateTime startOfNextYear = LocalDate.of(year + 1, 1, 1).atStartOfDay().plusDays(1);

        List<StudySession> sessions = studySessionRepository.findCompletedByYear(
                user.getId(), startOfYear, startOfNextYear);

        // Agrupar e somar minutos por dia no timezone do usuário, filtrando apenas o ano solicitado
        Map<String, Integer> data = new TreeMap<>();
        for (StudySession session : sessions) {
            LocalDate day = session.getStartTime()
                    .atZone(ZoneId.of("UTC"))
                    .withZoneSameInstant(userZone)
                    .toLocalDate();

            // Filtrar estritamente pelo ano solicitado (a margem pode trazer dias de anos adjacentes)
            if (day.getYear() != year) {
                continue;
            }

            String key = day.format(DATE_FORMATTER);
            data.merge(key, session.getDurationMinutes(), Integer::sum);
        }

        int totalMinutes = data.values().stream().mapToInt(Integer::intValue).sum();
        int totalDays = data.size();

        return new StatsDTOs.HeatmapResponse(year, totalMinutes, totalDays, data);
    }
}
