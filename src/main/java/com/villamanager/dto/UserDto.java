package com.villamanager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.villamanager.entity.UserRole;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private Long id;
    private String email;
    private String fullName;
    private String phoneNumber;
    private UserRole role;
    private Long villaId;
    private Boolean isActive;
    private String invitationStatus;
    private LocalDateTime subscriptionExpiresAt;
    private Integer maxViewers;
    private Boolean subscriptionExpired;
}
