package com.villamanager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApartmentRequest {
    // Backend fields
    private String apartmentNumber;
    private String ownerName;
    private String tenantName;
    private String phoneNumber;
    private String email;
    private String status;
    private BigDecimal openingBalance;
    private String apartmentType;

    // Frontend alias fields - map to backend fields
    private String unitNumber;       // maps to apartmentNumber
    private Integer floor;
    private String tenantPhone;      // maps to phoneNumber
    private BigDecimal monthlyRent;  // maps to openingBalance

    public String getEffectiveApartmentNumber() {
        return apartmentNumber != null ? apartmentNumber : unitNumber;
    }

    public String getEffectivePhoneNumber() {
        return phoneNumber != null ? phoneNumber : tenantPhone;
    }

    public BigDecimal getEffectiveBalance() {
        return openingBalance != null ? openingBalance : (monthlyRent != null ? monthlyRent : BigDecimal.ZERO);
    }
}
