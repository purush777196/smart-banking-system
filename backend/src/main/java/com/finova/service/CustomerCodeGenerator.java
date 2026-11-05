package com.finova.service;

import com.finova.repository.CustomerRepository;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.UUID;

/**
 * Dedicated generator for cryptographically random, collision-resistant customer codes.
 * Ensures internal database primary keys are never exposed in public customer references.
 */
@Component
public class CustomerCodeGenerator {

    private static final String PREFIX = "FIN-CUS-";
    private static final String ALPHABET = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ"; // 32 chars, excludes 0, 1, O, I to avoid visual ambiguity
    private static final int CODE_LENGTH = 8;
    private static final int MAX_ATTEMPTS = 5;

    private final CustomerRepository customerRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public CustomerCodeGenerator(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    /**
     * Generates a unique customer code with the format FIN-CUS-XXXXXXXX.
     * Verifies against the repository and safely retries if a collision occurs.
     */
    public String generateUniqueCode() {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            StringBuilder sb = new StringBuilder(PREFIX);
            for (int i = 0; i < CODE_LENGTH; i++) {
                int randomIndex = secureRandom.nextInt(ALPHABET.length());
                sb.append(ALPHABET.charAt(randomIndex));
            }
            String candidateCode = sb.toString();

            if (!customerRepository.existsByCustomerCode(candidateCode)) {
                return candidateCode;
            }
        }

        // Secondary fallback with UUID entropy if random selection experienced multiple collisions
        String fallbackCode = PREFIX + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        if (!customerRepository.existsByCustomerCode(fallbackCode)) {
            return fallbackCode;
        }

        throw new IllegalStateException("Failed to generate a unique customer code after maximum collision resolution attempts");
    }
}

