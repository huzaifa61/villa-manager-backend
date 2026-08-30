package com.villamanager.service;

import com.villamanager.dto.AuthResponse;
import com.villamanager.dto.LoginRequest;
import com.villamanager.entity.User;
import com.villamanager.entity.UserRole;
import com.villamanager.exception.AuthenticationFailedException;
import com.villamanager.repository.UserRepository;
import com.villamanager.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthenticationService authenticationService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("person@example.com");
        user.setFullName("Test Person");
        user.setPassword("encoded-password");
        user.setRole(UserRole.GENERAL_MANAGER);
        user.setIsActive(true);
    }

    @Test
    void loginNormalizesEmailBeforeLookup() {
        when(userRepository.findByEmailIgnoreCase("person@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encoded-password")).thenReturn(true);
        when(jwtTokenProvider.generateToken(user)).thenReturn("token");

        AuthResponse response = authenticationService.login(
                new LoginRequest("  Person@Example.COM  ", "password123"));

        assertEquals("token", response.getAccessToken());
        verify(userRepository).findByEmailIgnoreCase("person@example.com");
    }

    @Test
    void loginRejectsMissingCredentialsAsUnauthorized() {
        assertThrows(AuthenticationFailedException.class,
                () -> authenticationService.login(new LoginRequest(" ", null)));
    }
}
