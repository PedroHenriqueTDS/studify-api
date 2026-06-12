package com.studify.controller;

import com.studify.dto.report.ReportDTOs;
import com.studify.entity.User;
import com.studify.service.ReportService;
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
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Relatórios", description = "Relatórios semanais com resumo de estudo, tarefas, faltas, metas e streak")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/weekly")
    @Operation(
            summary = "Relatório semanal por data",
            description = "Retorna o relatório completo da semana (seg–dom) que contém a data informada."
    )
    public ResponseEntity<ReportDTOs.WeeklyReport> getWeeklyReport(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Qualquer data dentro da semana desejada (YYYY-MM-DD)", example = "2025-04-10")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(reportService.getWeeklyReport(user, date));
    }

    @GetMapping("/weekly/current")
    @Operation(
            summary = "Relatório da semana atual",
            description = "Atalho para o relatório da semana corrente calculada no timezone do usuário."
    )
    public ResponseEntity<ReportDTOs.WeeklyReport> getCurrentWeekReport(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(reportService.getCurrentWeekReport(user));
    }
}
