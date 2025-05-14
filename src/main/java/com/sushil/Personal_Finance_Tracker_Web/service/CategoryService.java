package com.sushil.Personal_Finance_Tracker_Web.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.sushil.Personal_Finance_Tracker_Web.dto.CategoryDTO;
import com.sushil.Personal_Finance_Tracker_Web.entity.Category;
import com.sushil.Personal_Finance_Tracker_Web.entity.User;
import com.sushil.Personal_Finance_Tracker_Web.repository.CategoryRepository;

import jakarta.annotation.PostConstruct;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @PostConstruct
    public void initDefaultCategories() {
        List<String> defaultCategories = List.of("Food", "Housing", "Transportation", "Entertainment", "Salary", "Other");
        for (String name : defaultCategories) {
            if (!categoryRepository.existsByNameAndUserIsNull(name)) {
                Category category = new Category();
                category.setName(name);
                categoryRepository.save(category);
            }
        }
    }

    public List<CategoryDTO> getCategories(User user) {
        return categoryRepository.findByUserIsNullOrUser(user).stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    public CategoryDTO addCategory(User user, String name) {
        // Validate category name
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be empty");
        }
        // Check for duplicates within the user's categories
        if (user != null && categoryRepository.existsByNameAndUser(name, user)) {
            throw new IllegalArgumentException("Category '" + name + "' already exists for this user");
        }
        Category category = new Category();
        category.setUser(user);
        category.setName(name);
        category = categoryRepository.save(category);
        return toDTO(category);
    }

    public void deleteCategory(User user, Long id) {
        Category category = categoryRepository.findById(id)
            .filter(c -> c.getUser() != null && c.getUser().equals(user))
            .orElseThrow(() -> new IllegalArgumentException("Category not found or not user-specific"));
        categoryRepository.delete(category);
    }

    public void validateCategory(String name, User user) {
        // Check if the category exists as a default category or as a user-specific category
        boolean exists = categoryRepository.existsByNameAndUserIsNull(name) || 
                        (user != null && categoryRepository.existsByNameAndUser(name, user));
        if (!exists) {
            throw new IllegalArgumentException("Category '" + name + "' does not exist");
        }
    }

    public Category findCategoryByName(String name, User user) {
        // First, check user-specific categories
        if (user != null) {
            List<Category> userCategories = categoryRepository.findByUserIsNullOrUser(user)
                .stream()
                .filter(c -> c.getUser() != null && c.getUser().equals(user) && c.getName().equals(name))
                .collect(Collectors.toList());
            if (!userCategories.isEmpty()) {
                return userCategories.get(0);
            }
        }
        // Then, check default categories
        List<Category> defaultCategories = categoryRepository.findByUserIsNullOrUser(user)
            .stream()
            .filter(c -> c.getUser() == null && c.getName().equals(name))
            .collect(Collectors.toList());
        if (!defaultCategories.isEmpty()) {
            return defaultCategories.get(0);
        }
        throw new IllegalArgumentException("Category '" + name + "' does not exist");
    }

    private CategoryDTO toDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        return dto;
    }
}