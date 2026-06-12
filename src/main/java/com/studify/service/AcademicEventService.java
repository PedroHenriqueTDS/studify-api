package com.studify.service;

import com.studify.dto.academicevent.AcademicEventDTOs;
import com.studify.entity.AcademicEvent;
import com.studify.entity.Subject;
import com.studify.entity.User;
import com.studify.exception.ResourceNotFoundException;
import com.studify.repository.AcademicEventRepository;
import com.studify.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AcademicEventService {

    private final AcademicEventRepository eventRepository;
    private final SubjectRepository subjectRepository;

    @Transactional
    public AcademicEventDTOs.Response create(AcademicEventDTOs.CreateRequest request, User user) {
        Subject subject = resolveSubject(request.subjectId(), user.getId());

        AcademicEvent event = AcademicEvent.builder()
                .title(request.title())
                .description(request.description())
                .type(request.type())
                .startDateTime(request.startDateTime())
                .endDateTime(request.endDateTime())
                .location(request.location())
                .subject(subject)
                .user(user)
                .build();

        return toResponse(eventRepository.save(event));
    }

    @Transactional(readOnly = true)
    public Page<AcademicEventDTOs.Response> findAll(User user, Pageable pageable) {
        return eventRepository.findByUserIdAndDeletedAtIsNull(user.getId(), pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public AcademicEventDTOs.Response findById(Long id, User user) {
        return toResponse(findOrThrow(id, user.getId()));
    }

    @Transactional
    public AcademicEventDTOs.Response update(Long id, AcademicEventDTOs.UpdateRequest request, User user) {
        AcademicEvent event = findOrThrow(id, user.getId());

        if (request.title() != null)         event.setTitle(request.title());
        if (request.description() != null)   event.setDescription(request.description());
        if (request.type() != null)          event.setType(request.type());
        if (request.startDateTime() != null) event.setStartDateTime(request.startDateTime());
        if (request.endDateTime() != null)   event.setEndDateTime(request.endDateTime());
        if (request.location() != null)      event.setLocation(request.location());
        if (request.subjectId() != null)     event.setSubject(resolveSubject(request.subjectId(), user.getId()));

        return toResponse(eventRepository.save(event));
    }

    @Transactional
    public void delete(Long id, User user) {
        AcademicEvent event = findOrThrow(id, user.getId());
        event.setDeletedAt(LocalDateTime.now());
        eventRepository.save(event);
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private AcademicEvent findOrThrow(Long id, Long userId) {
        return eventRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento não encontrado: " + id));
    }

    private Subject resolveSubject(Long subjectId, Long userId) {
        if (subjectId == null) return null;
        return subjectRepository.findByIdAndUserId(subjectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Matéria não encontrada: " + subjectId));
    }

    public AcademicEventDTOs.Response toResponse(AcademicEvent e) {
        return new AcademicEventDTOs.Response(
                e.getId(),
                e.getTitle(),
                e.getDescription(),
                e.getType(),
                e.getStartDateTime(),
                e.getEndDateTime(),
                e.getLocation(),
                e.getSubject() != null ? e.getSubject().getId() : null,
                e.getSubject() != null ? e.getSubject().getName() : null,
                e.getSubject() != null ? e.getSubject().getColor() : null,
                e.getCreatedAt()
        );
    }
}
