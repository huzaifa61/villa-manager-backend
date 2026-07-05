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
public class PaymentDto {
    private Long id;
    private Long villaId;
    private Long apartmentId;
    private String apartmentNumber;
    private Long categoryId;
    private String categoryName;
    private BigDecimal amount;
    private String paymentDate;
    private String paymentMethod;
    private String referenceNumber;
    private String status;
    private String notes;
    private Boolean isSplit;
}
