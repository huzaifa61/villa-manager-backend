package com.villamanager.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String password;

    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(name = "villa_id")
    private Long villaId;

    @Column(name = "invite_token", unique = true)
    private String inviteToken;

    @Column(name = "invite_expires_at")
    private LocalDateTime inviteExpiresAt;

    @Column(name = "invite_accepted_at")
    private LocalDateTime inviteAcceptedAt;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "push_token")
    private String pushToken;

    // ── Subscription fields (set by General Manager for Villa Managers) ──────
    @Column(name = "subscription_expires_at")
    private LocalDateTime subscriptionExpiresAt;

    @Column(name = "max_viewers")
    private Integer maxViewers = 5; // default 5 viewers per villa manager

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
