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
import com.villamanager.service.ExportService;
import com.villamanager.util.CsvExportUtil;

import java.util.List;

@RestController
@RequestMapping("/v1/villas/{villaId}/apartments")
@CrossOrigin(origins = "*", maxAge = 3600)
class ApartmentController {

    @Autowired
    private ApartmentService apartmentService;

    @Autowired
    private AccessControlService accessControlService;

    @Autowired
    private ExportService exportService;

    @Autowired
    private ApartmentExportSupport apartmentExportSupport;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ApartmentDto>>> getApartments(@PathVariable Long villaId) {
        accessControlService.requireVillaRead(villaId);
        List<ApartmentDto> apartments = apartmentService.getApartments(villaId);
        return ResponseEntity.ok(ApiResponse.success("Apartments retrieved successfully", apartments));
    }

    @GetMapping(value = "/export", produces = "text/csv")
    public ResponseEntity<byte[]> exportApartments(@PathVariable Long villaId) {
        accessControlService.requireVillaRead(villaId);
        List<ApartmentDto> apartments = apartmentExportSupport.apartmentsForExport(villaId);
        List<String> headers = exportService.withVillaColumn(ApartmentExportSupport.fullCsvHeaders());
        List<List<Object>> rows = apartmentExportSupport.buildFullCsvRows(villaId, apartments);
        return exportService.exportToCSV("apartments", villaId, "Apartments Report", headers, exportService.withVillaColumn(villaId, rows));
    }

    @GetMapping(value = "/export-excel")
    public ResponseEntity<byte[]> exportApartmentsExcel(@PathVariable Long villaId) {
        accessControlService.requireVillaRead(villaId);
        List<ApartmentDto> apartments = apartmentExportSupport.apartmentsForExport(villaId);
        List<String> headers = exportService.withVillaColumn(ApartmentExportSupport.exportHeaders());
        List<List<Object>> rows = apartmentExportSupport.buildExportRows(villaId, apartments);
        return exportService.exportToExcel("apartments", villaId, "Apartments", headers, exportService.withVillaColumn(villaId, rows));
    }

    @GetMapping(value = "/export-pdf")
    public ResponseEntity<byte[]> exportApartmentsPdf(@PathVariable Long villaId) {
        accessControlService.requireVillaRead(villaId);
        List<ApartmentDto> apartments = apartmentExportSupport.apartmentsForExport(villaId);
        List<String> headers = exportService.withVillaColumn(ApartmentExportSupport.exportHeaders());
        List<List<Object>> rows = apartmentExportSupport.buildExportRows(villaId, apartments);
        return exportService.exportToPdf("apartments", villaId, "Apartments Report", headers, exportService.withVillaColumn(villaId, rows));
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
