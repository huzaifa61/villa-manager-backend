package com.villamanager.util;

import com.villamanager.config.BrandingProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
public class ExcelExportUtil {

    public static byte[] generateExcel(String sheetName, List<String> headers, List<List<Object>> rows) throws IOException {
        return generateExcel(sheetName, headers, rows, null);
    }

    public static byte[] generateExcel(String sheetName, List<String> headers, List<List<Object>> rows, BrandingProperties branding) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(sheetName);
            int rowIndex = 0;

            // Branding header block
            if (branding != null) {
                CellStyle titleStyle = workbook.createCellStyle();
                Font titleFont = workbook.createFont();
                titleFont.setBold(true);
                titleFont.setFontHeightInPoints((short) 13);
                titleStyle.setFont(titleFont);

                CellStyle subStyle = workbook.createCellStyle();
                Font subFont = workbook.createFont();
                subFont.setFontHeightInPoints((short) 10);
                subStyle.setFont(subFont);

                // Row 0: App name — Villa name
                Row r0 = sheet.createRow(rowIndex++);
                Cell c0 = r0.createCell(0);
                c0.setCellValue(branding.getAppName() + " — " + branding.getCompanyName());
                c0.setCellStyle(titleStyle);

                // Row 1: Location
                Row r1 = sheet.createRow(rowIndex++);
                Cell c1 = r1.createCell(0);
                c1.setCellValue("Location: " + branding.getAddress());
                c1.setCellStyle(subStyle);

                // Row 2: Contact
                Row r2 = sheet.createRow(rowIndex++);
                Cell c2 = r2.createCell(0);
                c2.setCellValue("Email: " + branding.getSupportEmail() + "  |  Phone: " + branding.getPhoneNumber());
                c2.setCellStyle(subStyle);

                // Row 3: Generated date
                Row r3 = sheet.createRow(rowIndex++);
                Cell c3 = r3.createCell(0);
                c3.setCellValue("Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                c3.setCellStyle(subStyle);

                // Blank separator
                sheet.createRow(rowIndex++);
            }

            // Column headers
            Row headerRow = sheet.createRow(rowIndex++);
            CellStyle headerStyle = createHeaderStyle(workbook);
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            CellStyle dataStyle = createDataStyle(workbook);
            for (List<Object> rowData : rows) {
                Row row = sheet.createRow(rowIndex++);
                for (int col = 0; col < rowData.size(); col++) {
                    Cell cell = row.createCell(col);
                    setCellValue(cell, rowData.get(col), dataStyle);
                }
            }

            // Auto-size all columns
            for (int i = 0; i < headers.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            log.error("Error generating Excel", e);
            throw e;
        }
    }

    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        return style;
    }

    private static CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private static void setCellValue(Cell cell, Object value, CellStyle style) {
        cell.setCellStyle(style);
        if (value == null)                    { cell.setCellValue(""); }
        else if (value instanceof String)     { cell.setCellValue((String) value); }
        else if (value instanceof Integer)    { cell.setCellValue(((Integer) value).doubleValue()); }
        else if (value instanceof Long)       { cell.setCellValue(((Long) value).doubleValue()); }
        else if (value instanceof Double)     { cell.setCellValue((Double) value); }
        else if (value instanceof BigDecimal) { cell.setCellValue(((BigDecimal) value).doubleValue()); }
        else if (value instanceof Boolean)    { cell.setCellValue((Boolean) value ? "Yes" : "No"); }
        else if (value instanceof LocalDate)  { cell.setCellValue(((LocalDate) value).toString()); }
        else if (value instanceof LocalDateTime) { cell.setCellValue(((LocalDateTime) value).toString()); }
        else if (value instanceof Date)       { cell.setCellValue((Date) value); }
        else                                  { cell.setCellValue(value.toString()); }
    }
}
