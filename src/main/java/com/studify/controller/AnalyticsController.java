package com.studify.controller;

import com.studify.dto.analytics.AnalyticsDTOs;
import com.studify.entity.User;
import com.studify.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Analytics", description = "Dados agregados para gráficos do dashboard")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/hours-by-subject")
    @Operation(summary = "Minutos estudados por matéria no período (máx. 365 dias)")
    public ResponseEntity<AnalyticsDTOs.HoursBySubjectResponse> getHoursBySubject(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Data inicial (YYYY-MM-DD)", example = "2025-01-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @Parameter(description = "Data final (YYYY-MM-DD)", example = "2025-12-31")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(analyticsService.getHoursBySubject(user, start, end));
    }

    @GetMapping("/weekly-evolution")
    @Operation(summary = "Evolução de minutos estudados por semana (máx. 52 semanas)")
    public ResponseEntity<AnalyticsDTOs.WeeklyEvolutionResponse> getWeeklyEvolution(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Número de semanas para retornar (1–52, default: 12)", example = "12")
            @RequestParam(defaultValue = "12") int weeks) {
        return ResponseEntity.ok(analyticsService.getWeeklyEvolution(user, weeks));
    }

    @GetMapping("/goals-vs-actual")
    @Operation(summary = "Meta de horas planejada vs. horas reais por matéria")
    public ResponseEntity<AnalyticsDTOs.GoalsVsActualResponse> getGoalsVsActual(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(analyticsService.getGoalsVsActual(user));
    }

    @GetMapping("/productivity-by-weekday")
    @Operation(summary = "Média de minutos estudados por dia da semana (últimos 12 meses)")
    public ResponseEntity<AnalyticsDTOs.ProductivityByWeekdayResponse> getProductivityByWeekday(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(analyticsService.getProductivityByWeekday(user));
    }

    @GetMapping("/best-hours")
    @Operation(summary = "Horários mais produtivos por hora do dia (últimos 12 meses)")
    public ResponseEntity<AnalyticsDTOs.BestHoursResponse> getBestHours(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(analyticsService.getBestHours(user));
    }
}
