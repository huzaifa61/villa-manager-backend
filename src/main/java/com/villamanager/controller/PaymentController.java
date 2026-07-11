package com.villamanager.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.villamanager.dto.ApiResponse;
import com.villamanager.dto.PaymentDto;
import com.villamanager.dto.PaymentRequest;
import com.villamanager.entity.Apartment;
import com.villamanager.entity.Payment;
import com.villamanager.entity.PaymentStatus;
import com.villamanager.exception.ResourceNotFoundException;
import com.villamanager.repository.PaymentRepository;
import com.villamanager.repository.ApartmentRepository;
import com.villamanager.service.AccessControlService;
import com.villamanager.service.ExportService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/villas/{villaId}/payments")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PaymentController {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ApartmentRepository apartmentRepository;

    @Autowired
    private AccessControlService accessControlService;

    @Autowired
    private ExportService exportService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PaymentDto>>> getPayments(@PathVariable Long villaId) {
        accessControlService.requireVillaRead(villaId);
        List<Payment> payments = paymentRepository.findByVillaId(villaId);
        List<PaymentDto> dtos = payments.stream().map(this::mapToDto).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Payments retrieved successfully", dtos));
    }

    @GetMapping(value = "/export", produces = "text/csv")
    public ResponseEntity<byte[]> exportPayments(@PathVariable Long villaId) {
        accessControlService.requireVillaRead(villaId);
        List<PaymentDto> payments = paymentRepository.findByVillaId(villaId)
                .stream().map(this::mapToDto).collect(Collectors.toList());
        List<String> headers = exportService.withVillaColumn(Arrays.asList("ID", "Apartment", "Amount", "Payment Date", "Method", "Reference", "Status", "Notes"));
        List<List<Object>> rows = new ArrayList<>();
        for (PaymentDto p : payments) {
            List<Object> row = new ArrayList<>();
            row.add(p.getId()); row.add(p.getApartmentNumber() != null ? p.getApartmentNumber() : "All apartments");
            row.add(p.getAmount()); row.add(p.getPaymentDate());
            row.add(p.getPaymentMethod()); row.add(p.getReferenceNumber());
            row.add(p.getStatus()); row.add(p.getNotes());
            rows.add(row);
        }
        return exportService.exportToCSV("payments", villaId, "Payments Report", headers, exportService.withVillaColumn(villaId, rows));
    }

    @GetMapping(value = "/export-excel")
    public ResponseEntity<byte[]> exportPaymentsExcel(@PathVariable Long villaId) {
        accessControlService.requireVillaRead(villaId);
        List<PaymentDto> payments = paymentRepository.findByVillaId(villaId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        List<String> headers = exportService.withVillaColumn(Arrays.asList("ID", "Apartment", "Amount", "Payment Date", "Method", "Status"));
        List<List<Object>> rows = new ArrayList<>();
        for (PaymentDto p : payments) {
            List<Object> row = new ArrayList<>();
            row.add(p.getId());
            row.add(p.getApartmentNumber() != null ? p.getApartmentNumber() : "All apartments");
            row.add(p.getAmount());
            row.add(p.getPaymentDate());
            row.add(p.getPaymentMethod());
            row.add(p.getStatus());
            rows.add(row);
        }

        return exportService.exportToExcel("payments", villaId, "Payments", headers, exportService.withVillaColumn(villaId, rows));
    }

    @GetMapping(value = "/export-pdf")
    public ResponseEntity<byte[]> exportPaymentsPdf(@PathVariable Long villaId) {
        accessControlService.requireVillaRead(villaId);
        List<PaymentDto> payments = paymentRepository.findByVillaId(villaId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        List<String> headers = exportService.withVillaColumn(Arrays.asList("ID", "Apartment", "Amount", "Payment Date", "Method", "Status"));
        List<List<Object>> rows = new ArrayList<>();
        for (PaymentDto p : payments) {
            List<Object> row = new ArrayList<>();
            row.add(p.getId());
            row.add(p.getApartmentNumber() != null ? p.getApartmentNumber() : "All apartments");
            row.add(p.getAmount());
            row.add(p.getPaymentDate());
            row.add(p.getPaymentMethod());
            row.add(p.getStatus());
            rows.add(row);
        }

        return exportService.exportToPdf("payments", villaId, "Payments Report", headers, exportService.withVillaColumn(villaId, rows));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentDto>> createPayment(
            @PathVariable Long villaId,
            @RequestBody PaymentRequest request) {
        accessControlService.requireFinancialManage(villaId);
        PaymentDto dto = createPaymentWithSplit(villaId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment created successfully", dto));
    }

    @PostMapping("/apartment/{apartmentId}")
    public ResponseEntity<ApiResponse<PaymentDto>> createPaymentForApartment(
            @PathVariable Long villaId,
            @PathVariable Long apartmentId,
            @RequestBody PaymentRequest request) {
        accessControlService.requireFinancialManage(villaId);
        request.setApartmentId(apartmentId);
        if (request.getSplitType() == null || request.getSplitType().isBlank()) {
            request.setSplitType("SINGLE");
        }
        PaymentDto dto = createPaymentWithSplit(villaId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment created successfully", dto));
    }

    private PaymentDto createPaymentWithSplit(Long villaId, PaymentRequest request) {
        String splitType = request.getSplitType() != null ? request.getSplitType() : "SINGLE";

        if ("SELECTED_CUSTOM".equals(splitType) && request.getCustomAmounts() != null && !request.getCustomAmounts().isEmpty()) {
            Payment lastSaved = null;
            for (Map.Entry<String, BigDecimal> entry : request.getCustomAmounts().entrySet()) {
                Long aptId = Long.parseLong(entry.getKey());
                BigDecimal amt = entry.getValue();
                if (amt == null || amt.compareTo(BigDecimal.ZERO) <= 0) continue;
                Payment payment = buildPayment(villaId, request, aptId, amt, false);
                lastSaved = paymentRepository.save(payment);
                applyToApartment(aptId, amt, true);
            }
            return lastSaved != null ? mapToDto(lastSaved) : null;
        }

        if ("SELECTED_EQUAL".equals(splitType) && request.getSelectedApartmentIds() != null && !request.getSelectedApartmentIds().isEmpty()) {
            int count = request.getSelectedApartmentIds().size();
            BigDecimal share = request.getAmount().divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
            Payment lastSaved = null;
            for (Long aptId : request.getSelectedApartmentIds()) {
                Payment payment = buildPayment(villaId, request, aptId, share, false);
                lastSaved = paymentRepository.save(payment);
                applyToApartment(aptId, share, true);
            }
            return lastSaved != null ? mapToDto(lastSaved) : null;
        }

        Payment payment = buildPayment(villaId, request, request.getApartmentId(), request.getAmount(), request.getApartmentId() == null);
        Payment saved = paymentRepository.save(payment);
        applyPaymentAllocation(saved, true);
        return mapToDto(saved);
    }

    private Payment buildPayment(Long villaId, PaymentRequest request, Long apartmentId, BigDecimal amount, boolean isSplit) {
        Payment payment = new Payment();
        payment.setVillaId(villaId);
        payment.setApartmentId(apartmentId);
        payment.setCategoryId(request.getCategoryId() != null ? request.getCategoryId() : 1L);
        payment.setAmount(amount);
        payment.setPaymentDate(request.getPaymentDate() != null ? request.getPaymentDate() : LocalDate.now());
        payment.setPaymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "CASH");
        payment.setReferenceNumber(request.getReferenceNumber());
        payment.setStatus(parseStatus(request.getStatus()));
        payment.setNotes(request.getNotes());
        payment.setIsSplit(isSplit);
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());
        return payment;
    }

    @PutMapping("/{paymentId}")
    public ResponseEntity<ApiResponse<PaymentDto>> updatePayment(
            @PathVariable Long villaId,
            @PathVariable Long paymentId,
            @RequestBody PaymentRequest request) {
        accessControlService.requireFinancialManage(villaId);

        Payment payment = paymentRepository.findById(paymentId)
            .filter(p -> p.getVillaId().equals(villaId))
            .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));

        applyPaymentAllocation(payment, false);
        payment.setCategoryId(request.getCategoryId() != null ? request.getCategoryId() : payment.getCategoryId());
        payment.setAmount(request.getAmount());
        payment.setPaymentDate(request.getPaymentDate() != null ? request.getPaymentDate() : LocalDate.now());
        payment.setPaymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : payment.getPaymentMethod());
        payment.setReferenceNumber(request.getReferenceNumber());
        payment.setStatus(parseStatus(request.getStatus()));
        payment.setNotes(request.getNotes());
        payment.setUpdatedAt(LocalDateTime.now());

        Payment saved = paymentRepository.save(payment);
        applyPaymentAllocation(saved, true);

        return ResponseEntity.ok(ApiResponse.success("Payment updated successfully", mapToDto(saved)));
    }

    @DeleteMapping("/{paymentId}")
    public ResponseEntity<ApiResponse<Void>> deletePayment(
            @PathVariable Long villaId,
            @PathVariable Long paymentId) {
        accessControlService.requireFinancialManage(villaId);
        Payment payment = paymentRepository.findById(paymentId)
            .filter(p -> p.getVillaId().equals(villaId))
            .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));
        applyPaymentAllocation(payment, false);
        paymentRepository.delete(payment);
        return ResponseEntity.ok(ApiResponse.success("Payment deleted successfully", null));
    }

    private PaymentStatus parseStatus(String status) {
        try {
            String reqStatus = status != null ? status : "COMPLETED";
            if (reqStatus.equals("PAID")) reqStatus = "COMPLETED";
            return PaymentStatus.valueOf(reqStatus);
        } catch (IllegalArgumentException e) {
            return PaymentStatus.COMPLETED;
        }
    }

    private PaymentDto mapToDto(Payment p) {
        String aptNumber = null;
        if (p.getApartmentId() != null) {
            aptNumber = apartmentRepository.findById(p.getApartmentId())
                    .map(Apartment::getApartmentNumber)
                    .orElse("N/A");
        }
        return PaymentDto.builder()
            .id(p.getId())
            .villaId(p.getVillaId())
            .apartmentId(p.getApartmentId())
            .apartmentNumber(aptNumber)
            .amount(p.getAmount())
            .paymentDate(p.getPaymentDate() != null ? p.getPaymentDate().toString() : null)
            .paymentMethod(p.getPaymentMethod())
            .referenceNumber(p.getReferenceNumber())
            .status(p.getStatus().toString())
            .notes(p.getNotes())
            .isSplit(p.getIsSplit())
            .build();
    }

    private void applyToApartment(Long apartmentId, BigDecimal amount, boolean addPayment) {
        apartmentRepository.findById(apartmentId).ifPresent(apartment -> {
            BigDecimal current = apartment.getCurrentBalance() != null ? apartment.getCurrentBalance() : BigDecimal.ZERO;
            apartment.setCurrentBalance(addPayment ? current.subtract(amount) : current.add(amount));
            apartment.setUpdatedAt(LocalDateTime.now());
            apartmentRepository.save(apartment);
        });
    }

    private void applyPaymentAllocation(Payment payment, boolean addPayment) {
        BigDecimal amount = payment.getAmount() != null ? payment.getAmount() : BigDecimal.ZERO;

        if (payment.getApartmentId() != null) {
            applyToApartment(payment.getApartmentId(), amount, addPayment);
            return;
        }

        List<Apartment> apartments = apartmentRepository.findByVillaId(payment.getVillaId());
        if (apartments.isEmpty()) {
            return;
        }

        BigDecimal share = amount.divide(BigDecimal.valueOf(apartments.size()), 2, RoundingMode.HALF_UP);
        for (Apartment apartment : apartments) {
            BigDecimal current = apartment.getCurrentBalance() != null ? apartment.getCurrentBalance() : BigDecimal.ZERO;
            apartment.setCurrentBalance(addPayment ? current.subtract(share) : current.add(share));
            apartment.setUpdatedAt(LocalDateTime.now());
        }
        apartmentRepository.saveAll(apartments);
    }
}
