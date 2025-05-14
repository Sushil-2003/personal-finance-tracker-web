package com.sushil.Personal_Finance_Tracker_Web.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sushil.Personal_Finance_Tracker_Web.dto.TransactionDTO;
import com.sushil.Personal_Finance_Tracker_Web.entity.User;
import com.sushil.Personal_Finance_Tracker_Web.service.TransactionService;
import com.sushil.Personal_Finance_Tracker_Web.service.UserService;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final UserService userService;

    public TransactionController(TransactionService transactionService, UserService userService) {
        this.transactionService = transactionService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<TransactionDTO>> getTransactions() {
        User user = getCurrentUser();
        return ResponseEntity.ok(transactionService.getTransactions(user));
    }

    @PostMapping
    public ResponseEntity<TransactionDTO> createTransaction(@RequestBody TransactionDTO dto) {
        User user = getCurrentUser();
        return ResponseEntity.ok(transactionService.createTransaction(user, dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionDTO> updateTransaction(@PathVariable Long id, @RequestBody TransactionDTO dto) {
        User user = getCurrentUser();
        return ResponseEntity.ok(transactionService.updateTransaction(user, id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        User user = getCurrentUser();
        transactionService.deleteTransaction(user, id);
        return ResponseEntity.noContent().build();
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.findByEmail(email);
    }
}