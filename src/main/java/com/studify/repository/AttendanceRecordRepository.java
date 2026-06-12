package com.studify.repository;

import com.studify.entity.AttendanceRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    Optional<AttendanceRecord> findByIdAndUserIdAndDeletedAtIsNull(Long id, Long userId);

    Optional<AttendanceRecord> findByUserIdAndRecurringClassIdAndDateAndDeletedAtIsNull(Long userId, Long recurringClassId, LocalDate date);

    @Query("""
            SELECT ar FROM AttendanceRecord ar
            WHERE ar.user.id = :userId
              AND ar.recurringClass.subject.id = :subjectId
              AND ar.deletedAt IS NULL
            ORDER BY ar.date DESC
            """)
    Page<AttendanceRecord> findBySubjectIdAndUserId(@Param("subjectId") Long subjectId,
                                                    @Param("userId") Long userId,
                                                    Pageable pageable);

    @Query("""
            SELECT ar FROM AttendanceRecord ar
            WHERE ar.user.id = :userId
              AND ar.deletedAt IS NULL
            """)
    List<AttendanceRecord> findAllActiveByUserId(@Param("userId") Long userId);

    @Query("""
            SELECT ar FROM AttendanceRecord ar
            WHERE ar.user.id = :userId
              AND ar.date >= :from
              AND ar.date <= :to
              AND ar.deletedAt IS NULL
            """)
    List<AttendanceRecord> findByUserIdAndDateBetween(@Param("userId") Long userId,
                                                      @Param("from") LocalDate from,
                                                      @Param("to") LocalDate to);
}
