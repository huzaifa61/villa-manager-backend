package com.villamanager.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.villamanager.dto.ApiResponse;
import com.villamanager.dto.RecurringExpenseTemplateRequest;
import com.villamanager.entity.RecurringExpenseTemplate;
import com.villamanager.service.AccessControlService;
import com.villamanager.service.RecurringExpenseService;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/v1/villas/{villaId}/expense-templates")
@CrossOrigin(origins = "*", maxAge = 3600)
public class RecurringExpenseTemplateController {
    @Autowired private RecurringExpenseService recurringExpenseService;
    @Autowired private AccessControlService accessControlService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RecurringExpenseTemplate>>> getTemplates(@PathVariable Long villaId) {
        accessControlService.requireVillaRead(villaId);
        return ResponseEntity.ok(ApiResponse.success("Recurring expense templates retrieved successfully", recurringExpenseService.getTemplates(villaId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RecurringExpenseTemplate>> createTemplate(
            @PathVariable Long villaId,
            @RequestBody RecurringExpenseTemplateRequest request) {
        accessControlService.requireFinancialManage(villaId);
        RecurringExpenseTemplate template = recurringExpenseService.createTemplate(villaId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Recurring expense template created successfully", template));
    }

    @PutMapping("/{templateId}")
    public ResponseEntity<ApiResponse<RecurringExpenseTemplate>> updateTemplate(
            @PathVariable Long villaId,
            @PathVariable Long templateId,
            @RequestBody RecurringExpenseTemplateRequest request) {
        accessControlService.requireFinancialManage(villaId);
        RecurringExpenseTemplate template = recurringExpenseService.updateTemplate(villaId, templateId, request);
        return ResponseEntity.ok(ApiResponse.success("Recurring expense template updated successfully", template));
    }

    @DeleteMapping("/{templateId}")
    public ResponseEntity<ApiResponse<Void>> deleteTemplate(
            @PathVariable Long villaId,
            @PathVariable Long templateId) {
        accessControlService.requireFinancialManage(villaId);
        recurringExpenseService.deleteTemplate(villaId, templateId);
        return ResponseEntity.ok(ApiResponse.success("Recurring expense template deleted successfully", null));
    }

    @PostMapping("/run-due")
    public ResponseEntity<ApiResponse<Integer>> runDueTemplates(@PathVariable Long villaId) {
        accessControlService.requireFinancialManage(villaId);
        int generated = recurringExpenseService.generateDueTemplates(villaId, LocalDate.now());
        return ResponseEntity.ok(ApiResponse.success("Due recurring expenses generated successfully", generated));
    }
}
