package com.sushil.Personal_Finance_Tracker_Web.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "transactions")
@Data
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String type; // "income" or "expense"

    @Column(nullable = false)
    private String category;

    public void setCategory(String category) {
        this.category = category;
    }
    
    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private double amount;
}