package com.finova.config;

import com.finova.entity.User;
import com.finova.entity.UserRole;
import com.finova.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Controlled startup database seeder for Finova demo environment.
 * Seeds all 10 persona profiles with BCrypt-hashed passwords if they do not already exist.
 *
 * NOTE: For production deployments, disable this seeder and rotate all initial administrative credentials.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);
    private static final String DEFAULT_DEMO_PASSWORD = "demo1234";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        logger.info("Verifying demo user accounts in PostgreSQL database...");

        String encodedPassword = passwordEncoder.encode(DEFAULT_DEMO_PASSWORD);

        List<SeedUserDto> seedUsers = Arrays.asList(
                new SeedUserDto("Eleanor Vance", "admin@finova.bank", UserRole.SUPER_ADMIN),
                new SeedUserDto("Marcus Sterling", "manager@finova.bank", UserRole.BRANCH_MANAGER),
                new SeedUserDto("Sophia Rodriguez", "customer@finova.bank", UserRole.CUSTOMER),
                new SeedUserDto("Daniel Wright", "employee@finova.bank", UserRole.EMPLOYEE),
                new SeedUserDto("Amina Patel", "loans@finova.bank", UserRole.LOAN_OFFICER),
                new SeedUserDto("Julian Chen", "fraud@finova.bank", UserRole.FRAUD_ANALYST),
                new SeedUserDto("Claire Beaumont", "finance@finova.bank", UserRole.FINANCE_MANAGER),
                new SeedUserDto("David Ross", "audit@finova.bank", UserRole.COMPLIANCE_OFFICER),
                new SeedUserDto("Hannah Abbott", "support@finova.bank", UserRole.SUPPORT_AGENT),
                new SeedUserDto("Devin Sharma", "analytics@finova.bank", UserRole.ANALYTICS_HEAD)
        );

        int seededCount = 0;
        for (SeedUserDto seed : seedUsers) {
            if (!userRepository.existsByEmail(seed.email.toLowerCase())) {
                User user = new User(seed.fullName, seed.email.toLowerCase(), encodedPassword, seed.role);
                userRepository.save(user);
                seededCount++;
                logger.info("Seeded persona: {} ({}) as role {}", seed.fullName, seed.email, seed.role);
            }
        }

        if (seededCount > 0) {
            logger.info("Successfully provisioned {} demo banking accounts with BCrypt hashing.", seededCount);
        } else {
            logger.info("All 10 demo banking personas already exist in database. Skipping duplicate insertion.");
        }
    }

    private static class SeedUserDto {
        final String fullName;
        final String email;
        final UserRole role;

        SeedUserDto(String fullName, String email, UserRole role) {
            this.fullName = fullName;
            this.email = email;
            this.role = role;
        }
    }
}

