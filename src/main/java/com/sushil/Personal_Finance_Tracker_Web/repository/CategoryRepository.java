package com.sushil.Personal_Finance_Tracker_Web.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sushil.Personal_Finance_Tracker_Web.entity.Category;
import com.sushil.Personal_Finance_Tracker_Web.entity.User;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByUserIsNullOrUser(User user);
    boolean existsByNameAndUser(String name, User user);
    boolean existsByNameAndUserIsNull(String name);
}