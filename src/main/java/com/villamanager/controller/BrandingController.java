package com.villamanager.controller;

import com.villamanager.config.BrandingProperties;
import com.villamanager.dto.ApiResponse;
import com.villamanager.entity.AppBranding;
import com.villamanager.service.AccessControlService;
import com.villamanager.service.BrandingService;
import com.villamanager.service.ExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/v1/app")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class BrandingController {

    private final BrandingProperties brandingProperties;
    private final ExportService exportService;
    private final BrandingService brandingService;
    private final AccessControlService accessControlService;

    @GetMapping("/branding")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBranding() {
        AppBranding appBranding = brandingService.getAppBranding();
        Map<String, Object> branding = new HashMap<>();
        branding.put("appName", brandingProperties.getAppName());
        branding.put("appVersion", brandingProperties.getAppVersion());
        branding.put("logoUrl", brandingProperties.getLogoUrl());
        branding.put("companyName", brandingProperties.getCompanyName());
        branding.put("supportEmail", brandingProperties.getSupportEmail());
        branding.put("phoneNumber", brandingProperties.getPhoneNumber());
        branding.put("address", brandingProperties.getAddress());
        branding.put("hasLogo", appBranding.getLogoData() != null && appBranding.getLogoData().length > 0);
        branding.put("logoApiPath", "/v1/app/branding/logo");

        return ResponseEntity.ok(ApiResponse.success("Branding information retrieved successfully", branding));
    }

    @PostMapping(value = "/branding/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadLogo(@RequestPart("file") MultipartFile file) {
        accessControlService.requireGeneralManager();
        try {
            brandingService.saveGlobalLogo(file.getBytes(), file.getContentType());
            Map<String, Object> body = Map.of(
                    "hasLogo", true,
                    "logoApiPath", "/v1/app/branding/logo",
                    "contentType", file.getContentType()
            );
            return ResponseEntity.ok(ApiResponse.success("Logo uploaded successfully", body));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage(), e.getMessage()));
        }
    }

    @GetMapping("/branding/logo")
    public ResponseEntity<byte[]> getLogo() {
        AppBranding branding = brandingService.getAppBranding();
        if (branding.getLogoData() == null || branding.getLogoData().length == 0) {
            return ResponseEntity.notFound().build();
        }
        String contentType = branding.getLogoContentType() != null ? branding.getLogoContentType() : MediaType.IMAGE_PNG_VALUE;
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .header(HttpHeaders.CACHE_CONTROL, "private, max-age=3600")
                .body(branding.getLogoData());
    }

    @DeleteMapping("/branding/logo")
    public ResponseEntity<ApiResponse<Void>> deleteLogo() {
        accessControlService.requireGeneralManager();
        brandingService.deleteGlobalLogo();
        return ResponseEntity.ok(ApiResponse.success("Logo deleted successfully", null));
    }

    @GetMapping("/info")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAppInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("appName", brandingProperties.getAppName());
        info.put("appVersion", brandingProperties.getAppVersion());
        info.put("companyName", brandingProperties.getCompanyName());
        info.put("exportInfo", exportService.getExportInfo());

        return ResponseEntity.ok(ApiResponse.success("App information retrieved successfully", info));
    }

    @GetMapping("/export-info")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getExportInfo() {
        Map<String, Object> info = exportService.getExportInfo();
        return ResponseEntity.ok(ApiResponse.success("Export information retrieved successfully", info));
    }
}
