package com.skhueats.post.repository;

import com.skhueats.post.entity.Post;
import com.skhueats.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface PostRepository extends JpaRepository<Post, String> {

    long countByHostAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            User host,
            LocalDateTime start,
            LocalDateTime end
    );
}
