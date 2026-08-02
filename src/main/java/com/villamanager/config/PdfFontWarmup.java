package com.villamanager.config;

import com.villamanager.util.PdfFontProvider;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PdfFontWarmup {

    @PostConstruct
    public void warmUpPdfFonts() {
        try {
            PdfFontProvider.warmUp();
            log.info("PDF export fonts loaded (Latin + Arabic)");
        } catch (Exception e) {
            log.error("PDF export fonts failed to load — Arabic villa names will render incorrectly", e);
        }
    }
}
