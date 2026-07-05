package com.villamanager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseRequest {
    private Long apartmentId;
    private Long categoryId;
    private String description;
    private BigDecimal amount;
    private LocalDate expenseDate;
}
