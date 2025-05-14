package com.sushil.Personal_Finance_Tracker_Web.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String name;
    private String email;
    private String password;
}