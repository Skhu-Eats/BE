package com.skhueats.user.repository;

import com.skhueats.user.entity.User;
import com.skhueats.user.entity.UserFoodPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserFoodPreferenceRepository extends JpaRepository<UserFoodPreference, String> {

    List<UserFoodPreference> findAllByUser(User user);

    void deleteAllByUser(User user);
}
