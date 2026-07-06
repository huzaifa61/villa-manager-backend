package com.villamanager.dto;

import com.villamanager.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InviteUserRequest {
    private String email;
    private String fullName;
    private String phoneNumber;
    private UserRole role;
    private Long villaId;
}
