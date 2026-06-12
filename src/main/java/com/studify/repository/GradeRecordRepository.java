package com.studify.repository;

import com.studify.entity.GradeRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GradeRecordRepository extends JpaRepository<GradeRecord, Long> {

    Optional<GradeRecord> findByIdAndUserIdAndDeletedAtIsNull(Long id, Long userId);

    @Query("""
            SELECT r FROM GradeRecord r
            WHERE r.gradeGoal.id = :goalId
              AND r.user.id = :userId
              AND r.deletedAt IS NULL
            ORDER BY r.createdAt ASC
            """)
    Page<GradeRecord> findByGoalIdAndUserId(@Param("goalId") Long goalId,
                                            @Param("userId") Long userId,
                                            Pageable pageable);

    @Query("""
            SELECT r FROM GradeRecord r
            WHERE r.gradeGoal.id = :goalId
              AND r.user.id = :userId
              AND r.deletedAt IS NULL
            """)
    List<GradeRecord> findAllActiveByGoalIdAndUserId(@Param("goalId") Long goalId,
                                                     @Param("userId") Long userId);
}
