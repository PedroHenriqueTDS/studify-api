package com.studify.controller;

import com.studify.dto.gradegoal.GradeGoalDTOs;
import com.studify.entity.User;
import com.studify.service.GradeGoalService;
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
@RequestMapping("/api/v1/grade-goals")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Metas de Nota", description = "Metas de nota por matéria com cálculo de média ponderada e nota necessária")
public class GradeGoalController {

    private final GradeGoalService gradeGoalService;

    // ─── GradeGoal ────────────────────────────────────────────────────────────

    @PostMapping
    @Operation(summary = "Criar meta de nota para uma matéria")
    public ResponseEntity<GradeGoalDTOs.GoalResponse> create(
            @Valid @RequestBody GradeGoalDTOs.CreateRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(gradeGoalService.create(request, user));
    }

    @GetMapping
    @Operation(summary = "Listar metas de nota (paginado)")
    public ResponseEntity<Page<GradeGoalDTOs.GoalResponse>> findAll(
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(gradeGoalService.findAll(user, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar meta de nota por ID")
    public ResponseEntity<GradeGoalDTOs.GoalResponse> findById(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(gradeGoalService.findById(id, user));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar meta de nota")
    public ResponseEntity<GradeGoalDTOs.GoalResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody GradeGoalDTOs.UpdateRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(gradeGoalService.update(id, request, user));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar (soft delete) meta de nota")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        gradeGoalService.delete(id, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/summary")
    @Operation(summary = "Resumo: média atual, nota necessária, status (ON_TRACK / AT_RISK / FAILED / ACHIEVED)")
    public ResponseEntity<GradeGoalDTOs.SummaryResponse> getSummary(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(gradeGoalService.getSummary(id, user));
    }

    // ─── GradeRecord ──────────────────────────────────────────────────────────

    @PostMapping("/{id}/records")
    @Operation(summary = "Adicionar nota parcial à meta")
    public ResponseEntity<GradeGoalDTOs.RecordResponse> addRecord(
            @PathVariable Long id,
            @Valid @RequestBody GradeGoalDTOs.CreateRecordRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(gradeGoalService.addRecord(id, request, user));
    }

    @GetMapping("/{id}/records")
    @Operation(summary = "Listar notas parciais da meta (paginado)")
    public ResponseEntity<Page<GradeGoalDTOs.RecordResponse>> findRecords(
            @PathVariable Long id,
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(gradeGoalService.findRecords(id, user, pageable));
    }

    @PutMapping("/{id}/records/{recordId}")
    @Operation(summary = "Atualizar nota parcial")
    public ResponseEntity<GradeGoalDTOs.RecordResponse> updateRecord(
            @PathVariable Long id,
            @PathVariable Long recordId,
            @Valid @RequestBody GradeGoalDTOs.UpdateRecordRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(gradeGoalService.updateRecord(id, recordId, request, user));
    }

    @DeleteMapping("/{id}/records/{recordId}")
    @Operation(summary = "Deletar (soft delete) nota parcial")
    public ResponseEntity<Void> deleteRecord(
            @PathVariable Long id,
            @PathVariable Long recordId,
            @AuthenticationPrincipal User user) {
        gradeGoalService.deleteRecord(id, recordId, user);
        return ResponseEntity.noContent().build();
    }
}
