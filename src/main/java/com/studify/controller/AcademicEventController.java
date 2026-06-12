package com.studify.controller;

import com.studify.dto.academicevent.AcademicEventDTOs;
import com.studify.entity.User;
import com.studify.service.AcademicEventService;
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
@RequestMapping("/api/v1/academic-events")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Eventos Acadêmicos", description = "Provas, apresentações e entregas únicas")
public class AcademicEventController {

    private final AcademicEventService eventService;

    @PostMapping
    @Operation(summary = "Criar evento acadêmico")
    public ResponseEntity<AcademicEventDTOs.Response> create(
            @Valid @RequestBody AcademicEventDTOs.CreateRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.create(request, user));
    }

    @GetMapping
    @Operation(summary = "Listar eventos acadêmicos")
    public ResponseEntity<Page<AcademicEventDTOs.Response>> findAll(
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 20, sort = "startDateTime") Pageable pageable) {
        return ResponseEntity.ok(eventService.findAll(user, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar evento por ID")
    public ResponseEntity<AcademicEventDTOs.Response> findById(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(eventService.findById(id, user));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar evento acadêmico")
    public ResponseEntity<AcademicEventDTOs.Response> update(
            @PathVariable Long id,
            @Valid @RequestBody AcademicEventDTOs.UpdateRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(eventService.update(id, request, user));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover evento acadêmico (soft delete)")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        eventService.delete(id, user);
        return ResponseEntity.noContent().build();
    }
}
