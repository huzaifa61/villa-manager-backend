package com.villamanager.controller;

import com.villamanager.config.BrandingProperties;
import com.villamanager.dto.ApiResponse;
import com.villamanager.service.ExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/v1/app")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class BrandingController {

    private final BrandingProperties brandingProperties;
    private final ExportService exportService;

    @GetMapping("/branding")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBranding() {
        Map<String, Object> branding = new HashMap<>();
        branding.put("appName", brandingProperties.getAppName());
        branding.put("appVersion", brandingProperties.getAppVersion());
        branding.put("logoUrl", brandingProperties.getLogoUrl());
        branding.put("companyName", brandingProperties.getCompanyName());
        branding.put("supportEmail", brandingProperties.getSupportEmail());
        branding.put("phoneNumber", brandingProperties.getPhoneNumber());
        branding.put("address", brandingProperties.getAddress());

        return ResponseEntity.ok(ApiResponse.success("Branding information retrieved successfully", branding));
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
