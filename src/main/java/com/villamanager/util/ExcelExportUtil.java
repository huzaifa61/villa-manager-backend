package com.villamanager.util;

import com.villamanager.config.BrandingProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.util.Units;
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

    private static final int LOGO_MAX_WIDTH_PX = 88;
    private static final int LOGO_MAX_HEIGHT_PX = 88;
    private static final int LOGO_PADDING_PX = 4;

    public static byte[] generateExcel(String sheetName, List<String> headers, List<List<Object>> rows) throws IOException {
        return generateExcel(sheetName, headers, rows, null);
    }

    public static byte[] generateExcel(String sheetName, List<String> headers, List<List<Object>> rows, BrandingProperties branding) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(sheetName);
            int rowIndex = 0;

            // Branding header block
            if (branding != null) {
                int textColumn = 0;
                if (LogoExportHelper.hasLogo(branding.getLogoData())) {
                    try {
                        LogoExportHelper.ImageDimensions logoSize = LogoExportHelper.fitWithin(
                                branding.getLogoData(), LOGO_MAX_WIDTH_PX, LOGO_MAX_HEIGHT_PX);
                        int pictureType = LogoExportHelper.workbookPictureType(workbook, branding.getLogoContentType());
                        int pictureIdx = workbook.addPicture(branding.getLogoData(), pictureType);
                        Drawing<?> drawing = sheet.createDrawingPatriarch();
                        ClientAnchor anchor = workbook.getCreationHelper().createClientAnchor();
                        anchor.setCol1(0);
                        anchor.setRow1(rowIndex);
                        anchor.setDx1(Units.pixelToEMU(LOGO_PADDING_PX));
                        anchor.setDy1(Units.pixelToEMU(LOGO_PADDING_PX));
                        anchor.setCol2(0);
                        anchor.setRow2(rowIndex);
                        anchor.setDx2(Units.pixelToEMU(logoSize.width() + LOGO_PADDING_PX));
                        anchor.setDy2(Units.pixelToEMU(logoSize.height() + LOGO_PADDING_PX));
                        anchor.setAnchorType(ClientAnchor.AnchorType.DONT_MOVE_AND_RESIZE);
                        drawing.createPicture(anchor, pictureIdx);

                        int logoColumnChars = Math.max(10, (int) Math.ceil((logoSize.width() + LOGO_PADDING_PX * 2) / 7.0) + 1);
                        sheet.setColumnWidth(0, logoColumnChars * 256);
                        float logoRowHeightPts = Math.max(15f, (logoSize.height() + LOGO_PADDING_PX * 2) * 0.75f);
                        Row logoRow = sheet.getRow(rowIndex);
                        if (logoRow == null) {
                            logoRow = sheet.createRow(rowIndex);
                        }
                        logoRow.setHeightInPoints(logoRowHeightPts);
                        textColumn = 1;
                    } catch (Exception e) {
                        log.warn("Could not embed logo in Excel export", e);
                    }
                }

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
                Cell c0 = r0.createCell(textColumn);
                c0.setCellValue(branding.getAppName() + " — " + branding.getCompanyName());
                c0.setCellStyle(titleStyle);

                // Row 1: Location (only when meaningful)
                if (BrandingExportHelper.isRealAddress(branding.getAddress())) {
                    Row r1 = sheet.createRow(rowIndex++);
                    Cell c1 = r1.createCell(textColumn);
                    c1.setCellValue("Location: " + branding.getAddress().trim());
                    c1.setCellStyle(subStyle);
                }

                // Row 2: Contact (only non-empty fields)
                String contact = BrandingExportHelper.contactLine(branding);
                if (!contact.isBlank()) {
                    Row r2 = sheet.createRow(rowIndex++);
                    Cell c2 = r2.createCell(textColumn);
                    c2.setCellValue(contact);
                    c2.setCellStyle(subStyle);
                }

                // Generated date
                Row r3 = sheet.createRow(rowIndex++);
                Cell c3 = r3.createCell(textColumn);
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

            // Column widths without AWT (autoSizeColumn breaks on headless Linux/Railway)
            applyColumnWidths(sheet, headers, rows);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            log.error("Error generating Excel", e);
            throw e;
        }
    }

    private static void applyColumnWidths(Sheet sheet, List<String> headers, List<List<Object>> rows) {
        int columnCount = headers.size();
        int[] maxChars = new int[columnCount];
        for (int i = 0; i < columnCount; i++) {
            maxChars[i] = headers.get(i) != null ? headers.get(i).length() : 10;
        }
        for (List<Object> rowData : rows) {
            for (int col = 0; col < columnCount && col < rowData.size(); col++) {
                String text = rowData.get(col) == null ? "" : String.valueOf(rowData.get(col));
                maxChars[col] = Math.max(maxChars[col], text.length());
            }
        }
        for (int i = 0; i < columnCount; i++) {
            int chars = Math.min(60, Math.max(12, maxChars[i] + 2));
            sheet.setColumnWidth(i, chars * 256);
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
