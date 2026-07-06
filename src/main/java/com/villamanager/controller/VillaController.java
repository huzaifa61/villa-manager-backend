package com.villamanager.controller;

import com.villamanager.dto.ApiResponse;
import com.villamanager.entity.User;
import com.villamanager.entity.UserRole;
import com.villamanager.entity.Villa;
import com.villamanager.repository.VillaRepository;
import com.villamanager.service.AccessControlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/villas")
@CrossOrigin(origins = "*", maxAge = 3600)
public class VillaController {

    @Autowired private AccessControlService accessControlService;
    @Autowired private VillaRepository villaRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Villa>>> listVillas() {
        User user = accessControlService.currentUser();
        if (user.getRole() == UserRole.GENERAL_MANAGER) {
            return ResponseEntity.ok(ApiResponse.success("Villas retrieved successfully", villaRepository.findAll()));
        }
        if (user.getVillaId() == null) {
            return ResponseEntity.ok(ApiResponse.success("Villas retrieved successfully", List.of()));
        }
        return ResponseEntity.ok(ApiResponse.success(
                "Villas retrieved successfully",
                villaRepository.findById(user.getVillaId()).map(villa -> List.of(villa)).orElse(List.of())
        ));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Villa>> createVilla(@RequestBody Map<String, Object> body) {
        accessControlService.requireGeneralManager();
        Villa villa = new Villa();
        villa.setName(requiredText(body.get("name"), "Villa name is required"));
        villa.setLocation(text(body.get("location")));
        villa.setDescription(text(body.get("description")));
        villa.setTotalApartments(asInteger(body.get("totalApartments"), 0));
        villa.setIsActive(true);
        villa.setCreatedAt(LocalDateTime.now());
        villa.setUpdatedAt(LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Villa created successfully", villaRepository.save(villa)));
    }

    private String requiredText(Object value, String message) {
        String text = text(value);
        if (text == null || text.isBlank()) throw new IllegalArgumentException(message);
        return text;
    }

    private String text(Object value) {
        return value == null ? null : String.valueOf(value).trim();
    }

    private Integer asInteger(Object value, Integer fallback) {
        if (value == null || String.valueOf(value).isBlank()) return fallback;
        return Integer.valueOf(String.valueOf(value));
    }
}
