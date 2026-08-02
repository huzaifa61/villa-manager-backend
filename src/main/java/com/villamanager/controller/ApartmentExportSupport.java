package com.villamanager.controller;

import com.villamanager.dto.ApartmentDto;
import com.villamanager.entity.Apartment;
import com.villamanager.entity.Expense;
import com.villamanager.entity.Payment;
import com.villamanager.repository.ApartmentRepository;
import com.villamanager.repository.ExpenseRepository;
import com.villamanager.repository.PaymentRepository;
import com.villamanager.service.ApartmentService;
import com.villamanager.service.ApartmentService.ApartmentFinance;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
class ApartmentExportSupport {

    private final ApartmentRepository apartmentRepository;
    private final ExpenseRepository expenseRepository;
    private final PaymentRepository paymentRepository;
    private final ApartmentService apartmentService;

    List<ApartmentDto> apartmentsForExport(Long villaId) {
        return apartmentService.getApartments(villaId);
    }

    List<List<Object>> buildExportRows(Long villaId, List<ApartmentDto> apartments) {
        List<Apartment> entities = apartmentRepository.findByVillaId(villaId);
        List<Expense> expenses = expenseRepository.findByVillaId(villaId);
        List<Payment> payments = paymentRepository.findByVillaId(villaId);
        int apartmentCount = entities.size();

        List<List<Object>> rows = new ArrayList<>();
        for (ApartmentDto dto : apartments) {
            Apartment entity = entities.stream()
                .filter(a -> a.getId().equals(dto.getId()))
                .findFirst()
                .orElse(null);
            ApartmentFinance finance = entity != null
                ? apartmentService.computeFinance(entity, apartmentCount, expenses, payments)
                : new ApartmentFinance(BigDecimal.ZERO, BigDecimal.ZERO, dto.getCurrentBalance());
            rows.add(List.of(
                dto.getId(),
                dto.getApartmentNumber(),
                dto.getOwnerName(),
                dto.getTenantName(),
                dto.getPhoneNumber(),
                dto.getStatus(),
                finance.allocated(),
                finance.paid(),
                finance.balance(),
                dto.getApartmentType()
            ));
        }
        return rows;
    }

    static List<String> exportHeaders() {
        return List.of(
            "ID", "Apartment", "Owner", "Tenant", "Phone", "Status",
            "Allocated", "Paid", "Balance", "Type"
        );
    }

    List<List<Object>> buildFullCsvRows(Long villaId, List<ApartmentDto> apartments) {
        List<Apartment> entities = apartmentRepository.findByVillaId(villaId);
        List<Expense> expenses = expenseRepository.findByVillaId(villaId);
        List<Payment> payments = paymentRepository.findByVillaId(villaId);
        int apartmentCount = entities.size();

        List<List<Object>> rows = new ArrayList<>();
        for (ApartmentDto dto : apartments) {
            Apartment entity = entities.stream()
                .filter(a -> a.getId().equals(dto.getId()))
                .findFirst()
                .orElse(null);
            ApartmentFinance finance = entity != null
                ? apartmentService.computeFinance(entity, apartmentCount, expenses, payments)
                : new ApartmentFinance(BigDecimal.ZERO, BigDecimal.ZERO, dto.getCurrentBalance());
            rows.add(List.of(
                dto.getId(),
                dto.getApartmentNumber(),
                dto.getOwnerName(),
                dto.getTenantName(),
                dto.getPhoneNumber(),
                dto.getEmail(),
                dto.getStatus(),
                dto.getOpeningBalance(),
                finance.allocated(),
                finance.paid(),
                finance.balance(),
                dto.getApartmentType()
            ));
        }
        return rows;
    }

    static List<String> fullCsvHeaders() {
        return List.of(
            "ID", "Apartment", "Owner", "Tenant", "Phone", "Email", "Status",
            "Opening Balance", "Allocated", "Paid", "Balance", "Type"
        );
    }
}
