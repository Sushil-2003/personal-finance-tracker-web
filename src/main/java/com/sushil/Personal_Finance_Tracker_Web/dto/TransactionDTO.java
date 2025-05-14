package com.sushil.Personal_Finance_Tracker_Web.dto;

import java.time.LocalDate;
import lombok.Data;

@Data
public class TransactionDTO {
    private Long id;
    private String type;
    private String category;
    private LocalDate date;
    private double amount;
	
}