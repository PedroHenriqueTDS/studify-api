package com.studify.controller;

import com.studify.dto.stats.StatsDTOs;
import com.studify.entity.User;
import com.studify.service.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;

@RestController
@RequestMapping("/api/v1/stats")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Estatísticas de Atividade", description = "Streak de dias consecutivos e heatmap de atividade de estudo")
public class StatsController {

    private final StatsService statsService;

    @GetMapping("/streak")
    @Operation(
            summary = "Streak atual e histórico",
            description = "Retorna o streak atual (dias consecutivos até hoje/ontem), o maior streak histórico e os dias estudados no mês corrente."
    )
    public ResponseEntity<StatsDTOs.StreakResponse> getStreak(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(statsService.getStreak(user));
    }

    @GetMapping("/heatmap")
    @Operation(
            summary = "Heatmap de atividade anual",
            description = "Mapa de calor com o total de minutos estudados por dia no ano informado. Apenas dias com atividade aparecem no campo 'data'."
    )
    public ResponseEntity<StatsDTOs.HeatmapResponse> getHeatmap(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Ano para o heatmap (ex: 2025). Default: ano corrente.", example = "2025")
            @RequestParam(required = false) Integer year) {
        int targetYear = (year != null) ? year : LocalDate.now(ZoneId.of(user.getTimezone())).getYear();
        return ResponseEntity.ok(statsService.getHeatmap(user, targetYear));
    }
}
