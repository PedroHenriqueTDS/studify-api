package com.studify.service;

import com.studify.dto.studysession.StudySessionDTOs;
import com.studify.entity.StudySession;
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

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class StudySessionService {

    private final StudySessionRepository sessionRepository;
    private final SubjectRepository subjectRepository;

    @Transactional
    public StudySessionDTOs.Response create(StudySessionDTOs.CreateRequest request, User user) {
        Subject subject = subjectRepository.findByIdAndUserId(request.subjectId(), user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Matéria não encontrada: " + request.subjectId()));

        StudySession session = StudySession.builder()
                .subject(subject)
                .user(user)
                .startTime(request.startTime())
                .endTime(request.endTime())
                .notes(request.notes())
                .status(request.endTime() != null ? StudySession.SessionStatus.COMPLETED : StudySession.SessionStatus.IN_PROGRESS)
                .build();

        if (request.endTime() != null) {
            long minutes = Duration.between(request.startTime(), request.endTime()).toMinutes();
            session.setDurationMinutes((int) minutes);
        }

        return toResponse(sessionRepository.save(session));
    }

    @Transactional(readOnly = true)
    public Page<StudySessionDTOs.Response> findAll(User user, Long subjectId, Pageable pageable) {
        if (subjectId != null) {
            return sessionRepository.findByUserIdAndSubjectId(user.getId(), subjectId, pageable)
                    .map(this::toResponse);
        }
        return sessionRepository.findByUserId(user.getId(), pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public StudySessionDTOs.Response findById(Long id, User user) {
        return toResponse(findOrThrow(id, user.getId()));
    }

    @Transactional
    public StudySessionDTOs.Response finish(Long id, StudySessionDTOs.FinishRequest request, User user) {
        StudySession session = findOrThrow(id, user.getId());

        if (session.getStatus() == StudySession.SessionStatus.COMPLETED) {
            throw new BusinessException("Sessão já foi finalizada");
        }

        session.setEndTime(request.endTime());
        session.setNotes(request.notes());
        session.setStatus(StudySession.SessionStatus.COMPLETED);

        long minutes = Duration.between(session.getStartTime(), request.endTime()).toMinutes();
        session.setDurationMinutes((int) minutes);

        return toResponse(sessionRepository.save(session));
    }

    @Transactional
    public StudySessionDTOs.Response update(Long id, StudySessionDTOs.UpdateRequest request, User user) {
        StudySession session = findOrThrow(id, user.getId());

        if (request.startTime() != null) session.setStartTime(request.startTime());
        if (request.endTime() != null) {
            session.setEndTime(request.endTime());
            long minutes = Duration.between(session.getStartTime(), request.endTime()).toMinutes();
            session.setDurationMinutes((int) minutes);
        }
        if (request.notes() != null) session.setNotes(request.notes());
        if (request.status() != null) session.setStatus(request.status());

        return toResponse(sessionRepository.save(session));
    }

    @Transactional
    public void delete(Long id, User user) {
        StudySession session = findOrThrow(id, user.getId());
        sessionRepository.delete(session);
    }

    @Transactional(readOnly = true)
    public StudySessionDTOs.SummaryResponse getSummary(User user) {
        Integer totalMinutes = sessionRepository.sumDurationByUserId(user.getId());
        long totalSessions = sessionRepository.countByUserId(user.getId());

        return new StudySessionDTOs.SummaryResponse(
                totalMinutes,
                totalMinutes / 60,
                totalSessions,
                totalSessions > 0 ? (double) totalMinutes / totalSessions : 0
        );
    }

    @Transactional(readOnly = true)
    public StudySessionDTOs.SummaryResponse getSummaryByPeriod(User user, LocalDateTime from, LocalDateTime to) {
        Integer totalMinutes = sessionRepository.sumDurationByUserIdAndPeriod(user.getId(), from, to);
        long totalSessions = sessionRepository.countByUserId(user.getId());

        return new StudySessionDTOs.SummaryResponse(
                totalMinutes,
                totalMinutes / 60,
                totalSessions,
                totalSessions > 0 ? (double) totalMinutes / totalSessions : 0
        );
    }

    private StudySession findOrThrow(Long id, Long userId) {
        return sessionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Sessão não encontrada: " + id));
    }

    private StudySessionDTOs.Response toResponse(StudySession s) {
        return new StudySessionDTOs.Response(
                s.getId(),
                s.getSubject().getId(),
                s.getSubject().getName(),
                s.getSubject().getColor(),
                s.getStartTime(),
                s.getEndTime(),
                s.getDurationMinutes(),
                s.getNotes(),
                s.getStatus(),
                s.getCreatedAt()
        );
    }
}
