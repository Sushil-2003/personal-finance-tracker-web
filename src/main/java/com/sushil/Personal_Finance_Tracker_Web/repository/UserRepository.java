package com.sushil.Personal_Finance_Tracker_Web.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sushil.Personal_Finance_Tracker_Web.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}