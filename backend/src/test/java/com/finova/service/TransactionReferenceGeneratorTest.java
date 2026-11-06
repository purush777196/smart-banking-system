package com.finova.service;

import com.finova.repository.BankTransactionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionReferenceGeneratorTest {

    @Mock
    private BankTransactionRepository transactionRepository;

    @Test
    @DisplayName("1. Successful reference generation: Generates a non-blank reference")
    void testSuccessfulReferenceGeneration() {
        TransactionReferenceGenerator generator = new TransactionReferenceGenerator(transactionRepository);
        when(transactionRepository.existsByTransactionReference(anyString())).thenReturn(false);

        String ref = generator.generateReference();

        assertNotNull(ref);
        assertFalse(ref.isBlank());
        verify(transactionRepository, times(1)).existsByTransactionReference(ref);
    }

    @Test
    @DisplayName("2. Reference format: Starts with FIN-TXN- prefix and has exactly 32 uppercase hex characters")
    void testReferenceFormat() {
        TransactionReferenceGenerator generator = new TransactionReferenceGenerator(transactionRepository);
        when(transactionRepository.existsByTransactionReference(anyString())).thenReturn(false);

        String ref = generator.generateReference();

        assertTrue(ref.startsWith(TransactionReferenceGenerator.PREFIX));
        assertEquals(40, ref.length(), "Reference must be 40 characters (8 prefix + 32 UUID hex chars)");
        String entropy = ref.substring(TransactionReferenceGenerator.PREFIX.length());
        assertEquals(32, entropy.length());
        assertTrue(entropy.matches("^[0-9A-F]{32}$"), "Entropy part must be 32 uppercase hexadecimal characters");
    }

    @Test
    @DisplayName("3. Collision retry: Retries when reference already exists in database")
    void testCollisionRetry() {
        TransactionReferenceGenerator generator = new TransactionReferenceGenerator(transactionRepository);
        when(transactionRepository.existsByTransactionReference(anyString()))
                .thenReturn(true)
                .thenReturn(false);

        String ref = generator.generateReference();

        assertNotNull(ref);
        assertTrue(ref.startsWith(TransactionReferenceGenerator.PREFIX));
        verify(transactionRepository, times(2)).existsByTransactionReference(anyString());
    }

    @Test
    @DisplayName("4. Max retry exhaustion: Throws IllegalStateException when maximum attempts exceeded")
    void testMaxRetryExhaustion() {
        TransactionReferenceGenerator generator = new TransactionReferenceGenerator(transactionRepository);
        when(transactionRepository.existsByTransactionReference(anyString())).thenReturn(true);

        IllegalStateException ex = assertThrows(IllegalStateException.class, generator::generateReference);

        assertTrue(ex.getMessage().contains("Failed to generate a unique transaction reference"));
        verify(transactionRepository, times(TransactionReferenceGenerator.MAX_ATTEMPTS)).existsByTransactionReference(anyString());
    }

    @Test
    @DisplayName("5. Standalone generator: Works without repository dependency")
    void testStandaloneGenerator() {
        TransactionReferenceGenerator generator = new TransactionReferenceGenerator();

        String ref = generator.generateReference();

        assertNotNull(ref);
        assertTrue(ref.startsWith("FIN-TXN-"));
        assertEquals(40, ref.length());
    }

    @Test
    @DisplayName("6. Uniqueness across multiple generations: Distinct random references generated")
    void testUniquenessAcrossGenerations() {
        TransactionReferenceGenerator generator = new TransactionReferenceGenerator();
        Set<String> generatedRefs = new HashSet<>();
        int count = 100;

        for (int i = 0; i < count; i++) {
            String ref = generator.generateReference();
            assertTrue(generatedRefs.add(ref), "Collision detected for reference: " + ref);
        }

        assertEquals(count, generatedRefs.size());
    }
}

