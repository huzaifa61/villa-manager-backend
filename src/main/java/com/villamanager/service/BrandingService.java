package com.villamanager.service;

import com.villamanager.entity.AppBranding;
import com.villamanager.entity.Villa;
import com.villamanager.exception.InvalidOperationException;
import com.villamanager.repository.AppBrandingRepository;
import com.villamanager.repository.VillaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BrandingService {

    private static final int MAX_LOGO_BYTES = 2 * 1024 * 1024;
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/png", "image/jpeg", "image/jpg", "image/webp", "image/gif"
    );

    private final AppBrandingRepository appBrandingRepository;
    private final VillaRepository villaRepository;

    @Transactional(readOnly = true)
    public AppBranding getAppBranding() {
        return appBrandingRepository.findById(AppBranding.SINGLETON_ID)
                .orElseGet(this::createDefaultBranding);
    }

    @Transactional
    public AppBranding saveGlobalLogo(byte[] data, String contentType) {
        validateLogo(data, contentType);
        AppBranding branding = getAppBranding();
        branding.setLogoData(data);
        branding.setLogoContentType(normalizeContentType(contentType));
        branding.setUpdatedAt(LocalDateTime.now());
        return appBrandingRepository.save(branding);
    }

    @Transactional
    public void deleteGlobalLogo() {
        AppBranding branding = getAppBranding();
        branding.setLogoData(null);
        branding.setLogoContentType(null);
        branding.setUpdatedAt(LocalDateTime.now());
        appBrandingRepository.save(branding);
    }

    @Transactional
    public void saveVillaLogo(Long villaId, byte[] data, String contentType) {
        validateLogo(data, contentType);
        Villa villa = villaRepository.findById(villaId)
                .orElseThrow(() -> new InvalidOperationException("Villa not found"));
        villa.setLogoData(data);
        villa.setLogoContentType(normalizeContentType(contentType));
        villa.setUpdatedAt(LocalDateTime.now());
        villaRepository.save(villa);
    }

    @Transactional
    public void deleteVillaLogo(Long villaId) {
        Villa villa = villaRepository.findById(villaId)
                .orElseThrow(() -> new InvalidOperationException("Villa not found"));
        villa.setLogoData(null);
        villa.setLogoContentType(null);
        villa.setUpdatedAt(LocalDateTime.now());
        villaRepository.save(villa);
    }

    @Transactional(readOnly = true)
    public byte[] resolveLogoData(Villa villa) {
        if (villa != null && villa.getId() != null) {
            Villa loaded = villaRepository.findById(villa.getId()).orElse(null);
            if (loaded != null && loaded.getLogoData() != null && loaded.getLogoData().length > 0) {
                return loaded.getLogoData();
            }
        }
        AppBranding app = getAppBranding();
        return app.getLogoData();
    }

    @Transactional(readOnly = true)
    public String resolveLogoContentType(Villa villa) {
        if (villa != null && villa.getId() != null) {
            Villa loaded = villaRepository.findById(villa.getId()).orElse(null);
            if (loaded != null && loaded.getLogoData() != null && loaded.getLogoData().length > 0) {
                return loaded.getLogoContentType();
            }
        }
        AppBranding app = getAppBranding();
        return app.getLogoContentType();
    }

    @Transactional(readOnly = true)
    public boolean hasResolvableLogo(Villa villa) {
        byte[] data = resolveLogoData(villa);
        return data != null && data.length > 0;
    }

    private AppBranding createDefaultBranding() {
        AppBranding branding = new AppBranding();
        branding.setId(AppBranding.SINGLETON_ID);
        branding.setUpdatedAt(LocalDateTime.now());
        return appBrandingRepository.save(branding);
    }

    private void validateLogo(byte[] data, String contentType) {
        if (data == null || data.length == 0) {
            throw new InvalidOperationException("Logo file is empty");
        }
        if (data.length > MAX_LOGO_BYTES) {
            throw new InvalidOperationException("Logo must be 2 MB or smaller");
        }
        String normalized = normalizeContentType(contentType);
        if (!ALLOWED_TYPES.contains(normalized)) {
            throw new InvalidOperationException("Logo must be PNG, JPEG, WEBP, or GIF");
        }
    }

    private String normalizeContentType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return "image/png";
        }
        return contentType.trim().toLowerCase(Locale.ROOT);
    }
}
