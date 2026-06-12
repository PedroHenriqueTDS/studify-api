package com.studify.repository;

import com.studify.entity.RecurringClass;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RecurringClassRepository extends JpaRepository<RecurringClass, Long> {

    Page<RecurringClass> findByUserIdAndDeletedAtIsNull(Long userId, Pageable pageable);

    List<RecurringClass> findByUserIdAndDeletedAtIsNull(Long userId);

    Optional<RecurringClass> findByIdAndUserIdAndDeletedAtIsNull(Long id, Long userId);

    @Query("""
            SELECT rc FROM RecurringClass rc
            WHERE rc.user.id = :userId
              AND rc.active = true
              AND rc.deletedAt IS NULL
              AND rc.startDate <= :periodEnd
              AND rc.endDate   >= :periodStart
            """)
    List<RecurringClass> findActiveInPeriod(@Param("userId") Long userId,
                                            @Param("periodStart") LocalDate periodStart,
                                            @Param("periodEnd") LocalDate periodEnd);
}
