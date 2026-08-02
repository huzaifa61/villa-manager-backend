package com.villamanager.controller;

import com.villamanager.dto.ApiResponse;
import com.villamanager.entity.Vendor;
import com.villamanager.service.AccessControlService;
import com.villamanager.service.ExportService;
import com.villamanager.service.VendorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/v1/vendors")
@CrossOrigin(origins = "*", maxAge = 3600)
public class VendorController {

    @Autowired
    private VendorService vendorService;

    @Autowired
    private AccessControlService accessControlService;

    @Autowired
    private ExportService exportService;

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

    @GetMapping(value = "/export", produces = "text/csv")
    public ResponseEntity<byte[]> exportVendorsCsv(@RequestParam(required = false) Long villaId) {
        accessControlService.currentUser();
        return exportVendors("vendors", villaId, "csv");
    }

    @GetMapping(value = "/export-excel", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> exportVendorsExcel(@RequestParam(required = false) Long villaId) {
        accessControlService.currentUser();
        return exportVendorsExcelPdf("vendors", villaId, true);
    }

    @GetMapping(value = "/export-pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> exportVendorsPdf(@RequestParam(required = false) Long villaId) {
        accessControlService.currentUser();
        return exportVendorsExcelPdf("vendors", villaId, false);
    }

    private ResponseEntity<byte[]> exportVendors(String fileName, Long villaId, String format) {
        List<Vendor> vendors = vendorService.listForCurrentUser();
        List<String> headers = exportService.withVillaColumn(Arrays.asList(
                "ID", "Name", "Contact Person", "Phone", "Email", "Service Type", "Location", "Address", "Active"));
        List<List<Object>> rows = buildVendorRows(vendors);
        if ("csv".equals(format)) {
            return exportService.exportToCSV(
                    fileName, villaId, "Vendors", headers, exportService.withVillaColumn(villaId, rows));
        }
        throw new IllegalArgumentException("Unsupported export format: " + format);
    }

    private ResponseEntity<byte[]> exportVendorsExcelPdf(String fileName, Long villaId, boolean excel) {
        List<Vendor> vendors = vendorService.listForCurrentUser();
        List<String> headers = exportService.withVillaColumn(Arrays.asList(
                "ID", "Name", "Contact Person", "Phone", "Email", "Service Type", "Location", "Address", "Active"));
        List<List<Object>> rows = exportService.withVillaColumn(villaId, buildVendorRows(vendors));
        if (excel) {
            return exportService.exportToExcel(fileName, villaId, "Vendors", headers, rows);
        }
        return exportService.exportToPdf(fileName, villaId, "Vendors", headers, rows);
    }

    private List<List<Object>> buildVendorRows(List<Vendor> vendors) {
        List<List<Object>> rows = new ArrayList<>();
        for (Vendor vendor : vendors) {
            List<Object> row = new ArrayList<>();
            row.add(vendor.getId());
            row.add(vendor.getName());
            row.add(vendor.getContactPerson());
            row.add(vendor.getPhoneNumber());
            row.add(vendor.getEmail());
            row.add(vendor.getServiceType());
            row.add(vendor.getRegion());
            row.add(vendor.getAddress());
            row.add(vendor.getIsActive() != null && vendor.getIsActive() ? "Yes" : "No");
            rows.add(row);
        }
        return rows;
    }
}
