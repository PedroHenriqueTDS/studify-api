package com.studify.dto.stats;

import java.time.LocalDate;
import java.util.Map;

public class StatsDTOs {

    public record StreakResponse(
            int currentStreak,
            int longestStreak,
            int daysStudiedThisMonth,
            LocalDate lastStudyDate
    ) {}

    public record HeatmapResponse(
            int year,
            int totalMinutes,
            int totalDays,
            Map<String, Integer> data
    ) {}
}
