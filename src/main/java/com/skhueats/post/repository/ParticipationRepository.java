package com.skhueats.post.repository;

import com.skhueats.post.entity.Participation;
import com.skhueats.post.entity.ParticipationStatus;
import com.skhueats.post.entity.Post;
import com.skhueats.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ParticipationRepository extends JpaRepository<Participation, String> {

    boolean existsByPostAndUserAndStatus(Post post, User user, ParticipationStatus status);

    Optional<Participation> findByPostAndUser(Post post, User user);
}
