package com.studify.service;

import com.studify.dto.subject.SubjectDTOs;
import com.studify.entity.Subject;
import com.studify.entity.User;
import com.studify.exception.BusinessException;
import com.studify.exception.ResourceNotFoundException;
import com.studify.repository.StudySessionRepository;
import com.studify.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final StudySessionRepository studySessionRepository;

    @Transactional
    public SubjectDTOs.Response create(SubjectDTOs.CreateRequest request, User user) {
        if (subjectRepository.existsByNameAndUserId(request.name(), user.getId())) {
            throw new BusinessException("Você já possui uma matéria com esse nome");
        }

        Subject subject = Subject.builder()
                .name(request.name())
                .description(request.description())
                .color(request.color() != null ? request.color() : "#6366F1")
                .user(user)
                .build();

        return toResponse(subjectRepository.save(subject), user.getId());
    }

    @Transactional(readOnly = true)
    public Page<SubjectDTOs.Response> findAll(User user, Pageable pageable) {
        return subjectRepository.findByUserId(user.getId(), pageable)
                .map(s -> toResponse(s, user.getId()));
    }

    @Transactional(readOnly = true)
    public SubjectDTOs.Response findById(Long id, User user) {
        Subject subject = findSubjectOrThrow(id, user.getId());
        return toResponse(subject, user.getId());
    }

    @Transactional
    public SubjectDTOs.Response update(Long id, SubjectDTOs.UpdateRequest request, User user) {
        Subject subject = findSubjectOrThrow(id, user.getId());

        if (request.name() != null) subject.setName(request.name());
        if (request.description() != null) subject.setDescription(request.description());
        if (request.color() != null) subject.setColor(request.color());

        return toResponse(subjectRepository.save(subject), user.getId());
    }

    @Transactional
    public void delete(Long id, User user) {
        Subject subject = findSubjectOrThrow(id, user.getId());
        subjectRepository.delete(subject);
    }

    private Subject findSubjectOrThrow(Long id, Long userId) {
        return subjectRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Matéria não encontrada: " + id));
    }

    private SubjectDTOs.Response toResponse(Subject subject, Long userId) {
        Integer totalMinutes = studySessionRepository.sumDurationByUserIdAndSubjectId(userId, subject.getId());
        Long totalSessions = studySessionRepository.countByUserId(userId);

        return new SubjectDTOs.Response(
                subject.getId(),
                subject.getName(),
                subject.getDescription(),
                subject.getColor(),
                totalMinutes,
                totalSessions,
                subject.getCreatedAt()
        );
    }
}
