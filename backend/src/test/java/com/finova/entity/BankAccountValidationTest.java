package com.finova.entity;

import com.finova.entity.enums.AccountStatus;
import com.finova.entity.enums.AccountType;
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

public class BankAccountValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private Customer createStubCustomer() {
        User user = new User("Alice Brown", "alice.brown@example.com", "pass1234", UserRole.CUSTOMER);
        user.setId(10L);

        Customer customer = new Customer();
        customer.setId(100L);
        customer.setCustomerCode("CUST-VAL-01");
        customer.setUser(user);
        customer.setDateOfBirth(LocalDate.of(1990, 1, 15));
        customer.setPhoneNumber("+1-555-222-3333");
        customer.setAddress("456 Park Avenue");
        customer.setCity("Boston");
        customer.setState("MA");
        customer.setPostalCode("02108");
        customer.setCountry("USA");
        customer.setKycStatus(KycStatus.PENDING);
        return customer;
    }

    private BankAccount createValidBankAccount() {
        Customer customer = createStubCustomer();
        BankAccount account = new BankAccount("100200300401", customer, AccountType.SAVINGS);
        account.setBranchName("Downtown Boston Branch");
        account.setBranchCode("BOS-001");
        account.setIfscCode("FINO0001234");
        return account;
    }

    // 1. BankAccount entity creation
    @Test
    @DisplayName("1. Entity Creation: Valid bank account passes validation with zero violations")
    void testValidBankAccountCreation() {
        BankAccount account = createValidBankAccount();
        Set<ConstraintViolation<BankAccount>> violations = validator.validate(account);
        assertTrue(violations.isEmpty(), "Valid bank account must have zero constraint violations");
    }

    // 2. Default ACTIVE status
    @Test
    @DisplayName("2. Default Status: New account initializes with ACTIVE status")
    void testDefaultAccountStatus() {
        BankAccount account = new BankAccount();
        assertEquals(AccountStatus.ACTIVE, account.getAccountStatus(), "Default account status must be ACTIVE");
    }

    // 3. Default zero balance
    @Test
    @DisplayName("3. Default Balance: New account initializes with 0.00 zero balance")
    void testDefaultBalance() {
        BankAccount account = new BankAccount();
        assertNotNull(account.getBalance(), "Balance must not be null");
        assertEquals(new BigDecimal("0.00"), account.getBalance(), "Default balance must be 0.00");
    }

    // 4. Default INR currency
    @Test
    @DisplayName("4. Default Currency: New account initializes with INR currency")
    void testDefaultCurrency() {
        BankAccount account = new BankAccount();
        assertEquals("INR", account.getCurrency(), "Default currency must be INR");
    }

    // 5. SAVINGS account type
    @Test
    @DisplayName("5. AccountType: SAVINGS type is supported and validates successfully")
    void testSavingsAccountType() {
        BankAccount account = createValidBankAccount();
        account.setAccountType(AccountType.SAVINGS);
        Set<ConstraintViolation<BankAccount>> violations = validator.validate(account);
        assertTrue(violations.isEmpty());
        assertEquals(AccountType.SAVINGS, account.getAccountType());
    }

    // 6. CURRENT account type
    @Test
    @DisplayName("6. AccountType: CURRENT type is supported and validates successfully")
    void testCurrentAccountType() {
        BankAccount account = createValidBankAccount();
        account.setAccountType(AccountType.CURRENT);
        Set<ConstraintViolation<BankAccount>> violations = validator.validate(account);
        assertTrue(violations.isEmpty());
        assertEquals(AccountType.CURRENT, account.getAccountType());
    }

    // 7. FIXED_DEPOSIT account type
    @Test
    @DisplayName("7. AccountType: FIXED_DEPOSIT type is supported and validates successfully")
    void testFixedDepositAccountType() {
        BankAccount account = createValidBankAccount();
        account.setAccountType(AccountType.FIXED_DEPOSIT);
        Set<ConstraintViolation<BankAccount>> violations = validator.validate(account);
        assertTrue(violations.isEmpty());
        assertEquals(AccountType.FIXED_DEPOSIT, account.getAccountType());
    }

    // 8. AccountStatus enum values
    @Test
    @DisplayName("8. AccountStatus: Verifies all enum values (ACTIVE, FROZEN, CLOSED)")
    void testAccountStatusEnumValues() {
        assertEquals(3, AccountStatus.values().length);
        assertNotNull(AccountStatus.valueOf("ACTIVE"));
        assertNotNull(AccountStatus.valueOf("FROZEN"));
        assertNotNull(AccountStatus.valueOf("CLOSED"));

        BankAccount account = createValidBankAccount();
        account.setAccountStatus(AccountStatus.FROZEN);
        assertEquals(AccountStatus.FROZEN, account.getAccountStatus());
        account.setAccountStatus(AccountStatus.CLOSED);
        assertEquals(AccountStatus.CLOSED, account.getAccountStatus());
    }

    // 12. Required field constraints
    @Test
    @DisplayName("12. Required Fields: Null or blank required fields produce constraint violations")
    void testRequiredFieldConstraints() {
        BankAccount account = new BankAccount();

        // Null accountNumber
        account.setAccountNumber(null);
        account.setCustomer(null);
        account.setAccountType(null);
        account.setAccountStatus(null);
        account.setBalance(null);
        account.setCurrency(null);

        Set<ConstraintViolation<BankAccount>> violations = validator.validate(account);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("accountNumber")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("customer")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("accountType")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("accountStatus")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("balance")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("currency")));
    }

    @Test
    @DisplayName("12b. Account Number: Too short account number fails validation")
    void testAccountNumberTooShort() {
        BankAccount account = createValidBankAccount();
        account.setAccountNumber("12345"); // Min is 8
        Set<ConstraintViolation<BankAccount>> violations = validator.validate(account);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("accountNumber")));
    }

    // 13. Monetary precision and scale
    @Test
    @DisplayName("13. Monetary Precision: Validates precision 19 and scale 2")
    void testMonetaryPrecisionAndScale() {
        BankAccount account = createValidBankAccount();
        account.setBalance(new BigDecimal("123456789012345.67")); // 15 integer, 2 decimals
        Set<ConstraintViolation<BankAccount>> violations = validator.validate(account);
        assertTrue(violations.isEmpty());

        // More than 2 decimal places fails validation
        account.setBalance(new BigDecimal("100.999"));
        Set<ConstraintViolation<BankAccount>> decimalViolations = validator.validate(account);
        assertFalse(decimalViolations.isEmpty());
        assertTrue(decimalViolations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("balance")));
    }

    // 14. Negative initial balance validation
    @Test
    @DisplayName("14. Negative Balance: Negative balance violates @DecimalMin constraint")
    void testNegativeBalanceValidation() {
        BankAccount account = createValidBankAccount();
        // Bean validation check
        account.setAccountNumber("100200300401");
        assertThrows(IllegalArgumentException.class, () -> account.setBalance(new BigDecimal("-0.01")));

        // Constructor rejection check
        Customer customer = createStubCustomer();
        assertThrows(IllegalArgumentException.class, () -> new BankAccount("100200300401", customer, AccountType.SAVINGS, new BigDecimal("-100.00"), "INR"));
    }
}

