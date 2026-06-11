package com.skhueats.post.repository;

import com.skhueats.post.entity.Post;
import com.skhueats.post.entity.PostStatus;
import com.skhueats.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, String> {

    long countByHostAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            User host,
            LocalDateTime start,
            LocalDateTime end
    );

    @Query("""
            SELECT p FROM Post p
            JOIN FETCH p.host
            WHERE (:status IS NULL OR p.status = :status)
              AND (:startHour IS NULL OR HOUR(p.meetingTime) >= :startHour)
              AND (:endHour IS NULL OR HOUR(p.meetingTime) < :endHour)
            ORDER BY p.deadline ASC
            """)
    List<Post> findPostsByFilter(
            @Param("status") PostStatus status,
            @Param("startHour") Integer startHour,
            @Param("endHour") Integer endHour
    );
}
