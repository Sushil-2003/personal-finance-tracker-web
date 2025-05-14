package com.sushil.Personal_Finance_Tracker_Web.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sushil.Personal_Finance_Tracker_Web.dto.CategoryDTO;
import com.sushil.Personal_Finance_Tracker_Web.entity.User;
import com.sushil.Personal_Finance_Tracker_Web.service.CategoryService;
import com.sushil.Personal_Finance_Tracker_Web.service.UserService;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private static final Logger logger = LoggerFactory.getLogger(CategoryController.class);

    private final CategoryService categoryService;
    private final UserService userService;

    public CategoryController(CategoryService categoryService, UserService userService) {
        this.categoryService = categoryService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getCategories() {
        try {
            User user = getCurrentUser();
            if (user == null) {
                logger.warn("Unauthorized access attempt to get categories");
                return ResponseEntity.status(401).body(null);
            }
            List<CategoryDTO> categories = categoryService.getCategories(user);
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            logger.error("Error fetching categories", e);
            return ResponseEntity.status(500).body(null);
        }
    }

    @PostMapping
    public ResponseEntity<?> addCategory(@RequestBody CategoryDTO dto) {
        try {
            User user = getCurrentUser();
            if (user == null) {
                logger.warn("Unauthorized attempt to add category");
                return ResponseEntity.status(401).body("Unauthorized: User not found");
            }
            // Basic validation
            if (dto.getName() == null || dto.getName().trim().isEmpty()) {
                logger.warn("Invalid category name provided: {}", dto.getName());
                return ResponseEntity.badRequest().body("Category name cannot be empty");
            }
            logger.info("Attempting to add category: {} for user: {}", dto.getName(), user.getEmail());
            CategoryDTO savedCategory = categoryService.addCategory(user, dto.getName());
            logger.info("Category added successfully: {}", savedCategory.getName());
            return ResponseEntity.status(201).body(savedCategory);
        } catch (IllegalArgumentException e) {
            logger.warn("Bad request while adding category: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error adding category: {}", dto.getName(), e);
            return ResponseEntity.status(500).body("Failed to add category: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        try {
            User user = getCurrentUser();
            if (user == null) {
                logger.warn("Unauthorized attempt to delete category with ID: {}", id);
                return ResponseEntity.status(401).body("Unauthorized: User not found");
            }
            categoryService.deleteCategory(user, id);
            logger.info("Category deleted successfully: ID {}", id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            logger.warn("Bad request while deleting category ID {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error deleting category with ID: {}", id, e);
            return ResponseEntity.status(500).body("Failed to delete category: " + e.getMessage());
        }
    }

    private User getCurrentUser() {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            if (email == null || "anonymousUser".equals(email)) {
                return null;
            }
            return userService.findByEmail(email);
        } catch (Exception e) {
            logger.error("Error retrieving current user", e);
            return null;
        }
    }
}