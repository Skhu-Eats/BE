package com.skhueats.post.repository;

import com.skhueats.post.entity.PostFoodCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostFoodCategoryRepository extends JpaRepository<PostFoodCategory, Long> {

    List<PostFoodCategory> findAllByPostId(String postId);

    @Query("select pfc from PostFoodCategory pfc join fetch pfc.post where pfc.post.id in :postIds")
    List<PostFoodCategory> findAllByPostIdIn(@Param("postIds") List<String> postIds);

    @Modifying(flushAutomatically = true)
    @Query("delete from PostFoodCategory pfc where pfc.post.id = :postId")
    void deleteByPostId(@Param("postId") String postId);
}
