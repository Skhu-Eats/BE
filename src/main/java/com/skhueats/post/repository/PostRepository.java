package com.skhueats.post.repository;

import com.skhueats.post.entity.Post;
import com.skhueats.user.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, String> {

    long countByHostAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            User host,
            LocalDateTime start,
            LocalDateTime end
    );

    @Query("SELECT p FROM Post p JOIN FETCH p.host WHERE p.id = :id")
    Optional<Post> findByIdWithHost(@Param("id") String id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Post p JOIN FETCH p.host WHERE p.id = :id")
    Optional<Post> findByIdWithHostForUpdate(@Param("id") String id);

    @Query(value = """
            SELECT p.* FROM posts p
            WHERE (:startHour IS NULL OR HOUR(p.meeting_time) >= :startHour)
              AND (:endHour IS NULL OR HOUR(p.meeting_time) < :endHour)
              AND (:location IS NULL OR p.location LIKE CONCAT('%', :location, '%'))
              AND (:maxParticipants IS NULL OR p.max_participants = :maxParticipants)
              AND (:foodCategory IS NULL OR EXISTS (
                  SELECT 1 FROM post_food_categories pfc
                  WHERE pfc.post_id = p.id
                    AND pfc.category = :foodCategory
              ))
            ORDER BY p.deadline ASC
            """, nativeQuery = true)
    List<Post> findPostsByFilter(
            @Param("startHour") Integer startHour,
            @Param("endHour") Integer endHour,
            @Param("location") String location,
            @Param("maxParticipants") Integer maxParticipants,
            @Param("foodCategory") String foodCategory
    );

    @Query(value = """
            SELECT p.* FROM posts p
            WHERE p.host_id = :hostId
            ORDER BY
              CASE LOWER(p.status)
                WHEN 'open' THEN 0
                WHEN 'closed' THEN 1
                ELSE 2
              END,
              p.meeting_time DESC
            """, nativeQuery = true)
    List<Post> findAllByHostActiveFirst(@Param("hostId") String hostId);
}
