package com.studify.controller;

import com.studify.dto.sessionnote.SessionNoteDTOs;
import com.studify.entity.User;
import com.studify.service.SessionNoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Anotações", description = "Anotações Markdown vinculadas a sessões de estudo, com busca full-text")
public class SessionNoteController {

    private final SessionNoteService sessionNoteService;

    @PostMapping("/api/v1/study-sessions/{sessionId}/notes")
    @Operation(summary = "Criar anotação em uma sessão de estudo")
    public ResponseEntity<SessionNoteDTOs.Response> create(
            @PathVariable Long sessionId,
            @Valid @RequestBody SessionNoteDTOs.CreateRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(sessionNoteService.create(sessionId, request, user));
    }

    @GetMapping("/api/v1/study-sessions/{sessionId}/notes")
    @Operation(summary = "Listar anotações de uma sessão (paginado)")
    public ResponseEntity<Page<SessionNoteDTOs.Response>> findBySession(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(sessionNoteService.findBySession(sessionId, user, pageable));
    }

    @PutMapping("/api/v1/study-sessions/{sessionId}/notes/{noteId}")
    @Operation(summary = "Atualizar anotação")
    public ResponseEntity<SessionNoteDTOs.Response> update(
            @PathVariable Long sessionId,
            @PathVariable Long noteId,
            @Valid @RequestBody SessionNoteDTOs.UpdateRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(sessionNoteService.update(sessionId, noteId, request, user));
    }

    @DeleteMapping("/api/v1/study-sessions/{sessionId}/notes/{noteId}")
    @Operation(summary = "Deletar (soft delete) anotação")
    public ResponseEntity<Void> delete(
            @PathVariable Long sessionId,
            @PathVariable Long noteId,
            @AuthenticationPrincipal User user) {
        sessionNoteService.delete(sessionId, noteId, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/v1/notes/search")
    @Operation(
            summary = "Buscar anotações por termo (LIKE full-text)",
            description = "Pesquisa nas anotações do usuário autenticado. Busca case-insensitive por substring no conteúdo."
    )
    public ResponseEntity<Page<SessionNoteDTOs.Response>> search(
            @Parameter(description = "Termo de busca", example = "derivada") @RequestParam String q,
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(sessionNoteService.search(q, user, pageable));
    }
}
