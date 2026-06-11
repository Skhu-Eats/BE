package com.skhueats.post.repository;

import com.skhueats.post.entity.PostFoodCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostFoodCategoryRepository extends JpaRepository<PostFoodCategory, Long> {

    List<PostFoodCategory> findAllByPostId(String postId);

    List<PostFoodCategory> findAllByPostIdIn(List<String> postIds);

    void deleteByPostId(String postId);
}
