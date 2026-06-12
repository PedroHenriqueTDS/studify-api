package com.studify.repository;

import com.studify.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    Page<Task> findByUserId(Long userId, Pageable pageable);

    Page<Task> findByUserIdAndStatus(Long userId, Task.TaskStatus status, Pageable pageable);

    Page<Task> findByUserIdAndSubjectId(Long userId, Long subjectId, Pageable pageable);

    Optional<Task> findByIdAndUserId(Long id, Long userId);

    long countByUserIdAndStatus(Long userId, Task.TaskStatus status);

    @Query("""
            SELECT t FROM Task t
            WHERE t.user.id = :userId
              AND t.dueDate >= :start
              AND t.dueDate <= :end
              AND t.status NOT IN (
                  'COMPLETED',
                  'CANCELLED'
              )
            ORDER BY t.dueDate ASC
            """)
    List<Task> findPendingByDueDateRange(@Param("userId") Long userId,
                                         @Param("start") LocalDate start,
                                         @Param("end") LocalDate end);

    @Query("""
            SELECT COUNT(t) FROM Task t
            WHERE t.user.id = :userId
              AND t.status = 'COMPLETED'
              AND t.completedAt >= :from
              AND t.completedAt <= :to
            """)
    long countCompletedInPeriod(@Param("userId") Long userId,
                                @Param("from") java.time.LocalDateTime from,
                                @Param("to") java.time.LocalDateTime to);

    @Query("""
            SELECT COUNT(t) FROM Task t
            WHERE t.user.id = :userId
              AND t.status NOT IN ('COMPLETED', 'CANCELLED')
            """)
    long countPending(@Param("userId") Long userId);

    @Query("""
            SELECT COUNT(t) FROM Task t
            WHERE t.user.id = :userId
              AND t.status NOT IN ('COMPLETED', 'CANCELLED')
              AND t.dueDate < :today
            """)
    long countOverdue(@Param("userId") Long userId, @Param("today") LocalDate today);
}
