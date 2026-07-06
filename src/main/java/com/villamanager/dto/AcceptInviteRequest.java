package com.villamanager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AcceptInviteRequest {
    private String token;
    private String fullName;
    private String phoneNumber;
    private String password;
}
