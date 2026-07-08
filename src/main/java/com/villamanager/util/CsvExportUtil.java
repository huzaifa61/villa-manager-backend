package com.villamanager.util;

import com.villamanager.config.BrandingProperties;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public final class CsvExportUtil {
    private CsvExportUtil() {}

    public static String buildCsv(List<String> headers, List<? extends List<?>> rows) {
        return buildCsvWithBranding(headers, rows, null, null);
    }

    public static String buildCsvWithBranding(List<String> headers, List<? extends List<?>> rows,
                                               BrandingProperties branding, String reportTitle) {
        StringBuilder csv = new StringBuilder("\uFEFF"); // UTF-8 BOM

        // Branding header block
        if (branding != null) {
            String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            csv.append("\"").append(branding.getAppName()).append("\"\n");
            csv.append("\"").append(branding.getCompanyName()).append("\"\n");
            if (reportTitle != null && !reportTitle.isBlank()) {
                csv.append("\"Report: ").append(reportTitle).append("\"\n");
            }
            csv.append("\"Generated: ").append(now).append("\"\n");
            csv.append("\"Email: ").append(branding.getSupportEmail())
               .append(" | Phone: ").append(branding.getPhoneNumber())
               .append(" | ").append(branding.getAddress()).append("\"\n");
            csv.append("\n"); // blank line separator before data
        }

        // Column headers
        csv.append(headers.stream().map(CsvExportUtil::cleanCell).collect(Collectors.joining(",")));

        // Data rows
        for (List<?> row : rows) {
            csv.append("\n");
            csv.append(row.stream().map(CsvExportUtil::cleanCell).collect(Collectors.joining(",")));
        }

        return csv.toString();
    }

    private static String cleanCell(Object value) {
        String text = value == null ? "" : String.valueOf(value);
        return "\"" + text.replace("\"", "\"\"") + "\"";
    }
}
