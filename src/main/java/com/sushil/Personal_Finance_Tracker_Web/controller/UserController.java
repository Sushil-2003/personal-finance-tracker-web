package com.sushil.Personal_Finance_Tracker_Web.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sushil.Personal_Finance_Tracker_Web.dto.UserDTO;
import com.sushil.Personal_Finance_Tracker_Web.entity.User;
import com.sushil.Personal_Finance_Tracker_Web.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByEmail(email);
        UserDTO userDTO = convertToDTO(user);
        return ResponseEntity.ok(userDTO);
    }

    @PutMapping("/dark-mode")
    public ResponseEntity<UserDTO> toggleDarkMode() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserDTO userDTO = userService.toggleDarkMode(email);
        return ResponseEntity.ok(userDTO);
    }

    private UserDTO convertToDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setEmail(user.getEmail());
        userDTO.setName(user.getName());
        userDTO.setDarkModeEnabled(user.isDarkModeEnabled());
        return userDTO;
    }
}