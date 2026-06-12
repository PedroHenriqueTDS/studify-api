package com.studify.repository;

import com.studify.entity.AcademicEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AcademicEventRepository extends JpaRepository<AcademicEvent, Long> {

    Page<AcademicEvent> findByUserIdAndDeletedAtIsNull(Long userId, Pageable pageable);

    Optional<AcademicEvent> findByIdAndUserIdAndDeletedAtIsNull(Long id, Long userId);

    @Query("""
            SELECT e FROM AcademicEvent e
            WHERE e.user.id = :userId
              AND e.deletedAt IS NULL
              AND e.startDateTime >= :start
              AND e.startDateTime < :end
            ORDER BY e.startDateTime ASC
            """)
    List<AcademicEvent> findByPeriod(@Param("userId") Long userId,
                                     @Param("start") LocalDateTime start,
                                     @Param("end") LocalDateTime end);
}
