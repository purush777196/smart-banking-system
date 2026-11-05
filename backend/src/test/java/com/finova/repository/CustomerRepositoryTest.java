package com.finova.repository;

import com.finova.entity.Customer;
import com.finova.entity.Gender;
import com.finova.entity.KycStatus;
import com.finova.entity.User;
import com.finova.entity.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    private User primaryUser;

    @BeforeEach
    void setUp() {
        primaryUser = new User("Jane Doe", "jane.doe@example.com", "securePass123", UserRole.CUSTOMER);
        userRepository.save(primaryUser);
    }

    private Customer createStandardCustomer(String customerCode, User user) {
        Customer customer = new Customer();
        customer.setCustomerCode(customerCode);
        customer.setUser(user);
        customer.setDateOfBirth(LocalDate.of(1995, 8, 20));
        customer.setPhoneNumber("+1-555-123-4567");
        customer.setAlternatePhoneNumber("+1-555-987-6543");
        customer.setGender(Gender.FEMALE);
        customer.setAddress("123 Financial Way, Suite 100");
        customer.setCity("Metropolis");
        customer.setState("NY");
        customer.setPostalCode("10001");
        customer.setCountry("USA");
        customer.setOccupation("Financial Analyst");
        customer.setKycStatus(KycStatus.PENDING);
        return customer;
    }

    @Test
    @DisplayName("Customer: Should successfully persist and retrieve customer entity")
    void testPersistAndFindCustomer() {
        Customer customer = createStandardCustomer("CUST-9901", primaryUser);
        Customer saved = customerRepository.save(customer);
        entityManager.flush();
        entityManager.clear();

        assertNotNull(saved.getId(), "Customer ID should be generated");
        Optional<Customer> found = customerRepository.findById(saved.getId());
        assertTrue(found.isPresent(), "Customer should be found by ID");

        Customer retrieved = found.get();
        assertEquals("CUST-9901", retrieved.getCustomerCode());
        assertEquals("+1-555-123-4567", retrieved.getPhoneNumber());
        assertEquals(Gender.FEMALE, retrieved.getGender());
        assertEquals(KycStatus.PENDING, retrieved.getKycStatus());
        assertEquals("10001", retrieved.getPostalCode());
        assertNotNull(retrieved.getCreatedAt(), "createdAt should be populated");
        assertNotNull(retrieved.getUpdatedAt(), "updatedAt should be populated");
    }

    @Test
    @DisplayName("Customer: Should find customer by customer code")
    void testFindByCustomerCode() {
        Customer customer = createStandardCustomer("CUST-CODE-TEST", primaryUser);
        customerRepository.save(customer);
        entityManager.flush();

        Optional<Customer> found = customerRepository.findByCustomerCode("CUST-CODE-TEST");
        assertTrue(found.isPresent());
        assertEquals(primaryUser.getId(), found.get().getUser().getId());
    }

    @Test
    @DisplayName("Customer: Should enforce unique constraint on customerCode")
    void testUniqueCustomerCode() {
        Customer customer1 = createStandardCustomer("CUST-DUP-CODE", primaryUser);
        customerRepository.save(customer1);
        entityManager.flush();

        User secondUser = new User("John Smith", "john.smith@example.com", "pass1234", UserRole.CUSTOMER);
        userRepository.save(secondUser);

        Customer customer2 = createStandardCustomer("CUST-DUP-CODE", secondUser);
        assertThrows(DataIntegrityViolationException.class, () -> {
            customerRepository.saveAndFlush(customer2);
        }, "Saving two customers with identical customerCode should trigger DataIntegrityViolationException");
    }

    @Test
    @DisplayName("Customer: User relationship mapping (findByUserId & findByUserEmail)")
    void testUserRelationship() {
        Customer customer = createStandardCustomer("CUST-REL-01", primaryUser);
        customerRepository.save(customer);
        entityManager.flush();

        Optional<Customer> byUserId = customerRepository.findByUserId(primaryUser.getId());
        assertTrue(byUserId.isPresent(), "Should find customer by user ID");
        assertEquals("CUST-REL-01", byUserId.get().getCustomerCode());

        Optional<Customer> byEmail = customerRepository.findByUserEmail("jane.doe@example.com");
        assertTrue(byEmail.isPresent(), "Should find customer by user email");
        assertEquals("CUST-REL-01", byEmail.get().getCustomerCode());
    }

    @Test
    @DisplayName("Customer: Duplicate profile prevention (One customer per User)")
    void testDuplicateCustomerProfilePrevention() {
        Customer customer1 = createStandardCustomer("CUST-USER-1", primaryUser);
        customerRepository.save(customer1);
        entityManager.flush();

        Customer customer2 = createStandardCustomer("CUST-USER-2", primaryUser);
        assertThrows(DataIntegrityViolationException.class, () -> {
            customerRepository.saveAndFlush(customer2);
        }, "A single User must not have more than one Customer profile");
    }

    @Test
    @DisplayName("Customer: KYC status lifecycle transitions (PENDING, VERIFIED, REJECTED, UNDER_REVIEW)")
    void testKycStatusMapping() {
        Customer customer = createStandardCustomer("CUST-KYC-01", primaryUser);
        customer.setKycStatus(KycStatus.PENDING);
        customerRepository.save(customer);
        entityManager.flush();

        Customer retrieved = customerRepository.findByCustomerCode("CUST-KYC-01").orElseThrow();
        assertEquals(KycStatus.PENDING, retrieved.getKycStatus());

        retrieved.setKycStatus(KycStatus.UNDER_REVIEW);
        customerRepository.save(retrieved);
        entityManager.flush();
        assertEquals(KycStatus.UNDER_REVIEW, customerRepository.findByCustomerCode("CUST-KYC-01").get().getKycStatus());

        retrieved.setKycStatus(KycStatus.VERIFIED);
        customerRepository.save(retrieved);
        entityManager.flush();
        assertEquals(KycStatus.VERIFIED, customerRepository.findByCustomerCode("CUST-KYC-01").get().getKycStatus());

        retrieved.setKycStatus(KycStatus.REJECTED);
        customerRepository.save(retrieved);
        entityManager.flush();
        assertEquals(KycStatus.REJECTED, customerRepository.findByCustomerCode("CUST-KYC-01").get().getKycStatus());
    }

    @Test
    @DisplayName("Customer: Existence verification queries")
    void testExistenceQueries() {
        Customer customer = createStandardCustomer("CUST-EXIST-01", primaryUser);
        customer.setPhoneNumber("+1-555-444-3333");
        customerRepository.save(customer);
        entityManager.flush();

        assertTrue(customerRepository.existsByCustomerCode("CUST-EXIST-01"));
        assertFalse(customerRepository.existsByCustomerCode("CUST-NONEXISTENT"));

        assertTrue(customerRepository.existsByUserId(primaryUser.getId()));
        assertFalse(customerRepository.existsByUserId(999999L));

        assertTrue(customerRepository.existsByPhoneNumber("+1-555-444-3333"));
        assertFalse(customerRepository.existsByPhoneNumber("+1-999-000-0000"));
    }
}
