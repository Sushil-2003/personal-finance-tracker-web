package com.sushil.Personal_Finance_Tracker_Web.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
}