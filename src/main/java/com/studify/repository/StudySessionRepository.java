package com.studify.repository;

import com.studify.entity.StudySession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
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

    @Query("""
            SELECT s FROM StudySession s
            WHERE s.user.id = :userId
              AND s.startTime >= :start
              AND s.startTime < :end
            ORDER BY s.startTime ASC
            """)
    List<StudySession> findByPeriod(@Param("userId") Long userId,
                                    @Param("start") LocalDateTime start,
                                    @Param("end") LocalDateTime end);

    @Query("""
            SELECT s FROM StudySession s
            WHERE s.user.id = :userId
              AND s.status = 'COMPLETED'
              AND s.durationMinutes IS NOT NULL
              AND s.durationMinutes > 0
            ORDER BY s.startTime ASC
            """)
    List<StudySession> findAllCompletedByUserId(@Param("userId") Long userId);

    @Query("""
            SELECT s FROM StudySession s
            WHERE s.user.id = :userId
              AND s.status = 'COMPLETED'
              AND s.durationMinutes IS NOT NULL
              AND s.startTime >= :startOfYear
              AND s.startTime < :startOfNextYear
            ORDER BY s.startTime ASC
            """)
    List<StudySession> findCompletedByYear(@Param("userId") Long userId,
                                           @Param("startOfYear") LocalDateTime startOfYear,
                                           @Param("startOfNextYear") LocalDateTime startOfNextYear);

    @Query("""
            SELECT s FROM StudySession s
            WHERE s.user.id = :userId
              AND s.status = 'COMPLETED'
              AND s.durationMinutes IS NOT NULL
              AND s.startTime >= :start
              AND s.startTime < :end
            ORDER BY s.startTime ASC
            """)
    List<StudySession> findCompletedInRange(@Param("userId") Long userId,
                                            @Param("start") LocalDateTime start,
                                            @Param("end") LocalDateTime end);
}
