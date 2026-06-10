package com.skhueats.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@Table(name = "user_food_preferences")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserFoodPreference {

    @Id
    @Column(length = 36, nullable = false, updatable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 30)
    private String category;

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
    }

    private UserFoodPreference(User user, String category) {
        this.user = user;
        this.category = category;
    }

    public static UserFoodPreference of(User user, String category) {
        return new UserFoodPreference(user, category);
    }
}
