package com.skhueats.post.repository;

import com.skhueats.post.entity.Participation;
import com.skhueats.post.entity.ParticipationStatus;
import com.skhueats.post.entity.Post;
import com.skhueats.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ParticipationRepository extends JpaRepository<Participation, String> {

    boolean existsByPostAndUserAndStatus(Post post, User user, ParticipationStatus status);

    Optional<Participation> findByPostAndUser(Post post, User user);

    @Query("""
            SELECT p FROM Participation p
            JOIN FETCH p.user
            WHERE p.post.id = :postId
              AND p.status = :status
            ORDER BY p.createdAt ASC
            """)
    List<Participation> findAllByPostIdAndStatusWithUser(
            @Param("postId") String postId,
            @Param("status") ParticipationStatus status
    );

    @Query("""
            SELECT p FROM Participation p
            JOIN FETCH p.user
            WHERE p.post = :post
              AND p.status = :status
            """)
    List<Participation> findAllByPostAndStatusWithUser(
            @Param("post") Post post,
            @Param("status") ParticipationStatus status
    );

    @Query(
            value = """
                    SELECT p FROM Participation p
                    JOIN FETCH p.post joinedPost
                    JOIN FETCH joinedPost.host
                    WHERE p.user.id = :userId
                    ORDER BY
                      CASE WHEN p.status = :joinedStatus THEN 0 ELSE 1 END,
                      joinedPost.meetingTime DESC,
                      p.createdAt DESC
                    """,
            countQuery = "SELECT COUNT(p) FROM Participation p WHERE p.user.id = :userId"
    )
    Page<Participation> findHistoryByUserId(
            @Param("userId") String userId,
            @Param("joinedStatus") ParticipationStatus joinedStatus,
            Pageable pageable
    );

    @Query("""
            SELECT p FROM Participation p
            JOIN FETCH p.post joinedPost
            JOIN FETCH joinedPost.host
            WHERE p.user.id = :userId
            ORDER BY
              CASE WHEN p.status = :joinedStatus THEN 0 ELSE 1 END,
              joinedPost.meetingTime DESC,
              p.createdAt DESC
            """)
    List<Participation> findHistoryPreviewByUserId(
            @Param("userId") String userId,
            @Param("joinedStatus") ParticipationStatus joinedStatus,
            Pageable pageable
    );
}
