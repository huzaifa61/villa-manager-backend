package com.villamanager.dto;

import com.villamanager.entity.PropertyType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VillaRequest {
    private String name;
    private PropertyType propertyType;
    private String propertyNumber;
    private String region;
    private String whatsappLink;
    private String location;
    private String description;
}
