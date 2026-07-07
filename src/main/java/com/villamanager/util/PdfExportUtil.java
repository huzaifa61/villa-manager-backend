package com.villamanager.util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.villamanager.config.BrandingProperties;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Slf4j
public class PdfExportUtil {

    public static byte[] generatePdf(String title, List<String> headers, List<List<Object>> rows, BrandingProperties branding) throws Exception {
        try {
            Document document = new Document(PageSize.A4.rotate());
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, outputStream);
            
            document.open();
            
            // Add branding header
            addHeader(document, title, branding);
            
            // Add table
            PdfPTable table = new PdfPTable(headers.size());
            table.setWidthPercentage(100);
            
            // Add headers
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, getBoldFont()));
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                cell.setPadding(8);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }
            
            // Add data rows
            for (List<Object> row : rows) {
                for (Object value : row) {
                    PdfPCell cell = new PdfPCell(new Phrase(formatValue(value), getRegularFont()));
                    cell.setPadding(5);
                    table.addCell(cell);
                }
            }
            
            document.add(table);
            
            // Add footer
            addFooter(document, branding);
            
            document.close();
            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error("Error generating PDF", e);
            throw e;
        }
    }

    private static void addHeader(Document document, String title, BrandingProperties branding) throws DocumentException {
        // Company name and logo placeholder
        Paragraph companyName = new Paragraph(branding.getCompanyName(), getBoldFont());
        companyName.setAlignment(Element.ALIGN_CENTER);
        document.add(companyName);
        
        // Title
        Paragraph titlePara = new Paragraph(title, getTitleFont());
        titlePara.setAlignment(Element.ALIGN_CENTER);
        titlePara.setSpacingBefore(10);
        titlePara.setSpacingAfter(10);
        document.add(titlePara);
        
        // Generated date
        Paragraph datePara = new Paragraph("Generated: " + new Date(), getSmallFont());
        datePara.setAlignment(Element.ALIGN_RIGHT);
        datePara.setSpacingAfter(20);
        document.add(datePara);
    }

    private static void addFooter(Document document, BrandingProperties branding) throws DocumentException {
        document.add(new Paragraph("\n"));
        
        Paragraph footer = new Paragraph();
        footer.add(new Phrase("Email: " + branding.getSupportEmail() + " | ", getSmallFont()));
        footer.add(new Phrase("Phone: " + branding.getPhoneNumber() + " | ", getSmallFont()));
        footer.add(new Phrase("Address: " + branding.getAddress(), getSmallFont()));
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(20);
        
        document.add(footer);
    }

    private static Font getTitleFont() {
        return new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
    }

    private static Font getBoldFont() {
        return new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD);
    }

    private static Font getRegularFont() {
        return new Font(Font.FontFamily.HELVETICA, 10);
    }

    private static Font getSmallFont() {
        return new Font(Font.FontFamily.HELVETICA, 8);
    }

    private static String formatValue(Object value) {
        if (value == null) {
            return "";
        } else if (value instanceof BigDecimal) {
            return String.format("%.2f", (BigDecimal) value);
        } else if (value instanceof Double) {
            return String.format("%.2f", (Double) value);
        } else if (value instanceof LocalDate) {
            return ((LocalDate) value).toString();
        } else if (value instanceof LocalDateTime) {
            return ((LocalDateTime) value).toString();
        } else {
            return value.toString();
        }
    }
}
