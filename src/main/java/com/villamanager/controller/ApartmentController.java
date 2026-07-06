package com.villamanager.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.villamanager.dto.ApiResponse;
import com.villamanager.dto.ApartmentDto;
import com.villamanager.dto.ApartmentRequest;
import com.villamanager.service.AccessControlService;
import com.villamanager.service.ApartmentService;
import com.villamanager.util.CsvExportUtil;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/villas/{villaId}/apartments")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ApartmentController {

    @Autowired
    private ApartmentService apartmentService;

    @Autowired
    private AccessControlService accessControlService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ApartmentDto>>> getApartments(@PathVariable Long villaId) {
        accessControlService.requireVillaRead(villaId);
        List<ApartmentDto> apartments = apartmentService.getApartments(villaId);
        return ResponseEntity.ok(ApiResponse.success("Apartments retrieved successfully", apartments));
    }

    @GetMapping(value = "/export", produces = "text/csv")
    public ResponseEntity<String> exportApartments(@PathVariable Long villaId) {
        accessControlService.requireVillaRead(villaId);
        List<ApartmentDto> apartments = apartmentService.getApartments(villaId);
        String csv = CsvExportUtil.buildCsv(
                Arrays.asList("ID", "Apartment", "Owner", "Tenant", "Phone", "Email", "Status", "Opening Balance", "Current Balance", "Type"),
                apartments.stream()
                        .map(a -> Arrays.asList(
                                a.getId(),
                                a.getApartmentNumber(),
                                a.getOwnerName(),
                                a.getTenantName(),
                                a.getPhoneNumber(),
                                a.getEmail(),
                                a.getStatus(),
                                a.getOpeningBalance(),
                                a.getCurrentBalance(),
                                a.getApartmentType()))
                        .collect(Collectors.toList()));

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"apartments.csv\"")
                .body(csv);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ApartmentDto>> createApartment(
            @PathVariable Long villaId,
            @RequestBody ApartmentRequest request) {
        accessControlService.requireVillaManage(villaId);
        ApartmentDto apartment = apartmentService.createApartment(villaId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Apartment created successfully", apartment));
    }

    @PutMapping("/{apartmentId}")
    public ResponseEntity<ApiResponse<ApartmentDto>> updateApartment(
            @PathVariable Long villaId,
            @PathVariable Long apartmentId,
            @RequestBody ApartmentRequest request) {
        accessControlService.requireVillaManage(villaId);
        ApartmentDto apartment = apartmentService.updateApartment(villaId, apartmentId, request);
        return ResponseEntity.ok(ApiResponse.success("Apartment updated successfully", apartment));
    }

    @DeleteMapping("/{apartmentId}")
    public ResponseEntity<ApiResponse<Void>> deleteApartment(
            @PathVariable Long villaId,
            @PathVariable Long apartmentId) {
        accessControlService.requireVillaManage(villaId);
        apartmentService.deleteApartment(villaId, apartmentId);
        return ResponseEntity.ok(ApiResponse.success("Apartment deleted successfully", null));
    }
}
