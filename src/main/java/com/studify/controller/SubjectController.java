package com.studify.controller;

import com.studify.dto.subject.SubjectDTOs;
import com.studify.entity.User;
import com.studify.service.SubjectService;
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
@RequestMapping("/api/v1/subjects")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = " Matérias", description = "Gerenciamento de disciplinas/matérias")
public class SubjectController {

    private final SubjectService subjectService;

    @PostMapping
    @Operation(summary = "Criar nova matéria")
    public ResponseEntity<SubjectDTOs.Response> create(
            @Valid @RequestBody SubjectDTOs.CreateRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(subjectService.create(request, user));
    }

    @GetMapping
    @Operation(summary = "Listar todas as matérias do usuário (paginado)")
    public ResponseEntity<Page<SubjectDTOs.Response>> findAll(
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 10, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(subjectService.findAll(user, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar matéria por ID")
    public ResponseEntity<SubjectDTOs.Response> findById(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(subjectService.findById(id, user));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar matéria")
    public ResponseEntity<SubjectDTOs.Response> update(
            @PathVariable Long id,
            @Valid @RequestBody SubjectDTOs.UpdateRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(subjectService.update(id, request, user));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar matéria")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        subjectService.delete(id, user);
        return ResponseEntity.noContent().build();
    }
}
