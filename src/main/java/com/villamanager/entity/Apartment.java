package com.villamanager.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "apartments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Apartment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long villaId;

    @Column(nullable = false)
    private String apartmentNumber;

    private String ownerName;

    private String tenantName;

    private String phoneNumber;

    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApartmentStatus status = ApartmentStatus.ACTIVE;

    @Column(precision = 10, scale = 2)
    private BigDecimal openingBalance = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal currentBalance = BigDecimal.ZERO;

    private String apartmentType;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
}
