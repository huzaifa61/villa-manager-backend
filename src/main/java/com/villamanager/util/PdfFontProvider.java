package com.villamanager.util;

import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.FontSelector;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;

/**
 * Latin + Arabic fonts for PDF export. Latin headings use Noto Sans;
 * Arabic villa names use Noto Sans Arabic via {@link FontSelector}.
 */
@Slf4j
public final class PdfFontProvider {

    private static final String LATIN_REGULAR_PATH = "fonts/NotoSans-Regular.ttf";
    private static final String LATIN_BOLD_PATH = "fonts/NotoSans-Bold.ttf";
    private static final String ARABIC_REGULAR_PATH = "fonts/NotoSansArabic-Regular.ttf";
    private static final String ARABIC_BOLD_PATH = "fonts/NotoSansArabic-Bold.ttf";

    private static volatile BaseFont latinRegularBase;
    private static volatile BaseFont latinBoldBase;
    private static volatile BaseFont arabicRegularBase;
    private static volatile BaseFont arabicBoldBase;

    private PdfFontProvider() {}

    public static Phrase titlePhrase(String text) {
        return phrase(text, 18, true);
    }

    public static Phrase boldPhrase(String text) {
        return phrase(text, 11, true);
    }

    public static Phrase regularPhrase(String text) {
        return phrase(text, 10, false);
    }

    public static Phrase smallPhrase(String text) {
        return phrase(text, 8, false);
    }

    public static Phrase phrase(String text, float size, boolean bold) {
        FontSelector selector = new FontSelector();
        selector.addFont(font(bold ? latinBoldBase() : latinRegularBase(), size));
        selector.addFont(font(bold ? arabicBoldBase() : arabicRegularBase(), size));
        return selector.process(text == null ? "" : text);
    }

    private static Font font(BaseFont base, float size) {
        return new Font(base, size);
    }

    private static BaseFont latinRegularBase() {
        if (latinRegularBase == null) {
            latinRegularBase = loadBaseFont(LATIN_REGULAR_PATH);
        }
        return latinRegularBase;
    }

    private static BaseFont latinBoldBase() {
        if (latinBoldBase == null) {
            latinBoldBase = loadBaseFont(LATIN_BOLD_PATH);
        }
        return latinBoldBase;
    }

    private static BaseFont arabicRegularBase() {
        if (arabicRegularBase == null) {
            arabicRegularBase = loadBaseFont(ARABIC_REGULAR_PATH);
        }
        return arabicRegularBase;
    }

    private static BaseFont arabicBoldBase() {
        if (arabicBoldBase == null) {
            arabicBoldBase = loadBaseFont(ARABIC_BOLD_PATH);
        }
        return arabicBoldBase;
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
