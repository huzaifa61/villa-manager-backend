package com.villamanager.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "villas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Villa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "property_type")
    @Enumerated(EnumType.STRING)
    private PropertyType propertyType = PropertyType.VILLA;

    @Column(name = "property_number")
    private String propertyNumber;

    @Column(name = "region")
    private String region;

    @Column(name = "whatsapp_link")
    private String whatsappLink;

    private String location;

    private String description;

    @JsonIgnore
    @Basic(fetch = FetchType.LAZY)
    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Column(name = "logo_data", columnDefinition = "bytea")
    private byte[] logoData;

    @JsonIgnore
    @Column(name = "logo_content_type", length = 100)
    private String logoContentType;

    private Integer totalApartments = 0;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
}
