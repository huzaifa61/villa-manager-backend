package com.villamanager.config;

import com.villamanager.controller.AuthController;
import com.villamanager.dto.AuthResponse;
import com.villamanager.dto.UserDto;
import com.villamanager.entity.UserRole;
import com.villamanager.security.JwtAuthenticationFilter;
import com.villamanager.security.JwtTokenProvider;
import com.villamanager.service.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtTokenProvider.class})
@TestPropertySource(properties = "server.servlet.context-path=/api")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @org.springframework.boot.test.mock.mockito.MockBean
    private AuthenticationService authenticationService;

    @Test
    void loginIsPublicWithProductionContextPath() throws Exception {
        UserDto user = UserDto.builder()
                .id(1L)
                .email("gm@villa.com")
                .role(UserRole.GENERAL_MANAGER)
                .isActive(true)
                .build();
        AuthResponse authResponse = AuthResponse.builder()
                .accessToken("token")
                .tokenType("Bearer")
                .expiresIn(86400L)
                .user(user)
                .build();
        when(authenticationService.login(any())).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contextPath("/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"gm@villa.com\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void protectedEndpointWithoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/auth/validate").contextPath("/api"))
                .andExpect(status().isUnauthorized());
    }
}
