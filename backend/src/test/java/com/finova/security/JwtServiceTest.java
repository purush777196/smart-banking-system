package com.finova.security;

import com.finova.entity.User;
import com.finova.entity.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JwtServiceTest {

    private JwtService jwtService;
    private static final String VALID_TEST_SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        jwtService.setSecretString(VALID_TEST_SECRET);
        jwtService.setJwtExpiration(3600000); // 1 hour
    }

    @Test
    @DisplayName("JWT: Should generate valid signed token with user email and role")
    void testJwtGenerationAndClaims() {
        User user = new User("Eleanor Vance", "admin@finova.bank", "pass", UserRole.SUPER_ADMIN);
        user.setId(101L);

        String token = jwtService.generateToken(user);
        assertNotNull(token);
        assertTrue(token.split("\\.").length == 3, "JWT should consist of 3 parts");

        assertTrue(jwtService.validateToken(token));
        assertEquals("admin@finova.bank", jwtService.extractEmail(token));
        assertEquals(UserRole.SUPER_ADMIN, jwtService.extractRole(token));
        assertEquals(101L, jwtService.extractUserId(token));
    }

    @Test
    @DisplayName("JWT: Should validate token against expected email")
    void testJwtValidationWithEmail() {
        User user = new User("Julian Chen", "fraud@finova.bank", "pass", UserRole.FRAUD_ANALYST);
        user.setId(102L);

        String token = jwtService.generateToken(user);

        assertTrue(jwtService.isTokenValid(token, "fraud@finova.bank"));
        assertFalse(jwtService.isTokenValid(token, "attacker@finova.bank"));
    }

    @Test
    @DisplayName("JWT: Expired token should fail validation")
    void testExpiredToken() {
        // Set negative expiration to simulate expired token
        jwtService.setJwtExpiration(-1000);

        User user = new User("Expired User", "expired@finova.bank", "pass", UserRole.CUSTOMER);
        user.setId(103L);

        String token = jwtService.generateToken(user);
        assertFalse(jwtService.validateToken(token), "Expired token should not be valid");
    }

    @Test
    @DisplayName("JWT: Weak secret (<32 chars) should throw IllegalStateException")
    void testWeakSecretRejection() {
        JwtService weakJwtService = new JwtService();
        assertThrows(IllegalStateException.class, () -> {
            weakJwtService.setSecretString("short_weak_secret");
        });
    }
}

