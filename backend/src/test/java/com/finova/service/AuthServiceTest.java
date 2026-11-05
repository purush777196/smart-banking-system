package com.finova.service;

import com.finova.dto.AuthResponse;
import com.finova.dto.LoginRequest;
import com.finova.dto.RegisterRequest;
import com.finova.entity.User;
import com.finova.entity.UserRole;
import com.finova.repository.UserRepository;
import com.finova.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    private PasswordEncoder passwordEncoder;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        authService = new AuthService(userRepository, passwordEncoder, jwtService);
    }

    @Test
    @DisplayName("BCrypt: Password hashing should create valid hashes matching raw password")
    void testBCryptPasswordHashing() {
        String rawPassword = "demo1234";
        String encoded = passwordEncoder.encode(rawPassword);

        assertNotEquals(rawPassword, encoded);
        assertTrue(passwordEncoder.matches(rawPassword, encoded));
        assertFalse(passwordEncoder.matches("wrong_password", encoded));
    }

    @Test
    @DisplayName("AuthService: Successful login should return AuthResponse with JWT and User")
    void testSuccessfulLogin() {
        String email = "admin@finova.bank";
        String rawPassword = "demo1234";
        User user = new User("Eleanor Vance", email, passwordEncoder.encode(rawPassword), UserRole.SUPER_ADMIN);
        user.setId(1L);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("mock.jwt.token");

        AuthResponse response = authService.login(new LoginRequest(email, rawPassword));

        assertNotNull(response);
        assertEquals("mock.jwt.token", response.getToken());
        assertEquals(email, response.getUser().getEmail());
        assertEquals(UserRole.SUPER_ADMIN, response.getUser().getRole());
    }

    @Test
    @DisplayName("AuthService: Invalid password should throw BadCredentialsException")
    void testInvalidPassword() {
        String email = "admin@finova.bank";
        User user = new User("Eleanor Vance", email, passwordEncoder.encode("correct_password"), UserRole.SUPER_ADMIN);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        assertThrows(BadCredentialsException.class, () -> {
            authService.login(new LoginRequest(email, "incorrect_password"));
        });
    }

    @Test
    @DisplayName("AuthService: Unknown email should throw BadCredentialsException")
    void testUnknownEmail() {
        when(userRepository.findByEmail("unknown@finova.bank")).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class, () -> {
            authService.login(new LoginRequest("unknown@finova.bank", "demo1234"));
        });
    }

    @Test
    @DisplayName("AuthService: Duplicate email registration should throw IllegalArgumentException")
    void testDuplicateRegistration() {
        when(userRepository.existsByEmail("existing@finova.bank")).thenReturn(true);

        RegisterRequest request = new RegisterRequest("New Client", "existing@finova.bank", "securePass123");

        assertThrows(IllegalArgumentException.class, () -> {
            authService.register(request);
        });
    }

    @Test
    @DisplayName("AuthService: Public registration must always assign CUSTOMER role")
    void testPublicRegistrationRole() {
        when(userRepository.existsByEmail("newclient@finova.bank")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(99L);
            return u;
        });
        when(jwtService.generateToken(any(User.class))).thenReturn("client.jwt.token");

        RegisterRequest request = new RegisterRequest("New Client", "newclient@finova.bank", "securePass123");
        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals(UserRole.CUSTOMER, response.getUser().getRole(), "Public registration must assign CUSTOMER role");
    }
}

