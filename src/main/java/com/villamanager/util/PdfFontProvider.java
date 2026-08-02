package com.villamanager.util;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * Latin + Arabic fonts for PDF export.
 * Paragraphs and Arabic table cells use logical-order Unicode with Noto Sans Arabic.
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

    /** Load embedded fonts at startup so PDF export fails fast if fonts are missing from the JAR. */
    public static void warmUp() {
        latinRegularBase();
        latinBoldBase();
        arabicRegularBase();
        arabicBoldBase();
    }

    public static Phrase titlePhrase(String text) {
        return latinPhrase(text, 18, true);
    }

    public static Phrase boldPhrase(String text) {
        return latinPhrase(text, 11, true);
    }

    public static Phrase regularPhrase(String text) {
        return latinPhrase(text, 10, false);
    }

    public static Phrase smallPhrase(String text) {
        return latinPhrase(text, 8, false);
    }

    public static Phrase arabicPhrase(String text, float size, boolean bold) {
        Phrase phrase = new Phrase();
        if (text == null || text.isBlank()) {
            return phrase;
        }
        Font arabic = styledFont(bold ? arabicBoldBase() : arabicRegularBase(), size);
        phrase.add(new Chunk(text.trim(), arabic));
        return phrase;
    }

    public static Phrase latinPhrase(String text, float size, boolean bold) {
        Phrase phrase = new Phrase();
        if (text == null) {
            return phrase;
        }
        Font latin = styledFont(bold ? latinBoldBase() : latinRegularBase(), size);
        phrase.add(new Chunk(text, latin));
        return phrase;
    }

    /** Mixed Latin label + Arabic value in logical order, each in its own font. */
    public static Phrase mixedPhrase(String latinPart, String arabicPart, float size, boolean bold) {
        Phrase phrase = new Phrase();
        if (latinPart != null && !latinPart.isEmpty()) {
            phrase.add(new Chunk(latinPart, styledFont(bold ? latinBoldBase() : latinRegularBase(), size)));
        }
        if (arabicPart != null && !arabicPart.isBlank()) {
            phrase.add(new Chunk(arabicPart.trim(), styledFont(bold ? arabicBoldBase() : arabicRegularBase(), size)));
        }
        return phrase;
    }

    public static Phrase phrase(String text, float size, boolean bold) {
        if (text == null) {
            text = "";
        }
        if (!containsArabic(text)) {
            return latinPhrase(text, size, bold);
        }
        if (isMostlyArabic(text)) {
            return arabicPhrase(text, size, bold);
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
            Font font = styledFont(arabic ? arabicBase : latinBase, size);
            phrase.add(new Chunk(segment, font));
            index = end;
        }
        return phrase;
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

    public static boolean isMostlyArabic(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        int arabicCount = 0;
        int latinCount = 0;
        for (int i = 0; i < text.length(); i++) {
            char character = text.charAt(i);
            if (isArabicChar(character)) {
                arabicCount++;
            } else if (Character.isLetter(character)) {
                latinCount++;
            }
        }
        return arabicCount > 0 && arabicCount >= latinCount;
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
                return helveticaFallback();
            }
            File tempFile = Files.createTempFile("villa-pdf-font-", ".ttf").toFile();
            Files.write(tempFile.toPath(), input.readAllBytes());
            tempFile.deleteOnExit();
            return BaseFont.createFont(tempFile.getAbsolutePath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        } catch (IOException | com.itextpdf.text.DocumentException e) {
            log.error("Failed to load PDF font {}", classpathPath, e);
            return helveticaFallback();
        }
    }

    private static BaseFont helveticaFallback() {
        try {
            return BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
        } catch (Exception e) {
            throw new IllegalStateException("Could not initialize PDF fonts", e);
        }
    }
}
