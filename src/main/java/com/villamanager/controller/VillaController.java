package com.villamanager.controller;

import com.villamanager.dto.ApiResponse;
import com.villamanager.dto.VillaDto;
import com.villamanager.dto.VillaRequest;
import com.villamanager.entity.PropertyType;
import com.villamanager.entity.User;
import com.villamanager.entity.UserRole;
import com.villamanager.entity.Villa;
import com.villamanager.repository.UserRepository;
import com.villamanager.repository.VillaRepository;
import com.villamanager.service.AccessControlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/villas")
@CrossOrigin(origins = "*", maxAge = 3600)
public class VillaController {

    @Autowired private AccessControlService accessControlService;
    @Autowired private VillaRepository villaRepository;
    @Autowired private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<VillaDto>>> listVillas() {
        User user = accessControlService.currentUser();
        List<Villa> villas;
        if (user.getRole() == UserRole.GENERAL_MANAGER) {
            villas = villaRepository.findAll();
        } else if (user.getVillaId() != null) {
            villas = villaRepository.findById(user.getVillaId()).map(List::of).orElse(List.of());
        } else {
            villas = List.of();
        }
        return ResponseEntity.ok(ApiResponse.success("Villas retrieved successfully",
                villas.stream().map(this::toDto).collect(Collectors.toList())));
    }

    @GetMapping("/{villaId}")
    public ResponseEntity<ApiResponse<VillaDto>> getVilla(@PathVariable Long villaId) {
        accessControlService.requireVillaRead(villaId);
        Villa villa = villaRepository.findById(villaId)
                .orElseThrow(() -> new RuntimeException("Villa not found"));
        return ResponseEntity.ok(ApiResponse.success("Villa retrieved successfully", toDto(villa)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<VillaDto>> createVilla(@RequestBody VillaRequest request) {
        accessControlService.requireGeneralManager();
        Villa villa = new Villa();
        villa.setName(request.getName());
        villa.setPropertyType(request.getPropertyType() != null ? request.getPropertyType() : PropertyType.VILLA);
        villa.setPropertyNumber(request.getPropertyNumber());
        villa.setRegion(request.getRegion());
        villa.setWhatsappLink(request.getWhatsappLink());
        villa.setLocation(request.getLocation());
        villa.setDescription(request.getDescription());
        villa.setTotalApartments(0);
        villa.setIsActive(true);
        villa.setCreatedAt(LocalDateTime.now());
        villa.setUpdatedAt(LocalDateTime.now());
        Villa saved = villaRepository.save(villa);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Property created successfully", toDto(saved)));
    }

    @PutMapping("/{villaId}")
    public ResponseEntity<ApiResponse<VillaDto>> updateVilla(
            @PathVariable Long villaId,
            @RequestBody VillaRequest request) {
        User user = accessControlService.currentUser();
        // GM can update any villa, Villa Manager can only update their own
        if (user.getRole() != UserRole.GENERAL_MANAGER) {
            if (!villaId.equals(user.getVillaId())) {
                throw new RuntimeException("Access denied");
            }
        }
        Villa villa = villaRepository.findById(villaId)
                .orElseThrow(() -> new RuntimeException("Villa not found"));
        if (request.getName() != null) villa.setName(request.getName());
        if (request.getPropertyType() != null) villa.setPropertyType(request.getPropertyType());
        if (request.getPropertyNumber() != null) villa.setPropertyNumber(request.getPropertyNumber());
        if (request.getRegion() != null) villa.setRegion(request.getRegion());
        if (request.getWhatsappLink() != null) villa.setWhatsappLink(request.getWhatsappLink());
        if (request.getLocation() != null) villa.setLocation(request.getLocation());
        if (request.getDescription() != null) villa.setDescription(request.getDescription());
        villa.setUpdatedAt(LocalDateTime.now());
        return ResponseEntity.ok(ApiResponse.success("Property updated successfully", toDto(villaRepository.save(villa))));
    }

    @DeleteMapping("/{villaId}")
    public ResponseEntity<ApiResponse<Void>> deleteVilla(@PathVariable Long villaId) {
        accessControlService.requireGeneralManager();
        villaRepository.deleteById(villaId);
        return ResponseEntity.ok(ApiResponse.success("Property deleted successfully", null));
    }

    private VillaDto toDto(Villa v) {
        return VillaDto.builder()
                .id(v.getId())
                .name(v.getName())
                .propertyType(v.getPropertyType())
                .propertyNumber(v.getPropertyNumber())
                .region(v.getRegion())
                .whatsappLink(v.getWhatsappLink())
                .location(v.getLocation())
                .description(v.getDescription())
                .totalApartments(v.getTotalApartments())
                .isActive(v.getIsActive())
                .build();
    }
}
