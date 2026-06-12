package com.studify.controller;

import com.studify.dto.recurringclass.RecurringClassDTOs;
import com.studify.entity.User;
import com.studify.service.RecurringClassService;
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
@RequestMapping("/api/v1/recurring-classes")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Aulas Recorrentes", description = "Grade semanal fixa de aulas")
public class RecurringClassController {

    private final RecurringClassService classService;

    @PostMapping
    @Operation(summary = "Criar aula recorrente")
    public ResponseEntity<RecurringClassDTOs.Response> create(
            @Valid @RequestBody RecurringClassDTOs.CreateRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(classService.create(request, user));
    }

    @GetMapping
    @Operation(summary = "Listar aulas recorrentes")
    public ResponseEntity<Page<RecurringClassDTOs.Response>> findAll(
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 20, sort = "dayOfWeek") Pageable pageable) {
        return ResponseEntity.ok(classService.findAll(user, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar aula recorrente por ID")
    public ResponseEntity<RecurringClassDTOs.Response> findById(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(classService.findById(id, user));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar aula recorrente")
    public ResponseEntity<RecurringClassDTOs.Response> update(
            @PathVariable Long id,
            @Valid @RequestBody RecurringClassDTOs.UpdateRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(classService.update(id, request, user));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover aula recorrente (soft delete)")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        classService.delete(id, user);
        return ResponseEntity.noContent().build();
    }
}
