package com.villamanager.controller;

import com.villamanager.dto.ApiResponse;
import com.villamanager.dto.InviteUserRequest;
import com.villamanager.dto.UserDto;
import com.villamanager.service.UserManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/users")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {

    @Autowired
    private UserManagementService userManagementService;

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
}
