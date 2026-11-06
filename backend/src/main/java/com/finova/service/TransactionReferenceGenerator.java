package com.finova.service;

import com.finova.repository.BankTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Dedicated generator for cryptographically random, collision-resistant transaction references.
 * Formats references with a readable FIN-TXN- prefix followed by 32 uppercase hexadecimal characters.
 */
@Component
public class TransactionReferenceGenerator {

    public static final String PREFIX = "FIN-TXN-";
    public static final int MAX_ATTEMPTS = 5;

    private final BankTransactionRepository transactionRepository;

    @Autowired
    public TransactionReferenceGenerator(BankTransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public TransactionReferenceGenerator() {
        this.transactionRepository = null;
    }

    /**
     * Generates a unique, non-predictable transaction reference string.
     * Format: FIN-TXN-XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
     *
     * @return unique transaction reference
     */
    public String generateReference() {
        if (transactionRepository != null) {
            for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
                String candidate = createCandidate();
                if (!transactionRepository.existsByTransactionReference(candidate)) {
                    return candidate;
                }
            }
            throw new IllegalStateException("Failed to generate a unique transaction reference after " + MAX_ATTEMPTS + " attempts");
        }
        return createCandidate();
    }

    private String createCandidate() {
        return PREFIX + UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }
}
