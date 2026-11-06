package com.finova.dto.account;

import com.finova.entity.enums.AccountType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class AccountDtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Validation: Valid AccountCreateRequest passes validation")
    void testValidRequest() {
        AccountCreateRequest request = new AccountCreateRequest(AccountType.SAVINGS, "Main Branch", "BR-01", "FINO0001234");
        Set<ConstraintViolation<AccountCreateRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Validation: Null accountType fails validation")
    void testNullAccountType() {
        AccountCreateRequest request = new AccountCreateRequest();
        request.setAccountType(null);
        Set<ConstraintViolation<AccountCreateRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("accountType")));
    }

    @Test
    @DisplayName("Validation: Exceeding branchName max size fails validation")
    void testBranchNameTooLong() {
        AccountCreateRequest request = new AccountCreateRequest(AccountType.SAVINGS);
        request.setBranchName("A".repeat(101));
        Set<ConstraintViolation<AccountCreateRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("branchName")));
    }
}

