package com.villamanager.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "app_branding")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppBranding {

    public static final long SINGLETON_ID = 1L;

    @Id
    private Long id = SINGLETON_ID;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "logo_data")
    private byte[] logoData;

    @Column(name = "logo_content_type", length = 100)
    private String logoContentType;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
}
