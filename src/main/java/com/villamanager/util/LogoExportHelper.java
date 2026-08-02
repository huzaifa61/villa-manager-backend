package com.villamanager.util;

import org.apache.poi.ss.usermodel.Workbook;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public final class LogoExportHelper {

    private LogoExportHelper() {}

    public record ImageDimensions(int width, int height) {}

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

    public static ImageDimensions readImageDimensions(byte[] imageData) {
        if (imageData == null || imageData.length == 0) {
            return new ImageDimensions(80, 80);
        }
        try (ByteArrayInputStream input = new ByteArrayInputStream(imageData)) {
            BufferedImage image = ImageIO.read(input);
            if (image == null) {
                return new ImageDimensions(80, 80);
            }
            return new ImageDimensions(image.getWidth(), image.getHeight());
        } catch (IOException e) {
            return new ImageDimensions(80, 80);
        }
    }

    /** Scale image down to fit inside max box while preserving aspect ratio (no upscaling). */
    public static ImageDimensions fitWithin(byte[] imageData, int maxWidthPx, int maxHeightPx) {
        ImageDimensions source = readImageDimensions(imageData);
        if (source.width <= 0 || source.height <= 0) {
            return new ImageDimensions(maxWidthPx, maxHeightPx);
        }
        double scale = Math.min(
                (double) maxWidthPx / source.width,
                (double) maxHeightPx / source.height);
        if (scale > 1.0) {
            scale = 1.0;
        }
        int width = Math.max(1, (int) Math.round(source.width * scale));
        int height = Math.max(1, (int) Math.round(source.height * scale));
        return new ImageDimensions(width, height);
    }
}
