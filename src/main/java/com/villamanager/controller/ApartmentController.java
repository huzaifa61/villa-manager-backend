package com.villamanager.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.villamanager.dto.ApiResponse;
import com.villamanager.dto.ApartmentDto;
import com.villamanager.dto.ApartmentRequest;
import com.villamanager.service.ApartmentService;
import java.util.List;

@RestController
@RequestMapping("/v1/villas/{villaId}/apartments")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ApartmentController {

    @Autowired
    private ApartmentService apartmentService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ApartmentDto>>> getApartments(@PathVariable Long villaId) {
        List<ApartmentDto> apartments = apartmentService.getApartments(villaId);
        return ResponseEntity.ok(ApiResponse.success("Apartments retrieved successfully", apartments));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ApartmentDto>> createApartment(
            @PathVariable Long villaId,
            @RequestBody ApartmentRequest request) {
        ApartmentDto apartment = apartmentService.createApartment(villaId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Apartment created successfully", apartment));
    }
}
