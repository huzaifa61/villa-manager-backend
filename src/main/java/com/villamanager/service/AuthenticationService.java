package com.villamanager.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.villamanager.dto.AcceptInviteRequest;
import com.villamanager.dto.AuthResponse;
import com.villamanager.dto.LoginRequest;
import com.villamanager.dto.RegisterRequest;
import com.villamanager.dto.UserDto;
import com.villamanager.entity.User;
import com.villamanager.entity.UserRole;
import com.villamanager.exception.AuthenticationFailedException;
import com.villamanager.exception.InvalidOperationException;
import com.villamanager.repository.UserRepository;
import com.villamanager.security.JwtTokenProvider;

import java.time.LocalDateTime;

@Service
public class AuthenticationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthenticationFailedException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AuthenticationFailedException("Invalid email or password");
        }
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new AuthenticationFailedException("This account has not accepted its invitation yet");
        }

        // ── Subscription checks ──────────────────────────────────────────────

        if (user.getRole() == UserRole.VILLA_MANAGER) {
            // Direct subscription check for Villa Manager
            if (user.getSubscriptionExpiresAt() != null
                    && user.getSubscriptionExpiresAt().isBefore(LocalDateTime.now())) {
                throw new AuthenticationFailedException(
                        "Your subscription has expired. Please contact the General Manager to renew access.");
            }
        }

        if (user.getRole() == UserRole.VIEWER && user.getVillaId() != null) {
            // Find the Villa Manager for this viewer's villa
            userRepository.findAll().stream()
                    .filter(u -> u.getRole() == UserRole.VILLA_MANAGER
                            && user.getVillaId().equals(u.getVillaId())
                            && u.getSubscriptionExpiresAt() != null
                            && u.getSubscriptionExpiresAt().isBefore(LocalDateTime.now()))
                    .findFirst()
                    .ifPresent(expiredManager -> {
                        throw new AuthenticationFailedException(
                                "Access suspended. The Villa Manager's subscription has expired. Please contact your administrator.");
                    });
        }

        String token = jwtTokenProvider.generateToken(user);

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(86400L)
                .user(mapToUserDto(user))
                .build();
    }

    public AuthResponse register(RegisterRequest request) {
        if (request.getFullName() == null || request.getFullName().trim().isEmpty()) {
            throw new InvalidOperationException("Full name is required");
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new InvalidOperationException("Email is required");
        }
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new InvalidOperationException("Password must be at least 6 characters");
        }

        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new InvalidOperationException("An account already exists with this email");
        }

        User user = new User();
        user.setEmail(email);
        user.setFullName(request.getFullName().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhoneNumber(request.getPhoneNumber());
        user.setRole(UserRole.VIEWER);
        user.setVillaId(1L);
        user.setIsActive(true);
        User saved = userRepository.save(user);
        String token = jwtTokenProvider.generateToken(saved);

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(86400L)
                .user(mapToUserDto(saved))
                .build();
    }

    public AuthResponse acceptInvite(AcceptInviteRequest request) {
        if (request.getToken() == null || request.getToken().trim().isBlank()) {
            throw new InvalidOperationException("Invite token is required");
        }
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new InvalidOperationException("Password must be at least 6 characters");
        }

        User user = userRepository.findByInviteToken(request.getToken().trim())
                .orElseThrow(() -> new InvalidOperationException("Invalid invitation token"));

        if (user.getInviteExpiresAt() != null && user.getInviteExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidOperationException("This invitation has expired");
        }

        if (request.getFullName() != null && !request.getFullName().trim().isBlank()) {
            user.setFullName(request.getFullName().trim());
        }
        user.setPhoneNumber(request.getPhoneNumber());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setIsActive(true);
        user.setInviteAcceptedAt(LocalDateTime.now());
        user.setInviteToken(null);
        user.setInviteExpiresAt(null);
        User saved = userRepository.save(user);

        String token = jwtTokenProvider.generateToken(saved);
        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(86400L)
                .user(mapToUserDto(saved))
                .build();
    }

    private UserDto mapToUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .villaId(user.getVillaId())
                .isActive(user.getIsActive())
                .invitationStatus(Boolean.TRUE.equals(user.getIsActive()) ? "ACTIVE" : "INVITED")
                .build();
    }
}
