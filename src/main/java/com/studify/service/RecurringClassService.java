package com.studify.service;

import com.studify.dto.recurringclass.RecurringClassDTOs;
import com.studify.entity.RecurringClass;
import com.studify.entity.Subject;
import com.studify.entity.User;
import com.studify.exception.ResourceNotFoundException;
import com.studify.repository.RecurringClassRepository;
import com.studify.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RecurringClassService {

    private final RecurringClassRepository classRepository;
    private final SubjectRepository subjectRepository;

    @Transactional
    public RecurringClassDTOs.Response create(RecurringClassDTOs.CreateRequest request, User user) {
        if (request.endDate().isBefore(request.startDate())) {
            throw new IllegalArgumentException("Data de fim deve ser posterior à data de início");
        }
        if (request.endTime().isBefore(request.startTime()) || request.endTime().equals(request.startTime())) {
            throw new IllegalArgumentException("Horário de fim deve ser posterior ao horário de início");
        }

        Subject subject = resolveSubject(request.subjectId(), user.getId());

        RecurringClass rc = RecurringClass.builder()
                .title(request.title())
                .dayOfWeek(request.dayOfWeek())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .location(request.location())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .subject(subject)
                .user(user)
                .build();

        return toResponse(classRepository.save(rc));
    }

    @Transactional(readOnly = true)
    public Page<RecurringClassDTOs.Response> findAll(User user, Pageable pageable) {
        return classRepository.findByUserIdAndDeletedAtIsNull(user.getId(), pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public RecurringClassDTOs.Response findById(Long id, User user) {
        return toResponse(findOrThrow(id, user.getId()));
    }

    @Transactional
    public RecurringClassDTOs.Response update(Long id, RecurringClassDTOs.UpdateRequest request, User user) {
        RecurringClass rc = findOrThrow(id, user.getId());

        if (request.title() != null)     rc.setTitle(request.title());
        if (request.dayOfWeek() != null) rc.setDayOfWeek(request.dayOfWeek());
        if (request.startTime() != null) rc.setStartTime(request.startTime());
        if (request.endTime() != null)   rc.setEndTime(request.endTime());
        if (request.location() != null)  rc.setLocation(request.location());
        if (request.startDate() != null) rc.setStartDate(request.startDate());
        if (request.endDate() != null)   rc.setEndDate(request.endDate());
        if (request.active() != null)    rc.setActive(request.active());
        if (request.subjectId() != null) rc.setSubject(resolveSubject(request.subjectId(), user.getId()));

        return toResponse(classRepository.save(rc));
    }

    @Transactional
    public void delete(Long id, User user) {
        RecurringClass rc = findOrThrow(id, user.getId());
        rc.setDeletedAt(LocalDateTime.now());
        rc.setActive(false);
        classRepository.save(rc);
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private RecurringClass findOrThrow(Long id, Long userId) {
        return classRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Aula recorrente não encontrada: " + id));
    }

    private Subject resolveSubject(Long subjectId, Long userId) {
        if (subjectId == null) return null;
        return subjectRepository.findByIdAndUserId(subjectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Matéria não encontrada: " + subjectId));
    }

    public RecurringClassDTOs.Response toResponse(RecurringClass rc) {
        return new RecurringClassDTOs.Response(
                rc.getId(),
                rc.getTitle(),
                rc.getDayOfWeek(),
                rc.getStartTime(),
                rc.getEndTime(),
                rc.getLocation(),
                rc.getStartDate(),
                rc.getEndDate(),
                rc.isActive(),
                rc.getSubject() != null ? rc.getSubject().getId() : null,
                rc.getSubject() != null ? rc.getSubject().getName() : null,
                rc.getSubject() != null ? rc.getSubject().getColor() : null,
                rc.getCreatedAt()
        );
    }
}
