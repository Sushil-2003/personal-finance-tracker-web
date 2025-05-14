package com.sushil.Personal_Finance_Tracker_Web.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.sushil.Personal_Finance_Tracker_Web.dto.TransactionDTO;
import com.sushil.Personal_Finance_Tracker_Web.entity.Transaction;
import com.sushil.Personal_Finance_Tracker_Web.entity.User;
import com.sushil.Personal_Finance_Tracker_Web.repository.TransactionRepository;

@Service
public class TransactionService {

	private final TransactionRepository transactionRepository;
	private final CategoryService categoryService;

	public TransactionService(TransactionRepository transactionRepository, CategoryService categoryService) {
		this.transactionRepository = transactionRepository;
		this.categoryService = categoryService;
	}

	public List<TransactionDTO> getTransactions(User user) {
		return transactionRepository.findByUser(user).stream().map(this::toDTO).collect(Collectors.toList());
	}

	public TransactionDTO createTransaction(User user, TransactionDTO dto) {
		validateTransaction(user, dto);
		Transaction transaction = new Transaction();
		transaction.setUser(user);
		transaction.setType(dto.getType());
		transaction.setCategory(dto.getCategory());
		transaction.setDate(dto.getDate());
		transaction.setAmount(dto.getAmount());
		transaction = transactionRepository.save(transaction);
		return toDTO(transaction);
	}

	public TransactionDTO updateTransaction(User user, Long id, TransactionDTO dto) {
		Transaction transaction = transactionRepository.findById(id).filter(t -> t.getUser().equals(user))
				.orElseThrow(() -> new RuntimeException("Transaction not found"));
		validateTransaction(user, dto);
		transaction.setType(dto.getType());
		transaction.setCategory(dto.getCategory());
		transaction.setDate(dto.getDate());
		transaction.setAmount(dto.getAmount());
		transaction = transactionRepository.save(transaction);
		return toDTO(transaction);
	}

	public void deleteTransaction(User user, Long id) {
		Transaction transaction = transactionRepository.findById(id).filter(t -> t.getUser().equals(user))
				.orElseThrow(() -> new RuntimeException("Transaction not found"));
		transactionRepository.delete(transaction);
	}

	private void validateTransaction(User user, TransactionDTO dto) {
		if (!List.of("income", "expense").contains(dto.getType())) {
			throw new RuntimeException("Invalid transaction type");
		}
		if (dto.getAmount() <= 0) {
			throw new RuntimeException("Amount must be positive");
		}
		if (dto.getDate() == null) {
			throw new RuntimeException("Date is required");
		}
		// Validate category exists
		categoryService.validateCategory(dto.getCategory(), user);
	}

	private TransactionDTO toDTO(Transaction transaction) {
		TransactionDTO dto = new TransactionDTO();
		dto.setId(transaction.getId());
		dto.setType(transaction.getType());
		dto.setCategory(transaction.getCategory());
		dto.setDate(transaction.getDate());
		dto.setAmount(transaction.getAmount());
		return dto;
	}
}