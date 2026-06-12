package com.studify.controller;

import com.studify.dto.calendar.CalendarDTOs;
import com.studify.entity.User;
import com.studify.service.CalendarService;
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
@RequestMapping("/api/v1/calendar")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Calendário", description = "Visão unificada: aulas, eventos, tarefas e sessões de estudo")
public class CalendarController {

    private final CalendarService calendarService;

    @GetMapping
    @Operation(summary = "Calendário por período",
               description = "Agrega eventos, aulas recorrentes, prazos de tarefas e sessões de estudo num único response.")
    public ResponseEntity<CalendarDTOs.CalendarResponse> getCalendar(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Data de início (inclusive), formato YYYY-MM-DD", example = "2025-04-07")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @Parameter(description = "Data de fim (inclusive), formato YYYY-MM-DD", example = "2025-04-13")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(calendarService.getCalendar(user, start, end));
    }

    @GetMapping("/week")
    @Operation(summary = "Semana atual",
               description = "Atalho para o calendário da semana corrente (seg–dom), calculada no timezone do usuário.")
    public ResponseEntity<CalendarDTOs.CalendarResponse> getCurrentWeek(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(calendarService.getCurrentWeek(user));
    }
}
