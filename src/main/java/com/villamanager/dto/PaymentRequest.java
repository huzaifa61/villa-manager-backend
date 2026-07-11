package com.villamanager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    private Long apartmentId;
    private Long categoryId;
    private BigDecimal amount;
    private LocalDate paymentDate;
    private String paymentMethod;
    private String referenceNumber;
    private String status;
    private String notes;

    // Split type: SINGLE, ALL_EQUAL, SELECTED_EQUAL, SELECTED_CUSTOM
    private String splitType;

    // For SELECTED_EQUAL and SELECTED_CUSTOM
    private List<Long> selectedApartmentIds;

    // For SELECTED_CUSTOM: map of apartmentId -> amount
    private Map<String, BigDecimal> customAmounts;
}
