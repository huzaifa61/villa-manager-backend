package com.villamanager.util;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.languages.ArabicLigaturizer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;

/**
 * Latin + Arabic fonts for PDF export.
 * Builds phrases per script segment instead of FontSelector (unreliable with embedded TTF bytes).
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

    public static boolean containsArabic(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        for (int i = 0; i < text.length(); i++) {
            if (isArabicChar(text.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    public static Phrase phrase(String text, float size, boolean bold) {
        if (text == null) {
            text = "";
        }
        Phrase phrase = new Phrase();
        BaseFont latinBase = bold ? latinBoldBase() : latinRegularBase();
        BaseFont arabicBase = bold ? arabicBoldBase() : arabicRegularBase();

        int index = 0;
        while (index < text.length()) {
            char current = text.charAt(index);
            boolean arabic = isArabicChar(current);
            int end = index + 1;
            while (end < text.length() && isArabicChar(text.charAt(end)) == arabic) {
                end++;
            }
            String segment = text.substring(index, end);
            if (arabic) {
                segment = shapeArabic(segment);
            }
            Font font = styledFont(arabic ? arabicBase : latinBase, size);
            phrase.add(new Chunk(segment, font));
            index = end;
        }
        return phrase;
    }

    private static Font styledFont(BaseFont base, float size) {
        Font font = new Font(base, size);
        font.setColor(BaseColor.BLACK);
        return font;
    }

    private static boolean isArabicChar(char character) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of(character);
        return block == Character.UnicodeBlock.ARABIC
                || block == Character.UnicodeBlock.ARABIC_PRESENTATION_FORMS_A
                || block == Character.UnicodeBlock.ARABIC_PRESENTATION_FORMS_B
                || block == Character.UnicodeBlock.ARABIC_SUPPLEMENT;
    }

    private static String shapeArabic(String text) {
        try {
            ArabicLigaturizer ligaturizer = new ArabicLigaturizer();
            return ligaturizer.process(text);
        } catch (Exception e) {
            log.warn("Arabic shaping failed, using raw text", e);
            return text;
        }
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
