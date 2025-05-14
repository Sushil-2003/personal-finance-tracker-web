package com.sushil.Personal_Finance_Tracker_Web.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sushil.Personal_Finance_Tracker_Web.entity.Transaction;
import com.sushil.Personal_Finance_Tracker_Web.entity.User;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUser(User user);
}
