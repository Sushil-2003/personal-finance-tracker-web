package com.sushil.Personal_Finance_Tracker_Web.dto;

import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String email;
    private String name;
    private boolean darkModeEnabled;
}