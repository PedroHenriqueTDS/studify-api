package com.studify.repository;

import com.studify.entity.StudySession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

public interface StudySessionRepository extends JpaRepository<StudySession, Long> {

    Page<StudySession> findByUserId(Long userId, Pageable pageable);

    Page<StudySession> findByUserIdAndSubjectId(Long userId, Long subjectId, Pageable pageable);

    Optional<StudySession> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT COALESCE(SUM(s.durationMinutes), 0) FROM StudySession s " +
           "WHERE s.user.id = :userId AND s.status = 'COMPLETED'")
    Integer sumDurationByUserId(Long userId);

    @Query("SELECT COALESCE(SUM(s.durationMinutes), 0) FROM StudySession s " +
           "WHERE s.user.id = :userId AND s.subject.id = :subjectId AND s.status = 'COMPLETED'")
    Integer sumDurationByUserIdAndSubjectId(Long userId, Long subjectId);

    @Query("SELECT COALESCE(SUM(s.durationMinutes), 0) FROM StudySession s " +
           "WHERE s.user.id = :userId AND s.startTime >= :from AND s.startTime <= :to AND s.status = 'COMPLETED'")
    Integer sumDurationByUserIdAndPeriod(Long userId, LocalDateTime from, LocalDateTime to);

    long countByUserId(Long userId);
}
