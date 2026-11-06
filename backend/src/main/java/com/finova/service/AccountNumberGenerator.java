package com.finova.service;

import com.finova.exception.AccountCreationException;
import com.finova.repository.BankAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Objects;
import java.util.Random;

/**
 * Dedicated generator for cryptographically random, collision-resistant bank account numbers.
 * Generates readable, fixed-width account numbers formatted with a FIN prefix followed by 10 numeric digits.
 * Guarantees zero leakage of customer PII (e.g., date of birth, phone, SSN/IDs).
 */
@Component
public class AccountNumberGenerator {

    public static final String PREFIX = "FIN";
    public static final int NUMERIC_LENGTH = 10;
    public static final int MAX_RETRIES = 5;

    private final BankAccountRepository bankAccountRepository;
    private final Random random;

    @Autowired
    public AccountNumberGenerator(BankAccountRepository bankAccountRepository) {
        this(bankAccountRepository, new SecureRandom());
    }

    public AccountNumberGenerator(BankAccountRepository bankAccountRepository, Random random) {
        this.bankAccountRepository = Objects.requireNonNull(bankAccountRepository, "bankAccountRepository must not be null");
        this.random = Objects.requireNonNull(random, "random must not be null");
    }

    /**
     * Generates a unique bank account number with format FIN followed by 10 numeric digits.
     * Retries up to MAX_RETRIES times if an account number collision occurs.
     *
     * @return unique bank account number (e.g. FIN1029384756)
     * @throws AccountCreationException if unable to generate a unique account number within bounded retries
     */
    public String generateUniqueAccountNumber() {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            StringBuilder sb = new StringBuilder(PREFIX);
            for (int i = 0; i < NUMERIC_LENGTH; i++) {
                sb.append(random.nextInt(10));
            }
            String candidateNumber = sb.toString();

            if (!bankAccountRepository.existsByAccountNumber(candidateNumber)) {
                return candidateNumber;
            }
        }

        throw new AccountCreationException(
                "Failed to generate a unique account number after " + MAX_RETRIES + " attempts"
        );
    }

    /**
     * Alias for generateUniqueAccountNumber() for convenience.
     */
    public String generateAccountNumber() {
        return generateUniqueAccountNumber();
    }
}

