package com.villamanager.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.villamanager.dto.RecurringExpenseTemplateRequest;
import com.villamanager.entity.Apartment;
import com.villamanager.entity.Expense;
import com.villamanager.entity.RecurringExpenseTemplate;
import com.villamanager.exception.InvalidOperationException;
import com.villamanager.exception.ResourceNotFoundException;
import com.villamanager.repository.ApartmentRepository;
import com.villamanager.repository.ExpenseRepository;
import com.villamanager.repository.RecurringExpenseTemplateRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
public class RecurringExpenseService {
    @Autowired private RecurringExpenseTemplateRepository templateRepository;
    @Autowired private ExpenseRepository expenseRepository;
    @Autowired private ApartmentRepository apartmentRepository;

    public List<RecurringExpenseTemplate> getTemplates(Long villaId) {
        return templateRepository.findByVillaId(villaId);
    }

    public RecurringExpenseTemplate createTemplate(Long villaId, RecurringExpenseTemplateRequest request) {
        RecurringExpenseTemplate template = new RecurringExpenseTemplate();
        template.setVillaId(villaId);
        applyRequest(template, request);
        return templateRepository.save(template);
    }

    public RecurringExpenseTemplate updateTemplate(Long villaId, Long templateId, RecurringExpenseTemplateRequest request) {
        RecurringExpenseTemplate template = templateRepository.findById(templateId)
                .filter(t -> t.getVillaId().equals(villaId))
                .orElseThrow(() -> new ResourceNotFoundException("Template not found with id: " + templateId));
        applyRequest(template, request);
        return templateRepository.save(template);
    }

    public void deleteTemplate(Long villaId, Long templateId) {
        RecurringExpenseTemplate template = templateRepository.findById(templateId)
                .filter(t -> t.getVillaId().equals(villaId))
                .orElseThrow(() -> new ResourceNotFoundException("Template not found with id: " + templateId));
        templateRepository.delete(template);
    }

    @Transactional
    public int generateDueTemplates(LocalDate today) {
        int generated = 0;
        for (RecurringExpenseTemplate template : templateRepository.findByIsActiveTrue()) {
            if (generateIfDue(template, today)) {
                generated++;
            }
        }
        return generated;
    }

    @Transactional
    public int generateDueTemplates(Long villaId, LocalDate today) {
        int generated = 0;
        for (RecurringExpenseTemplate template : templateRepository.findByVillaId(villaId)) {
            if (Boolean.TRUE.equals(template.getIsActive()) && generateIfDue(template, today)) {
                generated++;
            }
        }
        return generated;
    }

    @Scheduled(cron = "0 5 0 * * *")
    @Transactional
    public void generateDueTemplatesDaily() {
        generateDueTemplates(LocalDate.now());
    }

    private void applyRequest(RecurringExpenseTemplate template, RecurringExpenseTemplateRequest request) {
        if (request.getTemplateName() == null || request.getTemplateName().trim().isEmpty()) {
            throw new InvalidOperationException("Template name is required");
        }
        if (request.getDescription() == null || request.getDescription().trim().isEmpty()) {
            throw new InvalidOperationException("Description is required");
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOperationException("Amount must be greater than zero");
        }
        int day = request.getDayOfMonth() != null ? request.getDayOfMonth() : 1;
        if (day < 1 || day > 31) {
            throw new InvalidOperationException("Day of month must be between 1 and 31");
        }

        template.setTemplateName(request.getTemplateName().trim());
        template.setDescription(request.getDescription().trim());
        template.setCategoryId(request.getCategoryId() != null ? request.getCategoryId() : 1L);
        template.setApartmentId(request.getApartmentId());
        template.setAmount(request.getAmount());
        template.setDayOfMonth(day);
        template.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        template.setUpdatedAt(LocalDateTime.now());
    }

    private boolean generateIfDue(RecurringExpenseTemplate template, LocalDate today) {
        YearMonth currentMonth = YearMonth.from(today);
        String monthKey = currentMonth.toString();
        if (monthKey.equals(template.getLastGeneratedForMonth())) {
            return false;
        }

        int dueDay = Math.min(template.getDayOfMonth(), currentMonth.lengthOfMonth());
        LocalDate dueDate = currentMonth.atDay(dueDay);
        if (today.isBefore(dueDate)) {
            return false;
        }

        Expense expense = new Expense();
        expense.setVillaId(template.getVillaId());
        expense.setApartmentId(template.getApartmentId());
        expense.setCategoryId(template.getCategoryId());
        expense.setDescription(template.getDescription());
        expense.setAmount(template.getAmount());
        expense.setExpenseDate(dueDate);
        expense.setIsSplit(false);
        expense.setCreatedAt(LocalDateTime.now());
        expense.setUpdatedAt(LocalDateTime.now());

        Expense saved = expenseRepository.save(expense);
        applyExpenseAllocation(saved, true);
        template.setLastGeneratedForMonth(monthKey);
        template.setUpdatedAt(LocalDateTime.now());
        templateRepository.save(template);
        return true;
    }

    private void applyExpenseAllocation(Expense expense, boolean add) {
        BigDecimal amount = expense.getAmount() != null ? expense.getAmount() : BigDecimal.ZERO;
        if (!add) {
            amount = amount.negate();
        }

        if (expense.getApartmentId() != null) {
            BigDecimal finalAmount = amount;
            apartmentRepository.findById(expense.getApartmentId()).ifPresent(apartment -> {
                BigDecimal current = apartment.getCurrentBalance() != null ? apartment.getCurrentBalance() : BigDecimal.ZERO;
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

        BigDecimal share = amount.divide(BigDecimal.valueOf(apartments.size()), 2, RoundingMode.HALF_UP);
        for (Apartment apartment : apartments) {
            BigDecimal current = apartment.getCurrentBalance() != null ? apartment.getCurrentBalance() : BigDecimal.ZERO;
            apartment.setCurrentBalance(current.add(share));
            apartment.setUpdatedAt(LocalDateTime.now());
        }
        apartmentRepository.saveAll(apartments);
    }
}
