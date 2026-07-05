package com.villamanager.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.villamanager.dto.ApiResponse;
import com.villamanager.dto.ExpenseDto;
import com.villamanager.dto.ExpenseRequest;
import com.villamanager.entity.Expense;
import com.villamanager.entity.Apartment;
import com.villamanager.exception.ResourceNotFoundException;
import com.villamanager.repository.ExpenseRepository;
import com.villamanager.repository.ApartmentRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/villas/{villaId}/expenses")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ExpenseController {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private ApartmentRepository apartmentRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ExpenseDto>>> getExpenses(@PathVariable Long villaId) {
        List<Expense> expenses = expenseRepository.findByVillaId(villaId);
        List<ExpenseDto> dtos = expenses.stream().map(this::mapToDto).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Expenses retrieved successfully", dtos));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ExpenseDto>> createExpense(
            @PathVariable Long villaId,
            @RequestBody ExpenseRequest request) {

        Expense expense = new Expense();
        expense.setVillaId(villaId);
        expense.setApartmentId(request.getApartmentId());
        expense.setCategoryId(request.getCategoryId() != null ? request.getCategoryId() : 1L);
        expense.setDescription(request.getDescription());
        expense.setAmount(request.getAmount());
        expense.setExpenseDate(request.getExpenseDate() != null ? request.getExpenseDate() : LocalDate.now());
        expense.setIsSplit(false);
        expense.setCreatedAt(LocalDateTime.now());
        expense.setUpdatedAt(LocalDateTime.now());

        Expense saved = expenseRepository.save(expense);
        applyExpenseAllocation(saved, true);

        ExpenseDto dto = mapToDto(saved);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Expense created successfully", dto));
    }

    @PutMapping("/{expenseId}")
    public ResponseEntity<ApiResponse<ExpenseDto>> updateExpense(
            @PathVariable Long villaId,
            @PathVariable Long expenseId,
            @RequestBody ExpenseRequest request) {

        Expense expense = expenseRepository.findById(expenseId)
            .filter(e -> e.getVillaId().equals(villaId))
            .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + expenseId));

        applyExpenseAllocation(expense, false);
        expense.setApartmentId(request.getApartmentId());
        expense.setCategoryId(request.getCategoryId() != null ? request.getCategoryId() : expense.getCategoryId());
        expense.setDescription(request.getDescription());
        expense.setAmount(request.getAmount());
        expense.setExpenseDate(request.getExpenseDate() != null ? request.getExpenseDate() : LocalDate.now());
        expense.setUpdatedAt(LocalDateTime.now());

        Expense saved = expenseRepository.save(expense);
        applyExpenseAllocation(saved, true);

        return ResponseEntity.ok(ApiResponse.success("Expense updated successfully", mapToDto(saved)));
    }

    @DeleteMapping("/{expenseId}")
    public ResponseEntity<ApiResponse<Void>> deleteExpense(
            @PathVariable Long villaId,
            @PathVariable Long expenseId) {
        Expense expense = expenseRepository.findById(expenseId)
            .filter(e -> e.getVillaId().equals(villaId))
            .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + expenseId));
        applyExpenseAllocation(expense, false);
        expenseRepository.delete(expense);
        return ResponseEntity.ok(ApiResponse.success("Expense deleted successfully", null));
    }

    private ExpenseDto mapToDto(Expense e) {
        String apartmentNumber = null;
        if (e.getApartmentId() != null) {
            apartmentNumber = apartmentRepository.findById(e.getApartmentId())
                .map(Apartment::getApartmentNumber)
                .orElse(null);
        }
        return ExpenseDto.builder()
            .id(e.getId())
            .villaId(e.getVillaId())
            .apartmentId(e.getApartmentId())
            .apartmentNumber(apartmentNumber)
            .categoryId(e.getCategoryId())
            .description(e.getDescription())
            .amount(e.getAmount())
            .expenseDate(e.getExpenseDate() != null ? e.getExpenseDate().toString() : null)
            .isSplit(e.getIsSplit())
            .build();
    }

    private void applyExpenseAllocation(Expense expense, boolean add) {

        BigDecimal amount = expense.getAmount() != null
                ? expense.getAmount()
                : BigDecimal.ZERO;

        if (!add) {
            amount = amount.negate();
        }

        if (expense.getApartmentId() != null) {
            BigDecimal finalAmount = amount;

            apartmentRepository.findById(expense.getApartmentId()).ifPresent(apartment -> {
                BigDecimal current = apartment.getCurrentBalance() != null
                        ? apartment.getCurrentBalance()
                        : BigDecimal.ZERO;

                apartment.setCurrentBalance(current.add(finalAmount));
                apartment.setUpdatedAt(LocalDateTime.now());

                apartmentRepository.save(apartment);
            });

            return;
        }

        List<Apartment> apartments = apartmentRepository.findByVillaId(expense.getVillaId());

        if (apartments.isEmpty()) {
            return;
        }

        BigDecimal share = amount.divide(
                BigDecimal.valueOf(apartments.size()),
                2,
                RoundingMode.HALF_UP);

        for (Apartment apartment : apartments) {
            BigDecimal current = apartment.getCurrentBalance() != null
                    ? apartment.getCurrentBalance()
                    : BigDecimal.ZERO;

            apartment.setCurrentBalance(current.add(share));
            apartment.setUpdatedAt(LocalDateTime.now());
        }

        apartmentRepository.saveAll(apartments);
    }
}
