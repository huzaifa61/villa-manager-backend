package com.villamanager.util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.villamanager.config.BrandingProperties;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
public class PdfExportUtil {

    public static byte[] generatePdf(String title, List<String> headers, List<List<Object>> rows, BrandingProperties branding) throws Exception {
        try {
            Document document = new Document(PageSize.A4.rotate());
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, outputStream);

            document.open();

            addHeader(document, title, branding);

            PdfPTable table = new PdfPTable(headers.size());
            table.setWidthPercentage(100);

            for (String header : headers) {
                table.addCell(textCell(header, true));
            }

            for (List<Object> row : rows) {
                for (Object value : row) {
                    table.addCell(textCell(formatValue(value), false));
                }
            }

            document.add(table);
            addFooter(document, branding);

            document.close();
            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error("Error generating PDF", e);
            throw e;
        }
    }

    private static PdfPCell textCell(String text, boolean bold) {
        float size = bold ? 11f : 10f;
        Phrase phrase;
        boolean mostlyArabic = PdfFontProvider.isMostlyArabic(text);
        if (mostlyArabic) {
            phrase = PdfFontProvider.arabicCellPhrase(text, size, bold);
        } else if (PdfFontProvider.containsArabic(text)) {
            phrase = PdfFontProvider.phrase(text, size, bold);
        } else {
            phrase = bold ? PdfFontProvider.boldPhrase(text) : PdfFontProvider.regularPhrase(text);
        }

        PdfPCell cell = new PdfPCell(phrase);
        cell.setPadding(bold ? 8 : 5);
        cell.setBackgroundColor(bold ? BaseColor.LIGHT_GRAY : BaseColor.WHITE);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        if (mostlyArabic) {
            cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
        }
        return cell;
    }

    private static void addHeader(Document document, String title, BrandingProperties branding) throws DocumentException {
        if (LogoExportHelper.hasLogo(branding.getLogoData())) {
            try {
                Image logo = Image.getInstance(branding.getLogoData());
                LogoExportHelper.ImageDimensions logoSize = LogoExportHelper.fitWithin(
                        branding.getLogoData(), 110, 110);
                logo.scaleToFit(logoSize.width(), logoSize.height());
                logo.setAlignment(Element.ALIGN_CENTER);
                document.add(logo);
                document.add(Chunk.NEWLINE);
            } catch (Exception e) {
                log.warn("Could not embed logo in PDF export", e);
            }
        }

        if (BrandingExportHelper.hasText(branding.getAppName())) {
            Paragraph appName = new Paragraph(PdfFontProvider.titlePhrase(branding.getAppName().trim()));
            appName.setAlignment(Element.ALIGN_CENTER);
            document.add(appName);
        }

        String villaName = branding.getCompanyName();
        if (BrandingExportHelper.hasText(villaName)) {
            String trimmedName = villaName.trim();
            Paragraph villaLine;
            if (PdfFontProvider.isMostlyArabic(trimmedName)) {
                villaLine = new Paragraph(PdfFontProvider.mixedPhrase("Villa: ", trimmedName, 14, true));
            } else {
                villaLine = new Paragraph(PdfFontProvider.boldPhrase("Villa: " + trimmedName));
            }
            villaLine.setAlignment(Element.ALIGN_CENTER);
            villaLine.setSpacingBefore(6);
            document.add(villaLine);
        }

        if (BrandingExportHelper.hasText(title)) {
            Paragraph reportLine = new Paragraph(PdfFontProvider.boldPhrase("Report: " + title.trim()));
            reportLine.setAlignment(Element.ALIGN_CENTER);
            reportLine.setSpacingBefore(6);
            document.add(reportLine);
        }

        String generated = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        Paragraph datePara = new Paragraph(PdfFontProvider.smallPhrase("Generated: " + generated));
        datePara.setAlignment(Element.ALIGN_RIGHT);
        datePara.setSpacingBefore(8);
        datePara.setSpacingAfter(16);
        document.add(datePara);
    }

    private static void addFooter(Document document, BrandingProperties branding) throws DocumentException {
        String contact = BrandingExportHelper.contactLine(branding);
        if (contact.isBlank()) {
            return;
        }
        document.add(new Paragraph("\n"));
        Paragraph footer = new Paragraph(PdfFontProvider.smallPhrase(contact));
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(20);
        document.add(footer);
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
