package com.finova.service;

import com.finova.exception.AccountCreationException;
import com.finova.repository.BankAccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountNumberGeneratorTest {

    @Mock
    private BankAccountRepository bankAccountRepository;

    // 1. Successful number generation
    @Test
    @DisplayName("1. Successful number generation: Successfully generates account number")
    void testSuccessfulNumberGeneration() {
        AccountNumberGenerator generator = new AccountNumberGenerator(bankAccountRepository);
        when(bankAccountRepository.existsByAccountNumber(anyString())).thenReturn(false);

        String accountNumber = generator.generateUniqueAccountNumber();

        assertNotNull(accountNumber);
        assertFalse(accountNumber.isBlank());
        verify(bankAccountRepository, times(1)).existsByAccountNumber(accountNumber);
    }

    // 2. Generated number format
    @Test
    @DisplayName("2. Generated number format: Starts with FIN prefix and followed by 10 digits")
    void testGeneratedNumberFormat() {
        AccountNumberGenerator generator = new AccountNumberGenerator(bankAccountRepository);
        when(bankAccountRepository.existsByAccountNumber(anyString())).thenReturn(false);

        String accountNumber = generator.generateUniqueAccountNumber();

        assertEquals(13, accountNumber.length(), "Account number must have total length of 13 characters");
        assertTrue(accountNumber.startsWith("FIN"), "Account number must start with FIN prefix");
        String numericPart = accountNumber.substring(3);
        assertEquals(10, numericPart.length(), "Numeric identifier must be exactly 10 digits");
        assertTrue(numericPart.matches("^\\d{10}$"), "Account number must contain only numeric digits after FIN prefix");
    }

    // 3. Collision retry
    @Test
    @DisplayName("3. Collision retry: Retries generation when a collision occurs")
    void testCollisionRetry() {
        AccountNumberGenerator generator = new AccountNumberGenerator(bankAccountRepository);
        // First candidate collides, second candidate is unique
        when(bankAccountRepository.existsByAccountNumber(anyString()))
                .thenReturn(true)
                .thenReturn(false);

        String accountNumber = generator.generateUniqueAccountNumber();

        assertNotNull(accountNumber);
        assertTrue(accountNumber.startsWith("FIN"));
        verify(bankAccountRepository, times(2)).existsByAccountNumber(anyString());
    }

    // 4. Multiple collisions
    @Test
    @DisplayName("4. Multiple collisions: Handles multiple consecutive collisions successfully")
    void testMultipleCollisions() {
        AccountNumberGenerator generator = new AccountNumberGenerator(bankAccountRepository);
        // Collides 3 times, succeeds on 4th attempt
        when(bankAccountRepository.existsByAccountNumber(anyString()))
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(false);

        String accountNumber = generator.generateUniqueAccountNumber();

        assertNotNull(accountNumber);
        assertTrue(accountNumber.startsWith("FIN"));
        verify(bankAccountRepository, times(4)).existsByAccountNumber(anyString());
    }

    // 5. Maximum retry exhaustion
    @Test
    @DisplayName("5. Maximum retry exhaustion: Throws AccountCreationException when max retries exceeded")
    void testMaxRetryExhaustion() {
        AccountNumberGenerator generator = new AccountNumberGenerator(bankAccountRepository);
        // Continually collides
        when(bankAccountRepository.existsByAccountNumber(anyString())).thenReturn(true);

        AccountCreationException exception = assertThrows(AccountCreationException.class,
                generator::generateUniqueAccountNumber);

        assertTrue(exception.getMessage().contains("Failed to generate a unique account number"));
        verify(bankAccountRepository, times(AccountNumberGenerator.MAX_RETRIES)).existsByAccountNumber(anyString());
    }

    // 6. Repository uniqueness verification
    @Test
    @DisplayName("6. Repository uniqueness verification: Confirms uniqueness against repository for every generated number")
    void testRepositoryUniquenessVerification() {
        // Test with deterministic random source for dependency injection testability
        Random mockRandom = mock(Random.class);
        when(mockRandom.nextInt(10)).thenReturn(1, 2, 3, 4, 5, 6, 7, 8, 9, 0);

        AccountNumberGenerator generator = new AccountNumberGenerator(bankAccountRepository, mockRandom);
        when(bankAccountRepository.existsByAccountNumber("FIN1234567890")).thenReturn(false);

        String accountNumber = generator.generateUniqueAccountNumber();

        assertEquals("FIN1234567890", accountNumber);
        verify(bankAccountRepository, times(1)).existsByAccountNumber("FIN1234567890");
    }
}

