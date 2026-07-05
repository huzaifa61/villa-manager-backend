package com.villamanager.controller;

import com.villamanager.dto.ApiResponse;
import com.villamanager.entity.Apartment;
import com.villamanager.entity.ServiceRequest;
import com.villamanager.entity.ServiceRequestStatus;
import com.villamanager.exception.ResourceNotFoundException;
import com.villamanager.repository.ApartmentRepository;
import com.villamanager.repository.ServiceRequestRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/villas/{villaId}/service-requests")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ServiceRequestController {

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    @Autowired
    private ApartmentRepository apartmentRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ServiceRequest>>> getServiceRequests(@PathVariable Long villaId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Service requests retrieved successfully",
                serviceRequestRepository.findByVillaId(villaId)
        ));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ServiceRequest>> createServiceRequest(
            @PathVariable Long villaId,
            @RequestBody Map<String, Object> body) {
        ServiceRequest request = new ServiceRequest();
        request.setVillaId(villaId);
        request.setApartmentId(resolveApartmentId(villaId, body.get("apartmentId")));
        request.setDescription(requiredText(body.get("description"), "Description is required"));
        request.setVendorId(asLong(body.get("vendorId")));
        request.setStatus(parseStatus(text(body.get("status")), ServiceRequestStatus.OPEN));
        request.setNotes(buildNotes(body));
        request.setCreatedAt(LocalDateTime.now());
        request.setUpdatedAt(LocalDateTime.now());
        ServiceRequest saved = serviceRequestRepository.save(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Service request created successfully", saved));
    }

    @PutMapping("/{requestId}")
    public ResponseEntity<ApiResponse<ServiceRequest>> updateServiceRequest(
            @PathVariable Long villaId,
            @PathVariable Long requestId,
            @RequestBody Map<String, Object> body) {
        ServiceRequest request = serviceRequestRepository.findById(requestId)
                .filter(item -> item.getVillaId().equals(villaId))
                .orElseThrow(() -> new ResourceNotFoundException("Service request not found"));
        request.setApartmentId(resolveApartmentId(villaId, body.get("apartmentId")));
        request.setDescription(requiredText(body.get("description"), "Description is required"));
        request.setVendorId(asLong(body.get("vendorId")));
        request.setStatus(parseStatus(text(body.get("status")), request.getStatus()));
        request.setNotes(buildNotes(body));
        request.setUpdatedAt(LocalDateTime.now());
        return ResponseEntity.ok(ApiResponse.success(
                "Service request updated successfully",
                serviceRequestRepository.save(request)
        ));
    }

    @DeleteMapping("/{requestId}")
    public ResponseEntity<ApiResponse<Void>> deleteServiceRequest(
            @PathVariable Long villaId,
            @PathVariable Long requestId) {
        ServiceRequest request = serviceRequestRepository.findById(requestId)
                .filter(item -> item.getVillaId().equals(villaId))
                .orElseThrow(() -> new ResourceNotFoundException("Service request not found"));
        serviceRequestRepository.delete(request);
        return ResponseEntity.ok(ApiResponse.success("Service request deleted successfully", null));
    }

    private Long resolveApartmentId(Long villaId, Object value) {
        Long requestedId = asLong(value);
        if (requestedId != null) {
            return apartmentRepository.findById(requestedId)
                    .filter(apartment -> apartment.getVillaId().equals(villaId))
                    .map(Apartment::getId)
                    .orElseThrow(() -> new ResourceNotFoundException("Apartment not found"));
        }
        return apartmentRepository.findByVillaId(villaId)
                .stream()
                .findFirst()
                .map(Apartment::getId)
                .orElseThrow(() -> new ResourceNotFoundException("Add an apartment before creating service requests"));
    }

    private String buildNotes(Map<String, Object> body) {
        String notes = text(body.get("notes"));
        String category = text(body.get("category"));
        String urgency = text(body.get("urgency"));
        String contact = text(body.get("preferredContact"));
        StringBuilder builder = new StringBuilder(notes != null ? notes : "");
        if (category != null) builder.append(builder.isEmpty() ? "" : "\n").append("Category: ").append(category);
        if (urgency != null) builder.append(builder.isEmpty() ? "" : "\n").append("Urgency: ").append(urgency);
        if (contact != null) builder.append(builder.isEmpty() ? "" : "\n").append("Preferred contact: ").append(contact);
        return builder.toString();
    }

    private ServiceRequestStatus parseStatus(String value, ServiceRequestStatus fallback) {
        if (value == null || value.isBlank()) return fallback;
        return ServiceRequestStatus.valueOf(value.toUpperCase().replace(" ", "_"));
    }

    private String requiredText(Object value, String message) {
        String text = text(value);
        if (text == null || text.isBlank()) throw new IllegalArgumentException(message);
        return text;
    }

    private String text(Object value) {
        return value == null ? null : String.valueOf(value).trim();
    }

    private Long asLong(Object value) {
        if (value == null || String.valueOf(value).isBlank()) return null;
        return Long.valueOf(String.valueOf(value));
    }
}
