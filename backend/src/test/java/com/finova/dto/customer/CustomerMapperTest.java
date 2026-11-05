package com.finova.dto.customer;

import com.finova.entity.Customer;
import com.finova.entity.Gender;
import com.finova.entity.KycStatus;
import com.finova.entity.User;
import com.finova.entity.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class CustomerMapperTest {

    private CustomerMapper customerMapper;
    private User testUser;
    private Customer testCustomer;

    @BeforeEach
    void setUp() {
        customerMapper = new CustomerMapper();

        testUser = new User("Sophia Rodriguez", "customer@finova.bank", "$2a$10$hashedPasswordHere123456", UserRole.CUSTOMER);
        testUser.setId(5L);

        testCustomer = new Customer();
        testCustomer.setId(100L);
        testCustomer.setCustomerCode("CUST-1001");
        testCustomer.setUser(testUser);
        testCustomer.setDateOfBirth(LocalDate.of(1992, 5, 14));
        testCustomer.setPhoneNumber("+1-212-555-0192");
        testCustomer.setAlternatePhoneNumber("+1-212-555-0199");
        testCustomer.setGender(Gender.FEMALE);
        testCustomer.setAddress("742 Evergreen Terrace, Suite 4B");
        testCustomer.setCity("New York");
        testCustomer.setState("NY");
        testCustomer.setPostalCode("10001");
        testCustomer.setCountry("USA");
        testCustomer.setOccupation("Software Engineering Director");
        testCustomer.setKycStatus(KycStatus.VERIFIED);
        testCustomer.setCreatedAt(LocalDateTime.of(2026, 1, 10, 10, 0));
        testCustomer.setUpdatedAt(LocalDateTime.of(2026, 1, 15, 12, 0));
    }

    @Test
    @DisplayName("Mapper: toResponse correctly maps fields and reads fullName and email from User")
    void testToResponse() {
        CustomerResponse response = customerMapper.toResponse(testCustomer);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals("CUST-1001", response.getCustomerCode());
        assertEquals("Sophia Rodriguez", response.getFullName());
        assertEquals("customer@finova.bank", response.getEmail());
        assertEquals("+1-212-555-0192", response.getPhoneNumber());
        assertEquals(Gender.FEMALE, response.getGender());
        assertEquals("New York", response.getCity());
        assertEquals(KycStatus.VERIFIED, response.getKycStatus());
        assertEquals(LocalDate.of(1992, 5, 14), response.getDateOfBirth());
    }

    @Test
    @DisplayName("Mapper: Password and passwordHash are completely excluded from CustomerResponse")
    void testPasswordExclusionFromResponse() {
        CustomerResponse response = customerMapper.toResponse(testCustomer);

        assertNotNull(response);
        // Verify via reflection that CustomerResponse contains no password or passwordHash fields
        assertFalse(hasField(CustomerResponse.class, "password"), "CustomerResponse must not contain password field");
        assertFalse(hasField(CustomerResponse.class, "passwordHash"), "CustomerResponse must not contain passwordHash field");
    }

    @Test
    @DisplayName("Mapper: toProfileResponse maps authenticated profile data and excludes passwords")
    void testToProfileResponse() {
        CustomerProfileResponse profile = customerMapper.toProfileResponse(testCustomer);

        assertNotNull(profile);
        assertEquals(100L, profile.getCustomerId());
        assertEquals(5L, profile.getUserId());
        assertEquals("CUST-1001", profile.getCustomerCode());
        assertEquals("Sophia Rodriguez", profile.getFullName());
        assertEquals("customer@finova.bank", profile.getEmail());
        assertEquals("Finova Signature Tier", profile.getMembershipTier());
        assertEquals(KycStatus.VERIFIED, profile.getKycStatus());

        assertFalse(hasField(CustomerProfileResponse.class, "password"));
        assertFalse(hasField(CustomerProfileResponse.class, "passwordHash"));
    }

    @Test
    @DisplayName("Mapper: toEntity sets initial KYC status strictly to PENDING and populates fields")
    void testToEntity() {
        CustomerCreateRequest request = new CustomerCreateRequest();
        request.setDateOfBirth(LocalDate.of(1995, 3, 22));
        request.setPhoneNumber("+1-555-999-1111");
        request.setAddress("500 Financial Plaza");
        request.setCity("Chicago");
        request.setState("IL");
        request.setPostalCode("60601");
        request.setCountry("USA");
        request.setOccupation("Data Scientist");

        Customer entity = customerMapper.toEntity(request, testUser, "CUST-9999");

        assertNotNull(entity);
        assertEquals("CUST-9999", entity.getCustomerCode());
        assertEquals(testUser, entity.getUser());
        assertEquals(LocalDate.of(1995, 3, 22), entity.getDateOfBirth());
        assertEquals("+1-555-999-1111", entity.getPhoneNumber());
        assertEquals("Chicago", entity.getCity());
        assertEquals(KycStatus.PENDING, entity.getKycStatus(), "Initial KYC status must be PENDING");
    }

    @Test
    @DisplayName("Mapper: updateEntityFromRequest preserves protected fields (customerCode, user, kycStatus, dateOfBirth)")
    void testProtectedFieldModificationPrevention() {
        CustomerUpdateRequest updateRequest = new CustomerUpdateRequest();
        updateRequest.setPhoneNumber("+1-212-999-8888");
        updateRequest.setAddress("999 New Address Blvd");
        updateRequest.setCity("Brooklyn");
        updateRequest.setPostalCode("11201");
        updateRequest.setOccupation("Senior VP");

        customerMapper.updateEntityFromRequest(testCustomer, updateRequest);

        // Permitted fields should be updated
        assertEquals("+1-212-999-8888", testCustomer.getPhoneNumber());
        assertEquals("999 New Address Blvd", testCustomer.getAddress());
        assertEquals("Brooklyn", testCustomer.getCity());
        assertEquals("11201", testCustomer.getPostalCode());
        assertEquals("Senior VP", testCustomer.getOccupation());

        // Protected fields MUST remain completely untouched
        assertEquals("CUST-1001", testCustomer.getCustomerCode(), "Customer code must not be altered");
        assertEquals(testUser, testCustomer.getUser(), "User reference must not be altered");
        assertEquals(KycStatus.VERIFIED, testCustomer.getKycStatus(), "KYC status must not be altered via update request");
        assertEquals(LocalDate.of(1992, 5, 14), testCustomer.getDateOfBirth(), "Date of birth must not be altered");
        assertEquals(100L, testCustomer.getId(), "Entity ID must not be altered");
    }

    private boolean hasField(Class<?> clazz, String fieldName) {
        try {
            clazz.getDeclaredField(fieldName);
            return true;
        } catch (NoSuchFieldException e) {
            return false;
        }
    }
}

