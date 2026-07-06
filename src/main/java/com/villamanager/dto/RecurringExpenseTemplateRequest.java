package com.villamanager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecurringExpenseTemplateRequest {
    private String templateName;
    private Long apartmentId;
    private Long categoryId;
    private String description;
    private BigDecimal amount;
    private Integer dayOfMonth;
    private Boolean isActive;
}
