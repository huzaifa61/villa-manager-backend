package com.villamanager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseDto {
    private Long id;
    private Long villaId;
    private Long apartmentId;
    private String apartmentNumber;
    private Long categoryId;
    private String categoryName;
    private String description;
    private BigDecimal amount;
    private String expenseDate;
    private Boolean isSplit;
}
