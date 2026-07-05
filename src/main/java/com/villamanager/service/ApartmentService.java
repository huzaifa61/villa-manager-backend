package com.villamanager.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.villamanager.dto.ApartmentDto;
import com.villamanager.dto.ApartmentRequest;
import com.villamanager.entity.Apartment;
import com.villamanager.entity.ApartmentStatus;
import com.villamanager.exception.ResourceNotFoundException;
import com.villamanager.repository.ApartmentRepository;
import com.villamanager.repository.VillaRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApartmentService {

    @Autowired
    private ApartmentRepository apartmentRepository;

    @Autowired
    private VillaRepository villaRepository;

    public List<ApartmentDto> getApartments(Long villaId) {
        // Auto-create villa 1 if it doesn't exist
        ensureVillaExists(villaId);
        return apartmentRepository.findByVillaId(villaId)
            .stream()
            .map(this::mapToDto)
            .collect(Collectors.toList());
    }

    public ApartmentDto createApartment(Long villaId, ApartmentRequest request) {
        ensureVillaExists(villaId);

        String aptNumber = request.getEffectiveApartmentNumber();
        if (aptNumber == null || aptNumber.isBlank()) {
            aptNumber = "APT-" + System.currentTimeMillis();
        }

        String statusStr = request.getStatus();
        ApartmentStatus status;
        try {
            status = (statusStr != null) ? ApartmentStatus.valueOf(statusStr.toUpperCase()) : ApartmentStatus.ACTIVE;
        } catch (IllegalArgumentException e) {
            status = ApartmentStatus.ACTIVE;
        }

        Apartment apartment = new Apartment();
        apartment.setVillaId(villaId);
        apartment.setApartmentNumber(aptNumber);
        apartment.setOwnerName(request.getOwnerName());
        apartment.setTenantName(request.getTenantName());
        apartment.setPhoneNumber(request.getEffectivePhoneNumber());
        apartment.setEmail(request.getEmail());
        apartment.setStatus(status);
        apartment.setOpeningBalance(request.getEffectiveBalance());
        apartment.setCurrentBalance(request.getEffectiveBalance());
        apartment.setApartmentType(request.getApartmentType());
        apartment.setCreatedAt(LocalDateTime.now());
        apartment.setUpdatedAt(LocalDateTime.now());

        Apartment saved = apartmentRepository.save(apartment);
        return mapToDto(saved);
    }

    private void ensureVillaExists(Long villaId) {
        if (!villaRepository.existsById(villaId)) {
            throw new ResourceNotFoundException("Villa not found with id: " + villaId);
        }
    }

    private ApartmentDto mapToDto(Apartment apartment) {
        return ApartmentDto.builder()
            .id(apartment.getId())
            .villaId(apartment.getVillaId())
            .apartmentNumber(apartment.getApartmentNumber())
            .ownerName(apartment.getOwnerName())
            .tenantName(apartment.getTenantName())
            .phoneNumber(apartment.getPhoneNumber())
            .email(apartment.getEmail())
            .status(apartment.getStatus().toString())
            .openingBalance(apartment.getOpeningBalance())
            .currentBalance(apartment.getCurrentBalance())
            .apartmentType(apartment.getApartmentType())
            .build();
    }
}
