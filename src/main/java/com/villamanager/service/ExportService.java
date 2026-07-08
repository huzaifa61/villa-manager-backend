package com.villamanager.service;

import com.villamanager.config.BrandingProperties;
import com.villamanager.config.ExportProperties;
import com.villamanager.entity.Villa;
import com.villamanager.repository.VillaRepository;
import com.villamanager.util.CsvExportUtil;
import com.villamanager.util.ExcelExportUtil;
import com.villamanager.util.PdfExportUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExportService {

    private final BrandingProperties brandingProperties;
    private final ExportProperties exportProperties;
    private final VillaRepository villaRepository;

    // ── CSV ──────────────────────────────────────────────────────────────────

    public ResponseEntity<byte[]> exportToCSV(String fileName, List<String> headers, List<List<Object>> rows) {
        return exportToCSV(fileName, null, fileName, headers, rows);
    }

    public ResponseEntity<byte[]> exportToCSV(String fileName, String title, List<String> headers, List<List<Object>> rows) {
        return exportToCSV(fileName, null, title, headers, rows);
    }

    public ResponseEntity<byte[]> exportToCSV(String fileName, Long villaId, String title, List<String> headers, List<List<Object>> rows) {
        try {
            Villa villa = resolveVilla(villaId);
            BrandingProperties effective = effectiveBranding(villa);
            String csv = CsvExportUtil.buildCsvWithBranding(headers, rows, effective, title);
            return buildResponse(csv.getBytes("UTF-8"), fileName, "csv");
        } catch (Exception e) {
            log.error("Error exporting to CSV", e);
            throw new RuntimeException("Failed to export CSV", e);
        }
    }

    // ── Excel ─────────────────────────────────────────────────────────────────

    public ResponseEntity<byte[]> exportToExcel(String fileName, String sheetName, List<String> headers, List<List<Object>> rows) {
        return exportToExcel(fileName, null, sheetName, headers, rows);
    }

    public ResponseEntity<byte[]> exportToExcel(String fileName, Long villaId, String sheetName, List<String> headers, List<List<Object>> rows) {
        try {
            Villa villa = resolveVilla(villaId);
            BrandingProperties effective = effectiveBranding(villa);
            byte[] excel = ExcelExportUtil.generateExcel(sheetName, headers, rows, effective);
            return buildResponse(excel, fileName, "xlsx");
        } catch (IOException e) {
            log.error("Error exporting to Excel", e);
            throw new RuntimeException("Failed to export Excel", e);
        }
    }

    // ── PDF ───────────────────────────────────────────────────────────────────

    public ResponseEntity<byte[]> exportToPdf(String fileName, String title, List<String> headers, List<List<Object>> rows) {
        return exportToPdf(fileName, null, title, headers, rows);
    }

    public ResponseEntity<byte[]> exportToPdf(String fileName, Long villaId, String title, List<String> headers, List<List<Object>> rows) {
        try {
            Villa villa = resolveVilla(villaId);
            BrandingProperties effective = effectiveBranding(villa);
            byte[] pdf = PdfExportUtil.generatePdf(title, headers, rows, effective);
            return buildResponse(pdf, fileName, "pdf");
        } catch (Exception e) {
            log.error("Error exporting to PDF", e);
            throw new RuntimeException("Failed to export PDF", e);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Fetch villa from DB, or null if villaId is null / not found */
    private Villa resolveVilla(Long villaId) {
        if (villaId == null) return null;
        return villaRepository.findById(villaId).orElse(null);
    }

    /**
     * Build effective branding: override address with villa name + location from DB.
     * Falls back to global branding when no villa is found.
     */
    private BrandingProperties effectiveBranding(Villa villa) {
        if (villa == null) return brandingProperties;

        BrandingProperties eff = new BrandingProperties();
        eff.setAppName(brandingProperties.getAppName());
        eff.setAppVersion(brandingProperties.getAppVersion());
        eff.setLogoUrl(brandingProperties.getLogoUrl());
        eff.setSupportEmail(brandingProperties.getSupportEmail());
        eff.setPhoneNumber(brandingProperties.getPhoneNumber());

        // Override company name and address with villa-specific values
        eff.setCompanyName(villa.getName()); // e.g. "Palm Villa A"

        String locationLine = buildLocationLine(villa);
        eff.setAddress(locationLine);

        return eff;
    }

    /** Build a readable location string from villa fields */
    private String buildLocationLine(Villa villa) {
        StringBuilder loc = new StringBuilder();
        if (villa.getPropertyNumber() != null && !villa.getPropertyNumber().isBlank()) {
            loc.append("No. ").append(villa.getPropertyNumber().trim()).append(" ");
        }
        if (villa.getRegion() != null && !villa.getRegion().isBlank()) {
            loc.append(villa.getRegion().trim());
        }
        if (villa.getLocation() != null && !villa.getLocation().isBlank()) {
            if (loc.length() > 0) loc.append(", ");
            loc.append(villa.getLocation().trim());
        }
        // Fall back to global address if villa has no location data
        return loc.length() > 0 ? loc.toString() : brandingProperties.getAddress();
    }

    private ResponseEntity<byte[]> buildResponse(byte[] content, String fileName, String format) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String finalFileName = fileName + "_" + timestamp + "." + format;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", finalFileName);
        headers.setContentType(getMediaType(format));
        return ResponseEntity.ok().headers(headers).body(content);
    }

    private MediaType getMediaType(String format) {
        return switch (format) {
            case "xlsx" -> MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            case "pdf"  -> MediaType.APPLICATION_PDF;
            default     -> MediaType.TEXT_PLAIN;
        };
    }

    public Map<String, Object> getExportInfo() {
        return Map.of(
                "appName", brandingProperties.getAppName(),
                "appVersion", brandingProperties.getAppVersion(),
                "maxRecords", exportProperties.getMaxRecords(),
                "supportedFormats", List.of("csv", "xlsx", "pdf")
        );
    }
}
