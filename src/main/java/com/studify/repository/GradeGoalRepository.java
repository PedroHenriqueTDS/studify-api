package com.studify.repository;

import com.studify.entity.GradeGoal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GradeGoalRepository extends JpaRepository<GradeGoal, Long> {

    Page<GradeGoal> findByUserIdAndDeletedAtIsNull(Long userId, Pageable pageable);

    Optional<GradeGoal> findByIdAndUserIdAndDeletedAtIsNull(Long id, Long userId);
}
