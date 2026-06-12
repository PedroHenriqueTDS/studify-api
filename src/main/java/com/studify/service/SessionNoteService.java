package com.studify.service;

import com.studify.dto.sessionnote.SessionNoteDTOs;
import com.studify.entity.SessionNote;
import com.studify.entity.StudySession;
import com.studify.entity.User;
import com.studify.exception.ResourceNotFoundException;
import com.studify.repository.SessionNoteRepository;
import com.studify.repository.StudySessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SessionNoteService {

    private final SessionNoteRepository sessionNoteRepository;
    private final StudySessionRepository studySessionRepository;

    @Transactional
    public SessionNoteDTOs.Response create(Long sessionId,
                                           SessionNoteDTOs.CreateRequest request,
                                           User user) {
        StudySession session = findSessionOrThrow(sessionId, user.getId());

        SessionNote note = SessionNote.builder()
                .studySession(session)
                .user(user)
                .content(request.content())
                .build();

        return toResponse(sessionNoteRepository.save(note));
    }

    @Transactional(readOnly = true)
    public Page<SessionNoteDTOs.Response> findBySession(Long sessionId, User user, Pageable pageable) {
        findSessionOrThrow(sessionId, user.getId()); // valida ownership
        return sessionNoteRepository
                .findByStudySessionIdAndUserIdAndDeletedAtIsNull(sessionId, user.getId(), pageable)
                .map(this::toResponse);
    }

    @Transactional
    public SessionNoteDTOs.Response update(Long sessionId, Long noteId,
                                           SessionNoteDTOs.UpdateRequest request,
                                           User user) {
        findSessionOrThrow(sessionId, user.getId()); // valida ownership da sessão
        SessionNote note = findNoteOrThrow(noteId, user.getId());

        note.setContent(request.content());
        return toResponse(sessionNoteRepository.save(note));
    }

    @Transactional
    public void delete(Long sessionId, Long noteId, User user) {
        findSessionOrThrow(sessionId, user.getId()); // valida ownership
        SessionNote note = findNoteOrThrow(noteId, user.getId());
        note.setDeletedAt(LocalDateTime.now());
        sessionNoteRepository.save(note);
    }

    @Transactional(readOnly = true)
    public Page<SessionNoteDTOs.Response> search(String query, User user, Pageable pageable) {
        return sessionNoteRepository
                .searchByContent(user.getId(), query, pageable)
                .map(this::toResponse);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private StudySession findSessionOrThrow(Long sessionId, Long userId) {
        return studySessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Sessão de estudo não encontrada: " + sessionId));
    }

    private SessionNote findNoteOrThrow(Long noteId, Long userId) {
        return sessionNoteRepository.findByIdAndUserIdAndDeletedAtIsNull(noteId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Anotação não encontrada: " + noteId));
    }

    private SessionNoteDTOs.Response toResponse(SessionNote note) {
        return new SessionNoteDTOs.Response(
                note.getId(),
                note.getStudySession().getId(),
                note.getContent(),
                note.getCreatedAt(),
                note.getUpdatedAt()
        );
    }
}
