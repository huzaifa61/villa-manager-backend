package com.villamanager.controller;

import com.villamanager.dto.ApiResponse;
import com.villamanager.dto.InviteUserRequest;
import com.villamanager.dto.UserDto;
import com.villamanager.entity.User;
import com.villamanager.entity.UserRole;
import com.villamanager.exception.ResourceNotFoundException;
import com.villamanager.repository.UserRepository;
import com.villamanager.service.AccessControlService;
import com.villamanager.service.UserManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/users")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {

    @Autowired private UserManagementService userManagementService;
    @Autowired private AccessControlService accessControlService;
    @Autowired private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserDto>>> listUsers() {
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", userManagementService.listUsers()));
    }

    @PostMapping("/invite")
    public ResponseEntity<ApiResponse<UserDto>> inviteUser(@RequestBody InviteUserRequest request) {
        UserDto user = userManagementService.inviteUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Invitation sent successfully", user));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserDto>> updateUser(@PathVariable Long userId, @RequestBody InviteUserRequest request) {
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", userManagementService.updateUser(userId, request)));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long userId) {
        userManagementService.deleteUser(userId);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully", null));
    }

    @PostMapping("/push-token")
    public ResponseEntity<ApiResponse<Void>> savePushToken(@RequestBody Map<String, String> body) {
        User user = accessControlService.currentUser();
        String token = body.get("token");
        if (token != null && !token.isBlank()) {
            user.setPushToken(token);
            userRepository.save(user);
        }
        return ResponseEntity.ok(ApiResponse.success("Push token saved", null));
    }

    // ── Subscription Management (General Manager only) ────────────────────────

    @PutMapping("/{userId}/subscription")
    public ResponseEntity<ApiResponse<UserDto>> updateSubscription(
            @PathVariable Long userId,
            @RequestBody Map<String, Object> body) {
        accessControlService.requireGeneralManager();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.getRole() != UserRole.VILLA_MANAGER) {
            throw new com.villamanager.exception.InvalidOperationException("Subscription can only be set for Villa Managers");
        }
        if (body.containsKey("subscriptionExpiresAt") && body.get("subscriptionExpiresAt") != null) {
            user.setSubscriptionExpiresAt(LocalDateTime.parse(body.get("subscriptionExpiresAt").toString()));
        }
        if (body.containsKey("maxViewers") && body.get("maxViewers") != null) {
            user.setMaxViewers(Integer.parseInt(body.get("maxViewers").toString()));
        }
        User saved = userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success("Subscription updated successfully", userManagementService.mapToDto(saved)));
    }

    @DeleteMapping("/{userId}/subscription")
    public ResponseEntity<ApiResponse<UserDto>> revokeSubscription(@PathVariable Long userId) {
        accessControlService.requireGeneralManager();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setSubscriptionExpiresAt(LocalDateTime.now().minusSeconds(1)); // expire immediately
        User saved = userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success("Subscription revoked", userManagementService.mapToDto(saved)));
    }
}
