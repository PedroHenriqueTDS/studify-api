package com.studify.service;

import com.studify.dto.goal.GoalDTOs;
import com.studify.entity.Goal;
import com.studify.entity.Subject;
import com.studify.entity.User;
import com.studify.exception.ResourceNotFoundException;
import com.studify.repository.GoalRepository;
import com.studify.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GoalService {

    private final GoalRepository goalRepository;
    private final SubjectRepository subjectRepository;

    @Transactional
    public GoalDTOs.Response create(GoalDTOs.CreateRequest request, User user) {
        Subject subject = null;
        if (request.subjectId() != null) {
            subject = subjectRepository.findByIdAndUserId(request.subjectId(), user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Matéria não encontrada"));
        }

        Goal goal = Goal.builder()
                .title(request.title())
                .description(request.description())
                .subject(subject)
                .user(user)
                .targetHours(request.targetHours())
                .deadline(request.deadline())
                .build();

        return toResponse(goalRepository.save(goal));
    }

    @Transactional(readOnly = true)
    public Page<GoalDTOs.Response> findAll(User user, Goal.GoalStatus status, Pageable pageable) {
        if (status != null) {
            return goalRepository.findByUserIdAndStatus(user.getId(), status, pageable).map(this::toResponse);
        }
        return goalRepository.findByUserId(user.getId(), pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public GoalDTOs.Response findById(Long id, User user) {
        return toResponse(findOrThrow(id, user.getId()));
    }

    @Transactional
    public GoalDTOs.Response update(Long id, GoalDTOs.UpdateRequest request, User user) {
        Goal goal = findOrThrow(id, user.getId());

        if (request.title() != null) goal.setTitle(request.title());
        if (request.description() != null) goal.setDescription(request.description());
        if (request.targetHours() != null) goal.setTargetHours(request.targetHours());
        if (request.deadline() != null) goal.setDeadline(request.deadline());
        if (request.status() != null) goal.setStatus(request.status());
        if (request.subjectId() != null) {
            Subject subject = subjectRepository.findByIdAndUserId(request.subjectId(), user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Matéria não encontrada"));
            goal.setSubject(subject);
        }

        return toResponse(goalRepository.save(goal));
    }

    @Transactional
    public GoalDTOs.Response addProgress(Long id, Double hours, User user) {
        Goal goal = findOrThrow(id, user.getId());
        goal.setCurrentHours(goal.getCurrentHours() + hours);

        if (goal.getCurrentHours() >= goal.getTargetHours()) {
            goal.setStatus(Goal.GoalStatus.COMPLETED);
        }

        return toResponse(goalRepository.save(goal));
    }

    @Transactional
    public void delete(Long id, User user) {
        Goal goal = findOrThrow(id, user.getId());
        goalRepository.delete(goal);
    }

    private Goal findOrThrow(Long id, Long userId) {
        return goalRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Meta não encontrada: " + id));
    }

    private GoalDTOs.Response toResponse(Goal g) {
        return new GoalDTOs.Response(
                g.getId(),
                g.getTitle(),
                g.getDescription(),
                g.getSubject() != null ? g.getSubject().getId() : null,
                g.getSubject() != null ? g.getSubject().getName() : null,
                g.getTargetHours(),
                g.getCurrentHours(),
                g.getProgressPercentage(),
                g.getDeadline(),
                g.getStatus(),
                g.getCreatedAt()
        );
    }
}
