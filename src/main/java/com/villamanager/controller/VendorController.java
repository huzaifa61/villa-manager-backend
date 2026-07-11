package com.villamanager.controller;

import com.villamanager.dto.ApiResponse;
import com.villamanager.entity.Vendor;
import com.villamanager.service.AccessControlService;
import com.villamanager.service.VendorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/vendors")
@CrossOrigin(origins = "*", maxAge = 3600)
public class VendorController {

    @Autowired
    private VendorService vendorService;

    @Autowired
    private AccessControlService accessControlService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Vendor>>> getVendors() {
        accessControlService.currentUser();
        return ResponseEntity.ok(ApiResponse.success("Vendors retrieved successfully", vendorService.listForCurrentUser()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Vendor>> createVendor(@RequestBody Vendor vendor) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Vendor created successfully", vendorService.create(vendor)));
    }

    @PutMapping("/{vendorId}")
    public ResponseEntity<ApiResponse<Vendor>> updateVendor(
            @PathVariable Long vendorId,
            @RequestBody Vendor body) {
        return ResponseEntity.ok(ApiResponse.success("Vendor updated successfully", vendorService.update(vendorId, body)));
    }

    @DeleteMapping("/{vendorId}")
    public ResponseEntity<ApiResponse<Void>> deleteVendor(@PathVariable Long vendorId) {
        vendorService.delete(vendorId);
        return ResponseEntity.ok(ApiResponse.success("Vendor deleted successfully", null));
    }
}
