package com.studify.repository;

import com.studify.entity.Subject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SubjectRepository extends JpaRepository<Subject, Long> {

    Page<Subject> findByUserId(Long userId, Pageable pageable);

    Optional<Subject> findByIdAndUserId(Long id, Long userId);

    List<Subject> findByUserId(Long userId);

    boolean existsByNameAndUserId(String name, Long userId);

    @Query("SELECT s FROM Subject s WHERE s.user.id = :userId AND " +
           "LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Subject> findByUserIdAndNameContaining(Long userId, String name);
}
