package com.villamanager.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.branding")
@Data
public class BrandingProperties {
    private String appName;
    private String appVersion;
    private String logoUrl;
    private String companyName;
    private String supportEmail;
    private String phoneNumber;
    private String address;
}
