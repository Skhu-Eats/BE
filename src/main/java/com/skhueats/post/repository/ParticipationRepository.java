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

import java.util.Optional;

public interface ParticipationRepository extends JpaRepository<Participation, String> {

    boolean existsByPostAndUserAndStatus(Post post, User user, ParticipationStatus status);

    Optional<Participation> findByPostAndUser(Post post, User user);

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
}
