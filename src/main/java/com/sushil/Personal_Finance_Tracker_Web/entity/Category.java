package com.sushil.Personal_Finance_Tracker_Web.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "categories")
@Data
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id") // Null for default categories
    private User user;

    @Column(nullable = false)
    private String name;
}