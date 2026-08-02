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

            addHeader(document, title, branding);

            PdfPTable table = new PdfPTable(headers.size());
            table.setWidthPercentage(100);

            for (String header : headers) {
                PdfPCell cell = new PdfPCell(PdfFontProvider.boldPhrase(header));
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                cell.setPadding(8);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            for (List<Object> row : rows) {
                for (Object value : row) {
                    PdfPCell cell = new PdfPCell(PdfFontProvider.regularPhrase(formatValue(value)));
                    cell.setPadding(5);
                    table.addCell(cell);
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

        String villaName = branding.getCompanyName();
        if (BrandingExportHelper.hasText(villaName)) {
            Paragraph companyName = new Paragraph(PdfFontProvider.boldPhrase("Villa: " + villaName.trim()));
            companyName.setAlignment(Element.ALIGN_CENTER);
            document.add(companyName);
        }

        Paragraph titlePara = new Paragraph(PdfFontProvider.titlePhrase(title));
        titlePara.setAlignment(Element.ALIGN_CENTER);
        titlePara.setSpacingBefore(10);
        titlePara.setSpacingAfter(10);
        document.add(titlePara);

        Paragraph datePara = new Paragraph(PdfFontProvider.smallPhrase("Generated: " + new Date()));
        datePara.setAlignment(Element.ALIGN_RIGHT);
        datePara.setSpacingAfter(20);
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
