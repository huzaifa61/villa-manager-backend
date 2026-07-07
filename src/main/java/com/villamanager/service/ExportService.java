package com.villamanager.service;

import com.villamanager.config.BrandingProperties;
import com.villamanager.config.ExportProperties;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExportService {

    private final BrandingProperties brandingProperties;
    private final ExportProperties exportProperties;

    public ResponseEntity<byte[]> exportToCSV(String fileName, List<String> headers, List<List<Object>> rows) {
        try {
            String csv = CsvExportUtil.buildCsv(headers, rows);
            return buildResponse(csv.getBytes(), fileName, "csv");
        } catch (Exception e) {
            log.error("Error exporting to CSV", e);
            throw new RuntimeException("Failed to export CSV", e);
        }
    }

    public ResponseEntity<byte[]> exportToExcel(String fileName, String sheetName, List<String> headers, List<List<Object>> rows) {
        try {
            byte[] excel = ExcelExportUtil.generateExcel(sheetName, headers, rows);
            return buildResponse(excel, fileName, "xlsx");
        } catch (IOException e) {
            log.error("Error exporting to Excel", e);
            throw new RuntimeException("Failed to export Excel", e);
        }
    }

    public ResponseEntity<byte[]> exportToPdf(String fileName, String title, List<String> headers, List<List<Object>> rows) {
        try {
            byte[] pdf = PdfExportUtil.generatePdf(title, headers, rows, brandingProperties);
            return buildResponse(pdf, fileName, "pdf");
        } catch (Exception e) {
            log.error("Error exporting to PDF", e);
            throw new RuntimeException("Failed to export PDF", e);
        }
    }

    private ResponseEntity<byte[]> buildResponse(byte[] content, String fileName, String format) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String finalFileName = fileName + "_" + timestamp + "." + format;
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", finalFileName);
        headers.setContentType(getMediaType(format));
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(content);
    }

    private MediaType getMediaType(String format) {
        return switch (format) {
            case "xlsx" -> MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            case "pdf" -> MediaType.APPLICATION_PDF;
            default -> MediaType.TEXT_PLAIN;
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
