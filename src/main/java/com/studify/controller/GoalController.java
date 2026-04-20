package com.studify.controller;

import com.studify.dto.goal.GoalDTOs;
import com.studify.entity.Goal;
import com.studify.entity.User;
import com.studify.service.GoalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/goals")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = " Metas", description = "Gerenciamento de metas de estudo")
public class GoalController {

    private final GoalService goalService;

    @PostMapping
    @Operation(summary = "Criar nova meta de estudo")
    public ResponseEntity<GoalDTOs.Response> create(
            @Valid @RequestBody GoalDTOs.CreateRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(goalService.create(request, user));
    }

    @GetMapping
    @Operation(summary = "Listar metas (filtrar por status opcional)")
    public ResponseEntity<Page<GoalDTOs.Response>> findAll(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) Goal.GoalStatus status,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(goalService.findAll(user, status, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar meta por ID")
    public ResponseEntity<GoalDTOs.Response> findById(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(goalService.findById(id, user));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar meta")
    public ResponseEntity<GoalDTOs.Response> update(
            @PathVariable Long id,
            @Valid @RequestBody GoalDTOs.UpdateRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(goalService.update(id, request, user));
    }

    @PatchMapping("/{id}/progress")
    @Operation(summary = "Adicionar progresso a uma meta (em horas)")
    public ResponseEntity<GoalDTOs.Response> addProgress(
            @PathVariable Long id,
            @RequestParam @Positive(message = "Horas devem ser positivas") Double hours,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(goalService.addProgress(id, hours, user));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar meta")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        goalService.delete(id, user);
        return ResponseEntity.noContent().build();
    }
}
