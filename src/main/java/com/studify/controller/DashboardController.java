package com.studify.controller;

import com.studify.entity.Goal;
import com.studify.entity.Task;
import com.studify.entity.User;
import com.studify.repository.GoalRepository;
import com.studify.repository.StudySessionRepository;
import com.studify.repository.SubjectRepository;
import com.studify.repository.TaskRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = " Dashboard", description = "Estatísticas e resumo geral do usuário")
public class DashboardController {

    private final SubjectRepository subjectRepository;
    private final StudySessionRepository sessionRepository;
    private final GoalRepository goalRepository;
    private final TaskRepository taskRepository;

    @GetMapping
    @Operation(summary = "Obter resumo geral do usuário")
    public ResponseEntity<Map<String, Object>> getDashboard(@AuthenticationPrincipal User user) {
        Long userId = user.getId();

        int totalMinutes = sessionRepository.sumDurationByUserId(userId);

        Map<String, Object> dashboard = Map.of(
                "user", Map.of("name", user.getName(), "email", user.getEmail()),
                "subjects", Map.of(
                        "total", subjectRepository.findByUserId(userId).size()
                ),
                "studySessions", Map.of(
                        "totalSessions", sessionRepository.countByUserId(userId),
                        "totalMinutes", totalMinutes,
                        "totalHours", totalMinutes / 60
                ),
                "goals", Map.of(
                        "inProgress", goalRepository.countByUserIdAndStatus(userId, Goal.GoalStatus.IN_PROGRESS),
                        "completed", goalRepository.countByUserIdAndStatus(userId, Goal.GoalStatus.COMPLETED)
                ),
                "tasks", Map.of(
                        "pending", taskRepository.countByUserIdAndStatus(userId, Task.TaskStatus.PENDING),
                        "inProgress", taskRepository.countByUserIdAndStatus(userId, Task.TaskStatus.IN_PROGRESS),
                        "completed", taskRepository.countByUserIdAndStatus(userId, Task.TaskStatus.COMPLETED)
                )
        );

        return ResponseEntity.ok(dashboard);
    }
}
