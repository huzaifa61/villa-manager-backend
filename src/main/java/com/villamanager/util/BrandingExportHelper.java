package com.villamanager.util;

import com.villamanager.config.BrandingProperties;

import java.util.ArrayList;
import java.util.List;

public final class BrandingExportHelper {

    private BrandingExportHelper() {}

    public static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    public static boolean isRealPhone(String phone) {
        if (!hasText(phone)) {
            return false;
        }
        String trimmed = phone.trim();
        return !trimmed.contains("XXX");
    }

    public static boolean isRealAddress(String address) {
        if (!hasText(address)) {
            return false;
        }
        String trimmed = address.trim();
        if (trimmed.startsWith("No. -") || trimmed.startsWith("No.-")) {
            return false;
        }
        return true;
    }

    /** Phone and address only; omitted when empty or placeholder. */
    public static String contactLine(BrandingProperties branding) {
        if (branding == null) {
            return "";
        }
        List<String> parts = new ArrayList<>();
        if (isRealPhone(branding.getPhoneNumber())) {
            parts.add("Phone: " + branding.getPhoneNumber().trim());
        }
        if (isRealAddress(branding.getAddress())) {
            parts.add("Address: " + branding.getAddress().trim());
        }
        return String.join(" | ", parts);
    }
}
