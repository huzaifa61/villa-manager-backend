package com.villamanager.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.villamanager.dto.ApiResponse;
import com.villamanager.dto.ExpenseDto;
import com.villamanager.dto.ExpenseRequest;
import com.villamanager.entity.Expense;
import com.villamanager.repository.ExpenseRepository;
import com.villamanager.repository.ApartmentRepository;
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
        List<ExpenseDto> dtos = expenses.stream().map(e -> ExpenseDto.builder()
            .id(e.getId())
            .villaId(e.getVillaId())
            .apartmentId(e.getApartmentId())
            .categoryId(e.getCategoryId())
            .description(e.getDescription())
            .amount(e.getAmount())
            .expenseDate(e.getExpenseDate() != null ? e.getExpenseDate().toString() : null)
            .isSplit(e.getIsSplit())
            .build()
        ).collect(Collectors.toList());
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

        ExpenseDto dto = ExpenseDto.builder()
            .id(saved.getId())
            .villaId(saved.getVillaId())
            .description(saved.getDescription())
            .amount(saved.getAmount())
            .expenseDate(saved.getExpenseDate().toString())
            .build();

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Expense created successfully", dto));
    }
}
