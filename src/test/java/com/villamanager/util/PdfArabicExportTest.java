package com.villamanager.util;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.villamanager.config.BrandingProperties;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PdfArabicExportTest {

  private static final String EXPECTED = "الريس";

  @Test
  void arabicVillaNameAppearsInPdf() throws Exception {
    BrandingProperties branding = new BrandingProperties();
    branding.setAppName("Villa Manager Pro");
    branding.setCompanyName(EXPECTED);

    List<String> headers = Arrays.asList("Villa", "ID", "Apartment");
    List<List<Object>> rows = List.of(
        Arrays.asList(EXPECTED, 2, "3"),
        Arrays.asList(EXPECTED, 3, "2")
    );

    byte[] pdf = PdfExportUtil.generatePdf("Apartments Report", headers, rows, branding);
    PdfReader reader = new PdfReader(pdf);
    String pageText = PdfTextExtractor.getTextFromPage(reader, 1);
    reader.close();

    assertTrue(
        pageText.contains(EXPECTED),
        "Extracted PDF text should include villa name. Got: " + pageText);
  }
}
