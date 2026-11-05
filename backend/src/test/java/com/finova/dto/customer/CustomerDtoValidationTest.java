package com.finova.dto.customer;

import com.finova.entity.Gender;
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

public class CustomerDtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private CustomerCreateRequest createValidRequest() {
        CustomerCreateRequest request = new CustomerCreateRequest();
        request.setDateOfBirth(LocalDate.of(1994, 6, 12));
        request.setPhoneNumber("+1-555-333-8888");
        request.setAlternatePhoneNumber("+1-555-333-9999");
        request.setGender(Gender.MALE);
        request.setAddress("100 Wall Street, Floor 14");
        request.setCity("New York");
        request.setState("NY");
        request.setPostalCode("10005");
        request.setCountry("USA");
        request.setOccupation("Portfolio Analyst");
        return request;
    }

    @Test
    @DisplayName("Validation: Valid CustomerCreateRequest should produce no violations")
    void testValidCustomerCreateRequest() {
        CustomerCreateRequest request = createValidRequest();
        Set<ConstraintViolation<CustomerCreateRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Valid request should not yield constraint violations");
    }

    @Test
    @DisplayName("Validation: Missing required fields in CustomerCreateRequest")
    void testMissingRequiredFields() {
        CustomerCreateRequest emptyRequest = new CustomerCreateRequest();
        Set<ConstraintViolation<CustomerCreateRequest>> violations = validator.validate(emptyRequest);

        assertFalse(violations.isEmpty(), "Empty request should produce violations");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("dateOfBirth")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("phoneNumber")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("address")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("city")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("state")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("postalCode")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("country")));
    }

    @Test
    @DisplayName("Validation: Invalid phone number format in CustomerCreateRequest")
    void testInvalidPhoneNumber() {
        CustomerCreateRequest request = createValidRequest();

        // Letters in phone number
        request.setPhoneNumber("not-a-valid-phone");
        Set<ConstraintViolation<CustomerCreateRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Alphabetic phone number should trigger violation");

        // Too short (< 7 characters)
        request.setPhoneNumber("12345");
        violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Too short phone number should trigger violation");

        // Invalid alternate phone
        request.setPhoneNumber("+1-555-333-8888");
        request.setAlternatePhoneNumber("invalid-alt-phone");
        violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Invalid alternate phone should trigger violation");
    }

    @Test
    @DisplayName("Validation: Invalid date of birth (future or null)")
    void testInvalidDateOfBirth() {
        CustomerCreateRequest request = createValidRequest();

        // Future date of birth
        request.setDateOfBirth(LocalDate.now().plusDays(5));
        Set<ConstraintViolation<CustomerCreateRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Future date of birth must trigger @Past violation");

        // Null date of birth
        request.setDateOfBirth(null);
        violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Null date of birth must trigger @NotNull violation");
    }

    @Test
    @DisplayName("Validation: Invalid postal code length")
    void testInvalidPostalCode() {
        CustomerCreateRequest request = createValidRequest();

        // < 3 characters
        request.setPostalCode("12");
        Set<ConstraintViolation<CustomerCreateRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Postal code < 3 chars must fail");

        // > 20 characters
        request.setPostalCode("12345678901234567890123");
        violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Postal code > 20 chars must fail");
    }

    @Test
    @DisplayName("Validation: CustomerUpdateRequest validation rules")
    void testCustomerUpdateRequestValidation() {
        CustomerUpdateRequest updateRequest = new CustomerUpdateRequest();
        updateRequest.setPhoneNumber("+1-555-888-9999");
        updateRequest.setAddress("789 Madison Avenue");
        updateRequest.setPostalCode("10021");

        Set<ConstraintViolation<CustomerUpdateRequest>> violations = validator.validate(updateRequest);
        assertTrue(violations.isEmpty(), "Valid update request should pass validation");

        // Invalid phone number in update request
        updateRequest.setPhoneNumber("bad-phone-number");
        violations = validator.validate(updateRequest);
        assertFalse(violations.isEmpty(), "Invalid phone in update request must fail");
    }
}

