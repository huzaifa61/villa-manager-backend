package com.villamanager.controller;

import com.villamanager.dto.ApiResponse;
import com.villamanager.entity.Vendor;
import com.villamanager.exception.ResourceNotFoundException;
import com.villamanager.repository.VendorRepository;
import com.villamanager.service.AccessControlService;
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
    private VendorRepository vendorRepository;

    @Autowired
    private AccessControlService accessControlService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Vendor>>> getVendors() {
        accessControlService.requireVendorManage();
        return ResponseEntity.ok(ApiResponse.success("Vendors retrieved successfully", vendorRepository.findAll()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Vendor>> createVendor(@RequestBody Vendor vendor) {
        accessControlService.requireVendorManage();
        vendor.setId(null);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Vendor created successfully", vendorRepository.save(vendor)));
    }

    @PutMapping("/{vendorId}")
    public ResponseEntity<ApiResponse<Vendor>> updateVendor(
            @PathVariable Long vendorId,
            @RequestBody Vendor body) {
        accessControlService.requireVendorManage();
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));
        vendor.setName(body.getName());
        vendor.setContactPerson(body.getContactPerson());
        vendor.setEmail(body.getEmail());
        vendor.setPhoneNumber(body.getPhoneNumber());
        vendor.setAddress(body.getAddress());
        vendor.setServiceType(body.getServiceType());
        vendor.setIsActive(body.getIsActive() != null ? body.getIsActive() : true);
        return ResponseEntity.ok(ApiResponse.success("Vendor updated successfully", vendorRepository.save(vendor)));
    }

    @DeleteMapping("/{vendorId}")
    public ResponseEntity<ApiResponse<Void>> deleteVendor(@PathVariable Long vendorId) {
        accessControlService.requireVendorManage();
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));
        vendorRepository.delete(vendor);
        return ResponseEntity.ok(ApiResponse.success("Vendor deleted successfully", null));
    }
}
