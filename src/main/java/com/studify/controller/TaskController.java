package com.studify.controller;

import com.studify.dto.task.TaskDTOs;
import com.studify.entity.Task;
import com.studify.entity.User;
import com.studify.service.TaskService;
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
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = " Tarefas", description = "Gerenciamento de tarefas de estudo")
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    @Operation(summary = "Criar nova tarefa")
    public ResponseEntity<TaskDTOs.Response> create(
            @Valid @RequestBody TaskDTOs.CreateRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.create(request, user));
    }

    @GetMapping
    @Operation(summary = "Listar tarefas (filtros: status, subjectId)")
    public ResponseEntity<Page<TaskDTOs.Response>> findAll(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) Task.TaskStatus status,
            @RequestParam(required = false) Long subjectId,
            @PageableDefault(size = 10, sort = "dueDate") Pageable pageable) {
        return ResponseEntity.ok(taskService.findAll(user, status, subjectId, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar tarefa por ID")
    public ResponseEntity<TaskDTOs.Response> findById(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(taskService.findById(id, user));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar tarefa")
    public ResponseEntity<TaskDTOs.Response> update(
            @PathVariable Long id,
            @Valid @RequestBody TaskDTOs.UpdateRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(taskService.update(id, request, user));
    }

    @PatchMapping("/{id}/complete")
    @Operation(summary = "Marcar tarefa como concluída")
    public ResponseEntity<TaskDTOs.Response> complete(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(taskService.complete(id, user));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar tarefa")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        taskService.delete(id, user);
        return ResponseEntity.noContent().build();
    }
}
