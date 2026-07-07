package com.villamanager.controller;

import com.villamanager.dto.ApiResponse;
import com.villamanager.entity.Apartment;
import com.villamanager.entity.Villa;
import com.villamanager.exception.ResourceNotFoundException;
import com.villamanager.repository.ApartmentRepository;
import com.villamanager.repository.ExpenseRepository;
import com.villamanager.repository.PaymentRepository;
import com.villamanager.repository.RecurringExpenseTemplateRepository;
import com.villamanager.repository.ServiceRequestRepository;
import com.villamanager.repository.UserRepository;
import com.villamanager.repository.VillaRepository;
import com.villamanager.service.AccessControlService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/v1/villas/{villaId}")
@CrossOrigin(origins = "*", maxAge = 3600)
public class VillaControlController {

    @Autowired
    private VillaRepository villaRepository;

    @Autowired
    private ApartmentRepository apartmentRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    @Autowired
    private RecurringExpenseTemplateRepository recurringExpenseTemplateRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccessControlService accessControlService;

    @GetMapping
    public ResponseEntity<ApiResponse<Villa>> getVilla(@PathVariable Long villaId) {
        accessControlService.requireVillaRead(villaId);
        return ResponseEntity.ok(ApiResponse.success("Villa retrieved successfully", getVillaOrThrow(villaId)));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<Villa>> updateVilla(
            @PathVariable Long villaId,
            @RequestBody Map<String, Object> body) {
        accessControlService.requireVillaManage(villaId);
        Villa villa = getVillaOrThrow(villaId);
        villa.setName(text(body.get("name"), villa.getName()));
        villa.setLocation(text(body.get("location"), villa.getLocation()));
        villa.setDescription(text(body.get("description"), villa.getDescription()));
        if (body.get("propertyNumber") != null) villa.setPropertyNumber(text(body.get("propertyNumber"), villa.getPropertyNumber()));
        if (body.get("region") != null) villa.setRegion(text(body.get("region"), villa.getRegion()));
        if (body.get("whatsappLink") != null) villa.setWhatsappLink(text(body.get("whatsappLink"), villa.getWhatsappLink()));
        if (body.get("propertyType") != null) {
            try { villa.setPropertyType(com.villamanager.entity.PropertyType.valueOf(String.valueOf(body.get("propertyType")))); } catch (Exception ignored) {}
        }
        villa.setUpdatedAt(LocalDateTime.now());
        return ResponseEntity.ok(ApiResponse.success("Villa updated successfully", villaRepository.save(villa)));
    }

    @Transactional
    @PostMapping("/reset-data")
    public ResponseEntity<ApiResponse<Void>> resetVillaData(@PathVariable Long villaId) {
        accessControlService.requireVillaManage(villaId);
        getVillaOrThrow(villaId);
        recurringExpenseTemplateRepository.deleteByVillaId(villaId);
        paymentRepository.deleteByVillaId(villaId);
        expenseRepository.deleteByVillaId(villaId);
        serviceRequestRepository.deleteByVillaId(villaId);
        for (Apartment apartment : apartmentRepository.findByVillaId(villaId)) {
            apartment.setCurrentBalance(apartment.getOpeningBalance() != null ? apartment.getOpeningBalance() : BigDecimal.ZERO);
            apartment.setUpdatedAt(LocalDateTime.now());
            apartmentRepository.save(apartment);
        }
        return ResponseEntity.ok(ApiResponse.success("Villa data reset successfully", null));
    }

    @Transactional
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteVilla(@PathVariable Long villaId) {
        accessControlService.requireGeneralManager();
        Villa villa = getVillaOrThrow(villaId);
        recurringExpenseTemplateRepository.deleteByVillaId(villaId);
        paymentRepository.deleteByVillaId(villaId);
        expenseRepository.deleteByVillaId(villaId);
        serviceRequestRepository.deleteByVillaId(villaId);
        apartmentRepository.deleteByVillaId(villaId);
        userRepository.findByVillaId(villaId).forEach(user -> {
            user.setVillaId(null);
            user.setIsActive(false);
            userRepository.save(user);
        });
        villaRepository.delete(villa);
        return ResponseEntity.ok(ApiResponse.success("Villa deleted successfully", null));
    }

    private Villa getVillaOrThrow(Long villaId) {
        return villaRepository.findById(villaId)
                .orElseThrow(() -> new ResourceNotFoundException("Villa not found"));
    }

    private String text(Object value, String fallback) {
        String text = value == null ? null : String.valueOf(value).trim();
        return text == null || text.isBlank() ? fallback : text;
    }
}
