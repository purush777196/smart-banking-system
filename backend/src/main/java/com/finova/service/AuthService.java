package com.finova.service;

import com.finova.dto.AuthResponse;
import com.finova.dto.LoginRequest;
import com.finova.dto.RegisterRequest;
import com.finova.dto.UserResponse;
import com.finova.entity.User;
import com.finova.entity.UserRole;
import com.finova.repository.UserRepository;
import com.finova.security.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("An account with email " + email + " already exists.");
        }

        // Public registration assigns CUSTOMER role only
        User user = new User(
                request.getFullName().trim(),
                email,
                passwordEncoder.encode(request.getPassword()),
                UserRole.CUSTOMER
        );

        User savedUser = userRepository.save(user);
        String token = jwtService.generateToken(savedUser);

        return new AuthResponse(token, UserResponse.fromEntity(savedUser));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password.");
        }

        if (user.getEnabled() != null && !user.getEnabled()) {
            throw new DisabledException("User account is disabled. Please contact bank administration.");
        }

        String token = jwtService.generateToken(user);
        return new AuthResponse(token, UserResponse.fromEntity(user));
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email != null ? email.trim().toLowerCase() : "")
                .orElseThrow(() -> new BadCredentialsException("User session not found."));

        return UserResponse.fromEntity(user);
    }
}

