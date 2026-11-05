package com.finova.security;

import com.finova.entity.User;
import com.finova.entity.UserRole;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtAuthenticationFilterTest {

    private JwtService jwtService;
    private JwtAuthenticationFilter filter;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        jwtService.setSecretString("404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        jwtService.setJwtExpiration(3600000);

        filter = new JwtAuthenticationFilter(jwtService);
        filterChain = mock(FilterChain.class);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Filter: Request with valid JWT header should populate SecurityContextHolder with user and role")
    void testFilterWithValidJwt() throws ServletException, IOException {
        User user = new User("Eleanor Vance", "admin@finova.bank", "hash", UserRole.SUPER_ADMIN);
        user.setId(1L);
        String token = jwtService.generateToken(user);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth, "Authentication should be set in SecurityContext");
        assertEquals("admin@finova.bank", auth.getName());
        assertTrue(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN")),
                "Granted authorities should include ROLE_SUPER_ADMIN");

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Filter: Request without Authorization header should proceed without setting Authentication")
    void testFilterWithoutAuthHeader() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication(), "SecurityContext should remain unauthenticated");
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Filter: Request with invalid or corrupted token should not set Authentication")
    void testFilterWithInvalidJwt() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalid.corrupted.token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication(), "SecurityContext should remain unauthenticated");
        verify(filterChain, times(1)).doFilter(request, response);
    }
}

