package com.villamanager.service;

import com.villamanager.dto.InviteUserRequest;
import com.villamanager.dto.UserDto;
import com.villamanager.entity.User;
import com.villamanager.entity.UserRole;
import com.villamanager.exception.InvalidOperationException;
import com.villamanager.exception.ResourceNotFoundException;
import com.villamanager.repository.UserRepository;
import com.villamanager.repository.VillaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class UserManagementService {

    @Autowired private AccessControlService accessControlService;
    @Autowired private EmailService emailService;
    @Autowired private UserRepository userRepository;
    @Autowired private VillaRepository villaRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    public List<UserDto> listUsers() {
        User current = accessControlService.currentUser();
        if (current.getRole() == UserRole.GENERAL_MANAGER) {
            return userRepository.findAll().stream().map(this::mapToDto).toList();
        }
        if (current.getRole() == UserRole.VILLA_MANAGER) {
            return userRepository.findByVillaId(current.getVillaId()).stream().map(this::mapToDto).toList();
        }
        return List.of(mapToDto(current));
    }

    public UserDto inviteUser(InviteUserRequest request) {
        User current = accessControlService.currentUser();
        if (request.getEmail() == null || request.getEmail().trim().isBlank()) {
            throw new InvalidOperationException("Email is required");
        }

        UserRole requestedRole = request.getRole() != null ? request.getRole() : UserRole.VIEWER;
        Long villaId = request.getVillaId();

        if (current.getRole() == UserRole.GENERAL_MANAGER) {
            if (requestedRole != UserRole.GENERAL_MANAGER && villaId == null) {
                throw new InvalidOperationException("Villa is required for Villa Managers and Viewers");
            }
        } else if (current.getRole() == UserRole.VILLA_MANAGER) {
            if (current.getVillaId() == null) {
                throw new InvalidOperationException("Villa Managers must be assigned to a villa before inviting users");
            }
            if (requestedRole != UserRole.VIEWER) {
                throw new InvalidOperationException("Villa Managers can only invite Viewer users");
            }
            villaId = current.getVillaId();
        } else {
            throw new InvalidOperationException("Viewers cannot invite users");
        }

        if (villaId != null && !villaRepository.existsById(villaId)) {
            throw new ResourceNotFoundException("Villa not found");
        }

        String email = request.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmail(email).orElseGet(User::new);
        if (user.getId() != null && Boolean.TRUE.equals(user.getIsActive())) {
            throw new InvalidOperationException("An active account already exists with this email");
        }

        user.setEmail(email);
        user.setFullName(clean(request.getFullName(), email));
        user.setPhoneNumber(request.getPhoneNumber());
        user.setRole(requestedRole);
        user.setVillaId(villaId);
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setIsActive(false);
        user.setInviteToken(UUID.randomUUID().toString());
        user.setInviteExpiresAt(LocalDateTime.now().plusDays(7));
        user.setInviteAcceptedAt(null);
        User saved = userRepository.save(user);

        emailService.sendInviteEmail(saved.getEmail(), saved.getInviteToken(), saved.getRole().name(), current.getFullName());
        return mapToDto(saved);
    }

    public UserDto updateUser(Long userId, InviteUserRequest request) {
        accessControlService.requireGeneralManager();
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (request.getFullName() != null && !request.getFullName().trim().isBlank()) user.setFullName(request.getFullName().trim());
        if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber());
        if (request.getRole() != null) user.setRole(request.getRole());
        if (request.getVillaId() != null) {
            if (!villaRepository.existsById(request.getVillaId())) throw new ResourceNotFoundException("Villa not found");
            user.setVillaId(request.getVillaId());
        }
        return mapToDto(userRepository.save(user));
    }

    public void deleteUser(Long userId) {
        accessControlService.requireGeneralManager();
        User current = accessControlService.currentUser();
        if (current.getId().equals(userId)) throw new InvalidOperationException("You cannot delete your own account");
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        userRepository.delete(user);
    }

    public UserDto mapToDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .villaId(user.getVillaId())
                .isActive(user.getIsActive())
                .invitationStatus(invitationStatus(user))
                .build();
    }

    private String invitationStatus(User user) {
        if (Boolean.TRUE.equals(user.getIsActive())) return "ACTIVE";
        if (user.getInviteExpiresAt() != null && user.getInviteExpiresAt().isBefore(LocalDateTime.now())) return "EXPIRED";
        return "INVITED";
    }

    private String clean(String value, String fallback) {
        return value == null || value.trim().isBlank() ? fallback : value.trim();
    }
}
