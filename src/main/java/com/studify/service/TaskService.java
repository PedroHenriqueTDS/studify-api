package com.studify.service;

import com.studify.dto.task.TaskDTOs;
import com.studify.entity.Subject;
import com.studify.entity.Task;
import com.studify.entity.User;
import com.studify.exception.ResourceNotFoundException;
import com.studify.repository.SubjectRepository;
import com.studify.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final SubjectRepository subjectRepository;

    @Transactional
    public TaskDTOs.Response create(TaskDTOs.CreateRequest request, User user) {
        Subject subject = null;
        if (request.subjectId() != null) {
            subject = subjectRepository.findByIdAndUserId(request.subjectId(), user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Matéria não encontrada"));
        }

        Task task = Task.builder()
                .title(request.title())
                .description(request.description())
                .subject(subject)
                .user(user)
                .priority(request.priority() != null ? request.priority() : Task.Priority.MEDIUM)
                .dueDate(request.dueDate())
                .build();

        return toResponse(taskRepository.save(task));
    }

    @Transactional(readOnly = true)
    public Page<TaskDTOs.Response> findAll(User user, Task.TaskStatus status, Long subjectId, Pageable pageable) {
        if (subjectId != null) {
            return taskRepository.findByUserIdAndSubjectId(user.getId(), subjectId, pageable).map(this::toResponse);
        }
        if (status != null) {
            return taskRepository.findByUserIdAndStatus(user.getId(), status, pageable).map(this::toResponse);
        }
        return taskRepository.findByUserId(user.getId(), pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public TaskDTOs.Response findById(Long id, User user) {
        return toResponse(findOrThrow(id, user.getId()));
    }

    @Transactional
    public TaskDTOs.Response update(Long id, TaskDTOs.UpdateRequest request, User user) {
        Task task = findOrThrow(id, user.getId());

        if (request.title() != null) task.setTitle(request.title());
        if (request.description() != null) task.setDescription(request.description());
        if (request.priority() != null) task.setPriority(request.priority());
        if (request.dueDate() != null) task.setDueDate(request.dueDate());
        if (request.status() != null) {
            task.setStatus(request.status());
            if (request.status() == Task.TaskStatus.COMPLETED && task.getCompletedAt() == null) {
                task.setCompletedAt(LocalDateTime.now());
            }
        }
        if (request.subjectId() != null) {
            Subject subject = subjectRepository.findByIdAndUserId(request.subjectId(), user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Matéria não encontrada"));
            task.setSubject(subject);
        }

        return toResponse(taskRepository.save(task));
    }

    @Transactional
    public TaskDTOs.Response complete(Long id, User user) {
        Task task = findOrThrow(id, user.getId());
        task.setStatus(Task.TaskStatus.COMPLETED);
        task.setCompletedAt(LocalDateTime.now());
        return toResponse(taskRepository.save(task));
    }

    @Transactional
    public void delete(Long id, User user) {
        Task task = findOrThrow(id, user.getId());
        taskRepository.delete(task);
    }

    private Task findOrThrow(Long id, Long userId) {
        return taskRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa não encontrada: " + id));
    }

    private TaskDTOs.Response toResponse(Task t) {
        return new TaskDTOs.Response(
                t.getId(),
                t.getTitle(),
                t.getDescription(),
                t.getSubject() != null ? t.getSubject().getId() : null,
                t.getSubject() != null ? t.getSubject().getName() : null,
                t.getPriority(),
                t.getStatus(),
                t.getDueDate(),
                t.getCompletedAt(),
                t.getCreatedAt()
        );
    }
}
