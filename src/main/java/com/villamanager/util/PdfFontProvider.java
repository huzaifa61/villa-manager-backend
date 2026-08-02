package com.villamanager.util;

import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.BaseFont;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;

/**
 * Unicode fonts for PDF export (Arabic, Latin, and common symbols).
 * Helvetica cannot render Arabic script and shows blank text.
 */
@Slf4j
public final class PdfFontProvider {

    private static final String REGULAR_PATH = "fonts/NotoSansArabic-Regular.ttf";
    private static final String BOLD_PATH = "fonts/NotoSansArabic-Bold.ttf";

    private static volatile BaseFont regularBase;
    private static volatile BaseFont boldBase;

    private PdfFontProvider() {}

    public static Font titleFont() {
        return sized(boldBase(), 18);
    }

    public static Font boldFont() {
        return sized(boldBase(), 11);
    }

    public static Font regularFont() {
        return sized(regularBase(), 10);
    }

    public static Font smallFont() {
        return sized(regularBase(), 8);
    }

    private static Font sized(BaseFont base, float size) {
        return new Font(base, size);
    }

    private static BaseFont regularBase() {
        if (regularBase == null) {
            regularBase = loadBaseFont(REGULAR_PATH);
        }
        return regularBase;
    }

    private static BaseFont boldBase() {
        if (boldBase == null) {
            boldBase = loadBaseFont(BOLD_PATH);
        }
        return boldBase;
    }

    private static BaseFont loadBaseFont(String classpathPath) {
        try (InputStream input = PdfFontProvider.class.getClassLoader().getResourceAsStream(classpathPath)) {
            if (input == null) {
                log.warn("PDF font not found at {}, falling back to Helvetica", classpathPath);
                return BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
            }
            byte[] bytes = input.readAllBytes();
            return BaseFont.createFont(
                    classpathPath,
                    BaseFont.IDENTITY_H,
                    BaseFont.EMBEDDED,
                    true,
                    bytes,
                    null);
        } catch (IOException | com.itextpdf.text.DocumentException e) {
            log.error("Failed to load PDF font {}", classpathPath, e);
            try {
                return BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
            } catch (Exception fallbackError) {
                throw new IllegalStateException("Could not initialize PDF fonts", fallbackError);
            }
        }
    }
}
