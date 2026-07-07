package com.villamanager.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
public class ExcelExportUtil {

    public static byte[] generateExcel(String sheetName, List<String> headers, List<List<Object>> rows) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(sheetName);
            
            // Create header row with styling
            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = createHeaderStyle(workbook);
            
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(headerStyle);
                sheet.autoSizeColumn(i);
            }
            
            // Add data rows
            CellStyle dataStyle = createDataStyle(workbook);
            for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
                Row row = sheet.createRow(rowIndex + 1);
                List<Object> rowData = rows.get(rowIndex);
                
                for (int colIndex = 0; colIndex < rowData.size(); colIndex++) {
                    Cell cell = row.createCell(colIndex);
                    Object value = rowData.get(colIndex);
                    
                    setCellValue(cell, value, dataStyle);
                }
            }
            
            // Auto-size columns
            for (int i = 0; i < headers.size(); i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Convert to bytes
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            log.error("Error generating Excel file", e);
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
        
        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Integer) {
            cell.setCellValue(((Integer) value).doubleValue());
            formatNumberCell(cell);
        } else if (value instanceof Long) {
            cell.setCellValue(((Long) value).doubleValue());
            formatNumberCell(cell);
        } else if (value instanceof Double) {
            cell.setCellValue((Double) value);
            formatNumberCell(cell);
        } else if (value instanceof BigDecimal) {
            cell.setCellValue(((BigDecimal) value).doubleValue());
            formatNumberCell(cell);
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value ? "Yes" : "No");
        } else if (value instanceof LocalDate) {
            cell.setCellValue(((LocalDate) value).toString());
        } else if (value instanceof LocalDateTime) {
            cell.setCellValue(((LocalDateTime) value).toString());
        } else if (value instanceof Date) {
            cell.setCellValue((Date) value);
        } else {
            cell.setCellValue(value.toString());
        }
    }

    private static void formatNumberCell(Cell cell) {
        Workbook workbook = cell.getSheet().getWorkbook();
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));
        cell.setCellStyle(style);
    }
}
