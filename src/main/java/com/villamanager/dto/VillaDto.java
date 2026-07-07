package com.villamanager.dto;

import com.villamanager.entity.PropertyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VillaDto {
    private Long id;
    private String name;
    private PropertyType propertyType;
    private String propertyNumber;
    private String region;
    private String whatsappLink;
    private String location;
    private String description;
    private Integer totalApartments;
    private Boolean isActive;
}
