package com.sushil.Personal_Finance_Tracker_Web.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password; // Store hashed password

    @Column(nullable = false)
    private String name;

    private boolean darkModeEnabled;
}