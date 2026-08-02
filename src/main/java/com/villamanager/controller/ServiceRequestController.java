package com.villamanager.controller;

import com.villamanager.dto.ApiResponse;
import com.villamanager.entity.Apartment;
import com.villamanager.entity.ServiceRequest;
import com.villamanager.entity.ServiceRequestStatus;
import com.villamanager.exception.ResourceNotFoundException;
import com.villamanager.repository.ApartmentRepository;
import com.villamanager.repository.ServiceRequestRepository;
import com.villamanager.repository.VendorRepository;
import com.villamanager.service.AccessControlService;
import com.villamanager.service.ExportService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/villas/{villaId}/service-requests")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ServiceRequestController {

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    @Autowired
    private ApartmentRepository apartmentRepository;

    @Autowired
    private AccessControlService accessControlService;

    @Autowired
    private ExportService exportService;

    @Autowired
    private VendorRepository vendorRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ServiceRequest>>> getServiceRequests(@PathVariable Long villaId) {
        accessControlService.requireVillaRead(villaId);
        return ResponseEntity.ok(ApiResponse.success(
                "Service requests retrieved successfully",
                serviceRequestRepository.findByVillaId(villaId)
        ));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ServiceRequest>> createServiceRequest(
            @PathVariable Long villaId,
            @RequestBody Map<String, Object> body) {
        accessControlService.requireVillaRead(villaId);
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
        accessControlService.requireServiceManage(villaId);
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
        accessControlService.requireServiceManage(villaId);
        ServiceRequest request = serviceRequestRepository.findById(requestId)
                .filter(item -> item.getVillaId().equals(villaId))
                .orElseThrow(() -> new ResourceNotFoundException("Service request not found"));
        serviceRequestRepository.delete(request);
        return ResponseEntity.ok(ApiResponse.success("Service request deleted successfully", null));
    }

    @GetMapping(value = "/export", produces = "text/csv")
    public ResponseEntity<byte[]> exportServiceRequestsCsv(@PathVariable Long villaId) {
        accessControlService.requireVillaRead(villaId);
        List<ServiceRequest> requests = serviceRequestRepository.findByVillaId(villaId);
        List<String> headers = exportService.withVillaColumn(Arrays.asList(
                "ID", "Apartment", "Description", "Status", "Vendor", "Notes", "Created At"));
        return exportService.exportToCSV(
                "service-requests",
                villaId,
                "Service Requests",
                headers,
                exportService.withVillaColumn(villaId, buildServiceRequestRows(villaId, requests)));
    }

    @GetMapping(value = "/export-excel", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> exportServiceRequestsExcel(@PathVariable Long villaId) {
        accessControlService.requireVillaRead(villaId);
        List<ServiceRequest> requests = serviceRequestRepository.findByVillaId(villaId);
        List<String> headers = exportService.withVillaColumn(Arrays.asList(
                "ID", "Apartment", "Description", "Status", "Vendor", "Notes", "Created At"));
        return exportService.exportToExcel(
                "service-requests",
                villaId,
                "Service Requests",
                headers,
                exportService.withVillaColumn(villaId, buildServiceRequestRows(villaId, requests)));
    }

    @GetMapping(value = "/export-pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> exportServiceRequestsPdf(@PathVariable Long villaId) {
        accessControlService.requireVillaRead(villaId);
        List<ServiceRequest> requests = serviceRequestRepository.findByVillaId(villaId);
        List<String> headers = exportService.withVillaColumn(Arrays.asList(
                "ID", "Apartment", "Description", "Status", "Vendor", "Notes", "Created At"));
        return exportService.exportToPdf(
                "service-requests",
                villaId,
                "Service Requests",
                headers,
                exportService.withVillaColumn(villaId, buildServiceRequestRows(villaId, requests)));
    }

    private List<List<Object>> buildServiceRequestRows(Long villaId, List<ServiceRequest> requests) {
        Map<Long, String> apartmentNumbers = apartmentRepository.findByVillaId(villaId)
                .stream()
                .collect(Collectors.toMap(Apartment::getId, Apartment::getApartmentNumber, (a, b) -> a));
        Set<Long> vendorIds = requests.stream()
                .map(ServiceRequest::getVendorId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        Map<Long, String> vendorNames = vendorRepository.findAllById(vendorIds)
                .stream()
                .collect(Collectors.toMap(com.villamanager.entity.Vendor::getId, com.villamanager.entity.Vendor::getName, (a, b) -> a));

        List<List<Object>> rows = new ArrayList<>();
        for (ServiceRequest request : requests) {
            List<Object> row = new ArrayList<>();
            row.add(request.getId());
            row.add(apartmentNumbers.getOrDefault(request.getApartmentId(), String.valueOf(request.getApartmentId())));
            row.add(request.getDescription());
            row.add(request.getStatus() != null ? request.getStatus().name() : "");
            row.add(request.getVendorId() != null ? vendorNames.getOrDefault(request.getVendorId(), "") : "");
            row.add(request.getNotes());
            row.add(request.getCreatedAt());
            rows.add(row);
        }
        return rows;
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
