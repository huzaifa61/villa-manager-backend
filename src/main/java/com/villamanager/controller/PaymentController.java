package com.villamanager.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.villamanager.dto.ApiResponse;
import com.villamanager.dto.PaymentDto;
import com.villamanager.dto.PaymentRequest;
import com.villamanager.entity.Payment;
import com.villamanager.entity.PaymentStatus;
import com.villamanager.repository.PaymentRepository;
import com.villamanager.repository.ApartmentRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/villas/{villaId}/payments")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PaymentController {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ApartmentRepository apartmentRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PaymentDto>>> getPayments(@PathVariable Long villaId) {
        List<Payment> payments = paymentRepository.findByVillaId(villaId);
        List<PaymentDto> dtos = payments.stream().map(p -> {
            String aptNumber = apartmentRepository.findById(p.getApartmentId())
                .map(a -> a.getApartmentNumber()).orElse("N/A");
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
        }).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Payments retrieved successfully", dtos));
    }

    @PostMapping("/apartment/{apartmentId}")
    public ResponseEntity<ApiResponse<PaymentDto>> createPayment(
            @PathVariable Long villaId,
            @PathVariable Long apartmentId,
            @RequestBody PaymentRequest request) {

        Payment payment = new Payment();
        payment.setVillaId(villaId);
        payment.setApartmentId(apartmentId);
        payment.setCategoryId(request.getCategoryId() != null ? request.getCategoryId() : 1L);
        payment.setAmount(request.getAmount());
        payment.setPaymentDate(request.getPaymentDate() != null ? request.getPaymentDate() : LocalDate.now());
        payment.setPaymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "CASH");
        payment.setReferenceNumber(request.getReferenceNumber());
        PaymentStatus pStatus;
        try {
            String reqStatus = request.getStatus() != null ? request.getStatus() : "COMPLETED";
            // Map frontend values to backend enum
            if (reqStatus.equals("PAID")) reqStatus = "COMPLETED";
            pStatus = PaymentStatus.valueOf(reqStatus);
        } catch (IllegalArgumentException e) {
            pStatus = PaymentStatus.COMPLETED;
        }
        payment.setStatus(pStatus);
        payment.setNotes(request.getNotes());
        payment.setIsSplit(false);
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());

        Payment saved = paymentRepository.save(payment);

        PaymentDto dto = PaymentDto.builder()
            .id(saved.getId())
            .villaId(saved.getVillaId())
            .apartmentId(saved.getApartmentId())
            .amount(saved.getAmount())
            .paymentDate(saved.getPaymentDate().toString())
            .status(saved.getStatus().toString())
            .notes(saved.getNotes())
            .build();

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Payment created successfully", dto));
    }
}
