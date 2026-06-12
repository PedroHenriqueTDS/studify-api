package com.studify.repository;

import com.studify.entity.SessionNote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SessionNoteRepository extends JpaRepository<SessionNote, Long> {

    Page<SessionNote> findByStudySessionIdAndUserIdAndDeletedAtIsNull(Long studySessionId,
                                                                      Long userId,
                                                                      Pageable pageable);

    Optional<SessionNote> findByIdAndUserIdAndDeletedAtIsNull(Long id, Long userId);

    @Query("""
            SELECT n FROM SessionNote n
            WHERE n.user.id = :userId
              AND n.deletedAt IS NULL
              AND LOWER(n.content) LIKE LOWER(CONCAT('%', :query, '%'))
            ORDER BY n.createdAt DESC
            """)
    Page<SessionNote> searchByContent(@Param("userId") Long userId,
                                      @Param("query") String query,
                                      Pageable pageable);
}
