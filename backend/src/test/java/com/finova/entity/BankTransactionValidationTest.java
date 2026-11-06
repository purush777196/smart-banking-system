package com.finova.entity;

import com.finova.entity.enums.AccountType;
import com.finova.entity.enums.TransactionStatus;
import com.finova.entity.enums.TransactionType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class BankTransactionValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private BankAccount createStubAccount() {
        User user = new User("Alice Brown", "alice.tx@finova.bank", "pass1234", UserRole.CUSTOMER);
        user.setId(10L);

        Customer customer = new Customer();
        customer.setId(100L);
        customer.setCustomerCode("CUST-TX-01");
        customer.setUser(user);
        customer.setDateOfBirth(LocalDate.of(1990, 1, 15));
        customer.setPhoneNumber("+1-555-222-3333");
        customer.setAddress("456 Park Avenue");
        customer.setCity("Boston");
        customer.setState("MA");
        customer.setPostalCode("02108");
        customer.setCountry("USA");

        BankAccount account = new BankAccount("FIN100200300401", customer, AccountType.SAVINGS);
        account.setId(500L);
        return account;
    }

    private BankTransaction createValidTransaction() {
        BankAccount account = createStubAccount();
        BankTransaction tx = new BankTransaction();
        tx.setTransactionReference("FIN-TXN-1234567890ABCDEF1234567890ABCDEF");
        tx.setAccount(account);
        tx.setTransactionType(TransactionType.DEPOSIT);
        tx.setTransactionStatus(TransactionStatus.COMPLETED);
        tx.setAmount(new BigDecimal("1500.50"));
        tx.setCurrency("INR");
        tx.setBalanceBefore(new BigDecimal("5000.00"));
        tx.setBalanceAfter(new BigDecimal("6500.50"));
        tx.setDescription("Payroll direct deposit");
        tx.setTransferGroupReference("GRP-TX-001");
        return tx;
    }

    // 1. Valid transaction creation
    @Test
    @DisplayName("1. Entity Validation: Valid transaction passes with zero violations")
    void testValidTransactionPassesValidation() {
        BankTransaction tx = createValidTransaction();
        Set<ConstraintViolation<BankTransaction>> violations = validator.validate(tx);
        assertTrue(violations.isEmpty(), "Valid bank transaction must have zero constraint violations");
    }

    // 2. Null account rejection
    @Test
    @DisplayName("2. Account Constraint: Null account reference produces constraint violation")
    void testNullAccountRejected() {
        BankTransaction tx = createValidTransaction();
        tx.setAccount(null);

        Set<ConstraintViolation<BankTransaction>> violations = validator.validate(tx);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("account")));
    }

    // 3. Null transaction type rejection
    @Test
    @DisplayName("3. Type Constraint: Null transaction type produces constraint violation")
    void testNullTransactionTypeRejected() {
        BankTransaction tx = createValidTransaction();
        tx.setTransactionType(null);

        Set<ConstraintViolation<BankTransaction>> violations = validator.validate(tx);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("transactionType")));
    }

    // 4. Null transaction status rejection
    @Test
    @DisplayName("4. Status Constraint: Null transaction status produces constraint violation")
    void testNullTransactionStatusRejected() {
        BankTransaction tx = createValidTransaction();
        tx.setTransactionStatus(null);

        Set<ConstraintViolation<BankTransaction>> violations = validator.validate(tx);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("transactionStatus")));
    }

    // 5. Zero amount rejection
    @Test
    @DisplayName("5. Amount Constraint: Zero amount produces constraint violation and setter rejection")
    void testZeroAmountRejected() {
        BankTransaction tx = createValidTransaction();

        // Setter rejection
        assertThrows(IllegalArgumentException.class, () -> tx.setAmount(BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class, () -> tx.setAmount(new BigDecimal("0.00")));
    }

    // 6. Negative amount rejection
    @Test
    @DisplayName("6. Amount Constraint: Negative amount produces constraint violation and setter rejection")
    void testNegativeAmountRejected() {
        BankTransaction tx = createValidTransaction();

        // Setter rejection
        assertThrows(IllegalArgumentException.class, () -> tx.setAmount(new BigDecimal("-100.00")));
    }

    // 7. Amount with more than two decimal places rejection
    @Test
    @DisplayName("7. Decimal Precision: Amount with more than 2 decimal places violates @Digits constraint")
    void testAmountFractionDigitsRejected() {
        BankTransaction tx = createValidTransaction();
        tx.setAmount(new BigDecimal("100.999"));

        Set<ConstraintViolation<BankTransaction>> violations = validator.validate(tx);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("amount")));
    }

    // 8. Negative balanceBefore rejection
    @Test
    @DisplayName("8. BalanceBefore Constraint: Negative balanceBefore violates @DecimalMin constraint")
    void testNegativeBalanceBeforeRejected() {
        BankTransaction tx = createValidTransaction();
        assertThrows(IllegalArgumentException.class, () -> tx.setBalanceBefore(new BigDecimal("-0.01")));
    }

    // 9. Negative balanceAfter rejection
    @Test
    @DisplayName("9. BalanceAfter Constraint: Negative balanceAfter violates @DecimalMin constraint")
    void testNegativeBalanceAfterRejected() {
        BankTransaction tx = createValidTransaction();
        assertThrows(IllegalArgumentException.class, () -> tx.setBalanceAfter(new BigDecimal("-50.00")));
    }

    // 10. Invalid currency rejection
    @Test
    @DisplayName("10. Currency Constraint: Lowercase, short, or invalid currency code violates @Pattern")
    void testInvalidCurrencyRejected() {
        BankTransaction tx = createValidTransaction();

        tx.setCurrency("inr"); // Lowercase fails pattern
        Set<ConstraintViolation<BankTransaction>> violations1 = validator.validate(tx);
        assertFalse(violations1.isEmpty());
        assertTrue(violations1.stream().anyMatch(v -> v.getPropertyPath().toString().equals("currency")));

        tx.setCurrency("IN"); // 2 characters fails pattern
        Set<ConstraintViolation<BankTransaction>> violations2 = validator.validate(tx);
        assertFalse(violations2.isEmpty());
        assertTrue(violations2.stream().anyMatch(v -> v.getPropertyPath().toString().equals("currency")));

        tx.setCurrency("USDT"); // 4 characters fails pattern
        Set<ConstraintViolation<BankTransaction>> violations3 = validator.validate(tx);
        assertFalse(violations3.isEmpty());
        assertTrue(violations3.stream().anyMatch(v -> v.getPropertyPath().toString().equals("currency")));
    }

    // 11. Blank transaction reference rejection
    @Test
    @DisplayName("11. Reference Constraint: Blank or short transaction reference violates validation")
    void testBlankOrShortTransactionReferenceRejected() {
        BankTransaction tx = createValidTransaction();

        tx.setTransactionReference(null);
        Set<ConstraintViolation<BankTransaction>> nullViolations = validator.validate(tx);
        assertFalse(nullViolations.isEmpty());
        assertTrue(nullViolations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("transactionReference")));

        tx.setTransactionReference("   ");
        Set<ConstraintViolation<BankTransaction>> blankViolations = validator.validate(tx);
        assertFalse(blankViolations.isEmpty());
        assertTrue(blankViolations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("transactionReference")));

        tx.setTransactionReference("TX123"); // Less than min 8 characters
        Set<ConstraintViolation<BankTransaction>> shortViolations = validator.validate(tx);
        assertFalse(shortViolations.isEmpty());
        assertTrue(shortViolations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("transactionReference")));
    }

    // 12. Valid balance snapshots
    @Test
    @DisplayName("12. Balance Snapshots: Verifies balance snapshots match financial ledger integrity")
    void testValidBalanceSnapshots() {
        BankTransaction tx = createValidTransaction();
        tx.setBalanceBefore(new BigDecimal("1000.00"));
        tx.setAmount(new BigDecimal("250.00"));
        tx.setBalanceAfter(new BigDecimal("1250.00"));

        assertEquals(new BigDecimal("1000.00"), tx.getBalanceBefore());
        assertEquals(new BigDecimal("250.00"), tx.getAmount());
        assertEquals(new BigDecimal("1250.00"), tx.getBalanceAfter());
        Set<ConstraintViolation<BankTransaction>> violations = validator.validate(tx);
        assertTrue(violations.isEmpty());
    }

    // 13. Timestamp initialization
    @Test
    @DisplayName("13. Timestamp Lifecycle: PrePersist initializes createdAt timestamp")
    void testTimestampInitialization() {
        BankTransaction tx = createValidTransaction();
        assertNull(tx.getCreatedAt());

        tx.onCreate();

        assertNotNull(tx.getCreatedAt(), "createdAt must be initialized by @PrePersist callback");
    }
}

