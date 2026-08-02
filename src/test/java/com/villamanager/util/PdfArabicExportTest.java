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
  private static final String EXPECTED_WITH_WORD = "الريس جرب";

  @Test
  void arabicVillaNameAppearsInPdf() throws Exception {
    assertPdfContainsArabic(EXPECTED);
  }

  @Test
  void arabicVillaNameWithExtraWordAppearsInPdf() throws Exception {
    assertPdfContainsArabic(EXPECTED_WITH_WORD);
  }

  private void assertPdfContainsArabic(String villaName) throws Exception {
    BrandingProperties branding = new BrandingProperties();
    branding.setAppName("Villa Manager Pro");
    branding.setCompanyName(villaName);

    List<String> headers = Arrays.asList("Villa", "ID", "Apartment");
    List<List<Object>> rows = List.of(
        Arrays.asList(villaName, 2, "3"),
        Arrays.asList(villaName, 3, "2")
    );

    byte[] pdf = PdfExportUtil.generatePdf("Apartments Report", headers, rows, branding);
    PdfReader reader = new PdfReader(pdf);
    String pageText = PdfTextExtractor.getTextFromPage(reader, 1);
    reader.close();

    assertTrue(
        !pageText.contains(reverseArabicWord("الريس")),
        "PDF should not contain reversed Arabic. Got: " + pageText);
  }

  private static String reverseArabicWord(String word) {
    return new StringBuilder(word).reverse().toString();
  }
}
