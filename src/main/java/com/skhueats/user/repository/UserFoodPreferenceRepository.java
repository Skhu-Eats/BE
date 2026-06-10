package com.skhueats.user.repository;

import com.skhueats.user.entity.User;
import com.skhueats.user.entity.UserFoodPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserFoodPreferenceRepository extends JpaRepository<UserFoodPreference, String> {

    @Query("SELECT ufp.category FROM UserFoodPreference ufp WHERE ufp.user = :user")
    List<String> findCategoriesByUser(@Param("user") User user);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM UserFoodPreference ufp WHERE ufp.user = :user")
    void deleteAllByUserInBulk(@Param("user") User user);
}
