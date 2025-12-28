package ru.practicum.commentservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.commentservice.model.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment c " +
            "WHERE c.eventId = :eventId " +
            "ORDER BY c.createdOn DESC")
    Page<Comment> findByEventIdOrderByCreatedOnDesc(@Param("eventId") Long eventId, Pageable pageable);

    @Query("SELECT c FROM Comment c " +
            "WHERE c.author = :authorId " +
            "ORDER BY c.createdOn DESC")
    Page<Comment> findByAuthorIdOrderByCreatedOnDesc(@Param("authorId") Long authorId, Pageable pageable);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.eventId = :eventId")
    Long countByEventId(@Param("eventId") Long eventId);

    @Query("SELECT c.eventId, COUNT(c) FROM Comment c WHERE c.eventId IN :eventIds GROUP BY c.eventId")
    List<Object[]> countCommentsByEventIds(@Param("eventIds") List<Long> eventIds);
}
