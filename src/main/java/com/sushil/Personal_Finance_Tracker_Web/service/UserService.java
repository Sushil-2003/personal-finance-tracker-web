package com.sushil.Personal_Finance_Tracker_Web.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.sushil.Personal_Finance_Tracker_Web.dto.UserDTO;
import com.sushil.Personal_Finance_Tracker_Web.entity.User;
import com.sushil.Personal_Finance_Tracker_Web.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserDTO registerUser(String name, String email, String password) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already registered");
        }
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setDarkModeEnabled(false);
        user = userRepository.save(user);
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setEmail(user.getEmail());
        userDTO.setName(user.getName());
        userDTO.setDarkModeEnabled(user.isDarkModeEnabled());
        return userDTO;
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public UserDTO toggleDarkMode(String email) {
        User user = findByEmail(email);
        user.setDarkModeEnabled(!user.isDarkModeEnabled());
        user = userRepository.save(user);
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setEmail(user.getEmail());
        userDTO.setName(user.getName());
        userDTO.setDarkModeEnabled(user.isDarkModeEnabled());
        return userDTO;
    }
}