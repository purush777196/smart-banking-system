package com.finova.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finova.dto.AuthResponse;
import com.finova.dto.LoginRequest;
import com.finova.dto.RegisterRequest;
import com.finova.dto.UserResponse;
import com.finova.entity.UserRole;
import com.finova.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        AuthController authController = new AuthController(authService);
        this.mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("AuthController: POST /api/auth/login returns 200 with JWT for valid credentials")
    void testLoginSuccess() throws Exception {
        LoginRequest loginRequest = new LoginRequest("admin@finova.bank", "demo1234");
        UserResponse userResponse = new UserResponse(1L, "Eleanor Vance", "admin@finova.bank", UserRole.SUPER_ADMIN);
        AuthResponse authResponse = new AuthResponse("valid.jwt.token", userResponse);

        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("valid.jwt.token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.email").value("admin@finova.bank"))
                .andExpect(jsonPath("$.user.role").value("SUPER_ADMIN"));
    }

    @Test
    @DisplayName("AuthController: POST /api/auth/login returns 401 on invalid credentials")
    void testLoginFailure() throws Exception {
        LoginRequest loginRequest = new LoginRequest("admin@finova.bank", "wrong_password");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Invalid email or password."));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @DisplayName("AuthController: POST /api/auth/register returns 201 Created for valid customer registration")
    void testRegisterSuccess() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest("New Client", "newclient@finova.bank", "password123");
        UserResponse userResponse = new UserResponse(20L, "New Client", "newclient@finova.bank", UserRole.CUSTOMER);
        AuthResponse authResponse = new AuthResponse("new.client.token", userResponse);

        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("new.client.token"))
                .andExpect(jsonPath("$.user.role").value("CUSTOMER"));
    }

    @Test
    @DisplayName("AuthController: GET /api/auth/me returns 200 with current user when authenticated")
    void testGetCurrentUserAuthenticated() throws Exception {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "customer@finova.bank",
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
        );

        UserResponse userResponse = new UserResponse(3L, "Sophia Rodriguez", "customer@finova.bank", UserRole.CUSTOMER);
        when(authService.getCurrentUser("customer@finova.bank")).thenReturn(userResponse);

        mockMvc.perform(get("/api/auth/me").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("customer@finova.bank"))
                .andExpect(jsonPath("$.fullName").value("Sophia Rodriguez"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    @Test
    @DisplayName("AuthController: GET /api/auth/me returns 401 when principal is null")
    void testGetCurrentUserUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }
}

