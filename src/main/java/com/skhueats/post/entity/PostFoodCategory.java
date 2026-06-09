package com.skhueats.post.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "post_food_categories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostFoodCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(name = "category", nullable = false, length = 30)
    private String category;

    public PostFoodCategory(Post post, String category) {
        this.post = post;
        this.category = category;
    }
}
