package com.villamanager.util;

import org.apache.poi.ss.usermodel.Workbook;

public final class LogoExportHelper {

    private LogoExportHelper() {}

    public static boolean hasLogo(byte[] logoData) {
        return logoData != null && logoData.length > 0;
    }

    public static int workbookPictureType(Workbook workbook, String contentType) {
        if (contentType != null && contentType.toLowerCase().contains("png")) {
            return Workbook.PICTURE_TYPE_PNG;
        }
        if (contentType != null && (contentType.toLowerCase().contains("jpeg") || contentType.toLowerCase().contains("jpg"))) {
            return Workbook.PICTURE_TYPE_JPEG;
        }
        return Workbook.PICTURE_TYPE_PNG;
    }
}
