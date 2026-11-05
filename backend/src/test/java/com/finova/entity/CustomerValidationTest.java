package com.finova.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class CustomerValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private Customer createValidCustomer() {
        User user = new User("Alice Brown", "alice.brown@example.com", "pass1234", UserRole.CUSTOMER);
        user.setId(10L);

        Customer customer = new Customer();
        customer.setCustomerCode("CUST-VAL-01");
        customer.setUser(user);
        customer.setDateOfBirth(LocalDate.of(1990, 1, 15));
        customer.setPhoneNumber("+1-555-222-3333");
        customer.setAlternatePhoneNumber("+1-555-444-5555");
        customer.setGender(Gender.FEMALE);
        customer.setAddress("456 Park Avenue");
        customer.setCity("Boston");
        customer.setState("MA");
        customer.setPostalCode("02108");
        customer.setCountry("USA");
        customer.setOccupation("Architect");
        customer.setKycStatus(KycStatus.PENDING);
        return customer;
    }

    @Test
    @DisplayName("Validation: Valid customer should produce zero violations")
    void testValidCustomer() {
        Customer customer = createValidCustomer();
        Set<ConstraintViolation<Customer>> violations = validator.validate(customer);
        assertTrue(violations.isEmpty(), "Valid customer should have no constraint violations");
    }

    @Test
    @DisplayName("Validation: Blank or short customerCode should fail")
    void testCustomerCodeValidation() {
        Customer customer = createValidCustomer();
        customer.setCustomerCode("");
        Set<ConstraintViolation<Customer>> violations = validator.validate(customer);
        assertFalse(violations.isEmpty(), "Blank customerCode should trigger validation violation");

        customer.setCustomerCode("AB"); // below min 3
        violations = validator.validate(customer);
        assertFalse(violations.isEmpty(), "Too short customerCode should trigger validation violation");
    }

    @Test
    @DisplayName("Validation: Null user reference should fail")
    void testNullUserValidation() {
        Customer customer = createValidCustomer();
        customer.setUser(null);
        Set<ConstraintViolation<Customer>> violations = validator.validate(customer);
        assertFalse(violations.isEmpty(), "Null user should trigger validation violation");
    }

    @Test
    @DisplayName("Validation: Future date of birth should fail")
    void testFutureDateOfBirthValidation() {
        Customer customer = createValidCustomer();
        customer.setDateOfBirth(LocalDate.now().plusDays(1));
        Set<ConstraintViolation<Customer>> violations = validator.validate(customer);
        assertFalse(violations.isEmpty(), "Future date of birth must trigger @Past violation");
    }

    @Test
    @DisplayName("Validation: Invalid phone number format should fail")
    void testInvalidPhoneNumberValidation() {
        Customer customer = createValidCustomer();
        customer.setPhoneNumber("invalid-phone-abc");
        Set<ConstraintViolation<Customer>> violations = validator.validate(customer);
        assertFalse(violations.isEmpty(), "Invalid phone number should trigger @Pattern violation");

        customer.setPhoneNumber("123"); // too short (< 7)
        violations = validator.validate(customer);
        assertFalse(violations.isEmpty(), "Too short phone number should trigger @Pattern violation");
    }

    @Test
    @DisplayName("Validation: Required address fields cannot be blank")
    void testRequiredAddressFields() {
        Customer customer = createValidCustomer();

        customer.setAddress("");
        assertFalse(validator.validate(customer).isEmpty(), "Blank address should fail");
        customer.setAddress("Valid Address");

        customer.setCity("   ");
        assertFalse(validator.validate(customer).isEmpty(), "Blank city should fail");
        customer.setCity("Valid City");

        customer.setState(null);
        assertFalse(validator.validate(customer).isEmpty(), "Null state should fail");
        customer.setState("Valid State");

        customer.setCountry("");
        assertFalse(validator.validate(customer).isEmpty(), "Blank country should fail");
        customer.setCountry("Valid Country");

        customer.setPostalCode("12"); // < 3 chars
        assertFalse(validator.validate(customer).isEmpty(), "Postal code < 3 characters should fail");
    }

    @Test
    @DisplayName("Validation: Null kycStatus should fail")
    void testNullKycStatusValidation() {
        Customer customer = createValidCustomer();
        customer.setKycStatus(null);
        Set<ConstraintViolation<Customer>> violations = validator.validate(customer);
        assertFalse(violations.isEmpty(), "Null kycStatus should trigger validation violation");
    }
}

