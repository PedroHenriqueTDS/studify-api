package com.studify.controller;

import com.studify.dto.attendance.AttendanceDTOs;
import com.studify.entity.User;
import com.studify.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Controle de Faltas", description = "Endpoints para registro de presença, faltas e controle de limite de faltas")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping
    @Operation(summary = "Registrar presença ou falta para uma aula recorrente")
    public ResponseEntity<AttendanceDTOs.Response> createOrUpdate(
            @Valid @RequestBody AttendanceDTOs.CreateRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(attendanceService.createOrUpdate(request, user));
    }

    @GetMapping("/subject/{subjectId}")
    @Operation(summary = "Listar registros de presença/falta de uma matéria específica")
    public ResponseEntity<Page<AttendanceDTOs.Response>> findBySubjectId(
            @PathVariable Long subjectId,
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 20, sort = "date") Pageable pageable) {
        return ResponseEntity.ok(attendanceService.findBySubjectId(subjectId, user, pageable));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar (soft-delete) um registro de presença")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        attendanceService.delete(id, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/summary")
    @Operation(summary = "Obter resumo global e por matéria do controle de faltas")
    public ResponseEntity<AttendanceDTOs.GlobalAttendanceSummary> getSummary(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(attendanceService.getSummary(user));
    }
}
