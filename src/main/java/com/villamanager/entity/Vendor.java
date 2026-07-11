package com.villamanager.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "vendors")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vendor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String contactPerson;

    private String email;

    private String phoneNumber;

    private String address;

    private String serviceType;

    /** Geographic area / location used to scope vendors to a villa manager's region. */
    private String region;

    @Column(nullable = false)
    private Boolean isActive = true;
}
