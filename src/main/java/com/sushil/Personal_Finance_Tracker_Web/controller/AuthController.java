package com.sushil.Personal_Finance_Tracker_Web.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sushil.Personal_Finance_Tracker_Web.dto.AuthResponse;
import com.sushil.Personal_Finance_Tracker_Web.dto.LoginRequest;
import com.sushil.Personal_Finance_Tracker_Web.dto.RegisterRequest;
import com.sushil.Personal_Finance_Tracker_Web.dto.UserDTO;
import com.sushil.Personal_Finance_Tracker_Web.entity.User;
import com.sushil.Personal_Finance_Tracker_Web.security.JwtUtil;
import com.sushil.Personal_Finance_Tracker_Web.service.UserService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        String email = authentication.getName();
        String token = jwtUtil.generateToken(email);
        User user = userService.findByEmail(email);
        UserDTO userDTO = convertToDTO(user);
        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setUser(userDTO);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        UserDTO user = userService.registerUser(request.getName(), request.getEmail(), request.getPassword());
        String token = jwtUtil.generateToken(user.getEmail());
        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setUser(user);
        return ResponseEntity.ok(response);
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
