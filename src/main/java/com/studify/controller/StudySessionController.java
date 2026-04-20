package com.studify.controller;

import com.studify.dto.studysession.StudySessionDTOs;
import com.studify.entity.User;
import com.studify.service.StudySessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/study-sessions")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "⏱m Sessões de Estudo", description = "Registros de sessões de estudo")
public class StudySessionController {

    private final StudySessionService sessionService;

    @PostMapping
    @Operation(summary = "Iniciar/registrar uma sessão de estudo")
    public ResponseEntity<StudySessionDTOs.Response> create(
            @Valid @RequestBody StudySessionDTOs.CreateRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sessionService.create(request, user));
    }

    @GetMapping
    @Operation(summary = "Listar sessões (paginado, filtrar por matéria opcional)")
    public ResponseEntity<Page<StudySessionDTOs.Response>> findAll(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) Long subjectId,
            @PageableDefault(size = 10, sort = "startTime") Pageable pageable) {
        return ResponseEntity.ok(sessionService.findAll(user, subjectId, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar sessão por ID")
    public ResponseEntity<StudySessionDTOs.Response> findById(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(sessionService.findById(id, user));
    }

    @PatchMapping("/{id}/finish")
    @Operation(summary = "Finalizar uma sessão em andamento")
    public ResponseEntity<StudySessionDTOs.Response> finish(
            @PathVariable Long id,
            @Valid @RequestBody StudySessionDTOs.FinishRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(sessionService.finish(id, request, user));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar sessão de estudo")
    public ResponseEntity<StudySessionDTOs.Response> update(
            @PathVariable Long id,
            @Valid @RequestBody StudySessionDTOs.UpdateRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(sessionService.update(id, request, user));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar sessão de estudo")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        sessionService.delete(id, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/summary")
    @Operation(summary = "Resumo geral do tempo estudado")
    public ResponseEntity<StudySessionDTOs.SummaryResponse> getSummary(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(sessionService.getSummary(user));
    }

    @GetMapping("/summary/period")
    @Operation(summary = "Resumo de estudo por período")
    public ResponseEntity<StudySessionDTOs.SummaryResponse> getSummaryByPeriod(
            @AuthenticationPrincipal User user,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(sessionService.getSummaryByPeriod(user, from, to));
    }
}
