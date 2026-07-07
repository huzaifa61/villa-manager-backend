package com.villamanager.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.export")
@Data
public class ExportProperties {
    private Integer maxRecords = 10000;
    private Integer chunkSize = 500;
    private String tempDir = "/tmp/villa-exports";
}
