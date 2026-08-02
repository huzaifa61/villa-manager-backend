package com.villamanager.controller;

import com.villamanager.dto.TableExportRequest;
import com.villamanager.service.AccessControlService;
import com.villamanager.service.ExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/villas/{villaId}/exports")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TableExportController {

    @Autowired
    private ExportService exportService;

    @Autowired
    private AccessControlService accessControlService;

    @PostMapping(value = "/excel", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> exportExcel(
            @PathVariable Long villaId,
            @RequestBody TableExportRequest request) {
        accessControlService.requireVillaRead(villaId);
        List<String> headers = exportService.withVillaColumn(request.getHeaders());
        List<List<Object>> rows = exportService.withVillaColumn(villaId, request.getRows());
        return exportService.exportToExcel(
                sanitizeFileName(request.getFileName(), "export"),
                villaId,
                sheetName(request),
                headers,
                rows);
    }

    @PostMapping(value = "/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> exportPdf(
            @PathVariable Long villaId,
            @RequestBody TableExportRequest request) {
        accessControlService.requireVillaRead(villaId);
        List<String> headers = exportService.withVillaColumn(request.getHeaders());
        List<List<Object>> rows = exportService.withVillaColumn(villaId, request.getRows());
        return exportService.exportToPdf(
                sanitizeFileName(request.getFileName(), "export"),
                villaId,
                title(request),
                headers,
                rows);
    }

    @PostMapping(value = "/csv", produces = "text/csv")
    public ResponseEntity<byte[]> exportCsv(
            @PathVariable Long villaId,
            @RequestBody TableExportRequest request) {
        accessControlService.requireVillaRead(villaId);
        List<String> headers = exportService.withVillaColumn(request.getHeaders());
        List<List<Object>> rows = exportService.withVillaColumn(villaId, request.getRows());
        return exportService.exportToCSV(
                sanitizeFileName(request.getFileName(), "export"),
                villaId,
                title(request),
                headers,
                rows);
    }

    private String title(TableExportRequest request) {
        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            return request.getTitle().trim();
        }
        return sanitizeFileName(request.getFileName(), "Export");
    }

    private String sheetName(TableExportRequest request) {
        if (request.getSheetName() != null && !request.getSheetName().isBlank()) {
            return request.getSheetName().trim();
        }
        return title(request);
    }

    private String sanitizeFileName(String value, String fallback) {
        if (value == null || value.isBlank()) return fallback;
        String trimmed = value.trim();
        int dot = trimmed.lastIndexOf('.');
        return dot > 0 ? trimmed.substring(0, dot) : trimmed;
    }
}
