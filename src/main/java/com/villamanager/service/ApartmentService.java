package com.villamanager.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.villamanager.dto.ApartmentDto;
import com.villamanager.dto.ApartmentRequest;
import com.villamanager.entity.Apartment;
import com.villamanager.entity.ApartmentStatus;
import com.villamanager.entity.Expense;
import com.villamanager.entity.Payment;
import com.villamanager.entity.PaymentStatus;
import com.villamanager.exception.ResourceNotFoundException;
import com.villamanager.repository.ApartmentRepository;
import com.villamanager.repository.ExpenseRepository;
import com.villamanager.repository.PaymentRepository;
import com.villamanager.repository.VillaRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApartmentService {

    @Autowired
    private ApartmentRepository apartmentRepository;

    @Autowired
    private VillaRepository villaRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    public List<ApartmentDto> getApartments(Long villaId) {
        // Auto-create villa 1 if it doesn't exist
        ensureVillaExists(villaId);
        List<Apartment> apartments = apartmentRepository.findByVillaId(villaId);
        recalculateBalances(villaId, apartments);
        return apartments.stream()
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

    public ApartmentDto updateApartment(Long villaId, Long apartmentId, ApartmentRequest request) {
        ensureVillaExists(villaId);
        Apartment apartment = apartmentRepository.findById(apartmentId)
            .filter(a -> a.getVillaId().equals(villaId))
            .orElseThrow(() -> new ResourceNotFoundException("Apartment not found with id: " + apartmentId));

        String aptNumber = request.getEffectiveApartmentNumber();
        if (aptNumber != null && !aptNumber.isBlank()) {
            apartment.setApartmentNumber(aptNumber);
        }
        apartment.setOwnerName(request.getOwnerName());
        apartment.setTenantName(request.getTenantName());
        apartment.setPhoneNumber(request.getEffectivePhoneNumber());
        apartment.setEmail(request.getEmail());
        apartment.setApartmentType(request.getApartmentType());

        if (request.getStatus() != null) {
            try {
                apartment.setStatus(ApartmentStatus.valueOf(request.getStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                apartment.setStatus(ApartmentStatus.ACTIVE);
            }
        }

        if (request.getOpeningBalance() != null || request.getMonthlyRent() != null) {
            BigDecimal oldOpening = apartment.getOpeningBalance() != null ? apartment.getOpeningBalance() : BigDecimal.ZERO;
            BigDecimal oldCurrent = apartment.getCurrentBalance() != null ? apartment.getCurrentBalance() : BigDecimal.ZERO;
            BigDecimal newOpening = request.getEffectiveBalance();
            apartment.setOpeningBalance(newOpening);
            apartment.setCurrentBalance(oldCurrent.subtract(oldOpening).add(newOpening));
        }

        apartment.setUpdatedAt(LocalDateTime.now());
        return mapToDto(apartmentRepository.save(apartment));
    }

    public void deleteApartment(Long villaId, Long apartmentId) {
        ensureVillaExists(villaId);
        Apartment apartment = apartmentRepository.findById(apartmentId)
            .filter(a -> a.getVillaId().equals(villaId))
            .orElseThrow(() -> new ResourceNotFoundException("Apartment not found with id: " + apartmentId));
        apartmentRepository.delete(apartment);
    }

    private void ensureVillaExists(Long villaId) {
        if (!villaRepository.existsById(villaId)) {
            throw new ResourceNotFoundException("Villa not found with id: " + villaId);
        }
    }

    public ApartmentDto mapToDto(Apartment apartment) {
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

    private void recalculateBalances(Long villaId, List<Apartment> apartments) {
        if (apartments.isEmpty()) {
            return;
        }

        List<Expense> expenses = expenseRepository.findByVillaId(villaId);
        List<Payment> payments = paymentRepository.findByVillaId(villaId);
        BigDecimal globalExpenseTotal = expenses.stream()
            .filter(e -> e.getApartmentId() == null)
            .map(e -> e.getAmount() != null ? e.getAmount() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal globalShare = globalExpenseTotal.divide(BigDecimal.valueOf(apartments.size()), 2, RoundingMode.HALF_UP);

        for (Apartment apartment : apartments) {
            BigDecimal opening = apartment.getOpeningBalance() != null ? apartment.getOpeningBalance() : BigDecimal.ZERO;
            BigDecimal directExpenses = expenses.stream()
                .filter(e -> apartment.getId().equals(e.getApartmentId()))
                .map(e -> e.getAmount() != null ? e.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal paid = payments.stream()
                .filter(p -> apartment.getId().equals(p.getApartmentId()))
                .filter(p -> p.getStatus() == PaymentStatus.COMPLETED || p.getStatus() == PaymentStatus.PAID)
                .map(p -> p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            apartment.setCurrentBalance(opening.add(globalShare).add(directExpenses).subtract(paid));
            apartment.setUpdatedAt(LocalDateTime.now());
        }

        apartmentRepository.saveAll(apartments);
    }
}
