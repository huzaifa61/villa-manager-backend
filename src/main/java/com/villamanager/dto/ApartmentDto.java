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
public class ApartmentDto {
    private Long id;
    private Long villaId;
    private String apartmentNumber;
    private String ownerName;
    private String tenantName;
    private String phoneNumber;
    private String email;
    private String status;
    private BigDecimal openingBalance;
    private BigDecimal currentBalance;
    private String apartmentType;
}
