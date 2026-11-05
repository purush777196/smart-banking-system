package com.finova.service;

import com.finova.dto.customer.CustomerCreateRequest;
import com.finova.dto.customer.CustomerMapper;
import com.finova.dto.customer.CustomerProfileResponse;
import com.finova.dto.customer.CustomerResponse;
import com.finova.dto.customer.CustomerUpdateRequest;
import com.finova.entity.Customer;
import com.finova.entity.Gender;
import com.finova.entity.KycStatus;
import com.finova.entity.User;
import com.finova.entity.UserRole;
import com.finova.exception.CustomerAlreadyExistsException;
import com.finova.exception.CustomerNotFoundException;
import com.finova.exception.UnauthorizedCustomerAccessException;
import com.finova.exception.UserNotFoundException;
import com.finova.repository.CustomerRepository;
import com.finova.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private UserRepository userRepository;

    @Spy
    private CustomerMapper customerMapper = new CustomerMapper();

    @Mock
    private CustomerCodeGenerator customerCodeGenerator;

    @InjectMocks
    private CustomerService customerService;

    private User customerUser;
    private User otherCustomerUser;
    private User adminUser;
    private User managerUser;
    private User employeeUser;
    private User complianceUser;
    private Customer testCustomer;

    @BeforeEach
    void setUp() {
        customerUser = new User("Sophia Rodriguez", "customer@finova.bank", "hash123", UserRole.CUSTOMER);
        customerUser.setId(10L);

        otherCustomerUser = new User("David Miller", "david@example.com", "hash123", UserRole.CUSTOMER);
        otherCustomerUser.setId(11L);

        adminUser = new User("Eleanor Vance", "admin@finova.bank", "hash123", UserRole.SUPER_ADMIN);
        adminUser.setId(1L);

        managerUser = new User("Marcus Sterling", "manager@finova.bank", "hash123", UserRole.BRANCH_MANAGER);
        managerUser.setId(2L);

        employeeUser = new User("Daniel Wright", "employee@finova.bank", "hash123", UserRole.EMPLOYEE);
        employeeUser.setId(3L);

        complianceUser = new User("David Ross", "audit@finova.bank", "hash123", UserRole.COMPLIANCE_OFFICER);
        complianceUser.setId(4L);

        testCustomer = new Customer();
        testCustomer.setId(100L);
        testCustomer.setCustomerCode("FIN-CUS-ABC12345");
        testCustomer.setUser(customerUser);
        testCustomer.setDateOfBirth(LocalDate.of(1992, 5, 14));
        testCustomer.setPhoneNumber("+1-212-555-0192");
        testCustomer.setAddress("742 Evergreen Terrace");
        testCustomer.setCity("New York");
        testCustomer.setState("NY");
        testCustomer.setPostalCode("10001");
        testCustomer.setCountry("USA");
        testCustomer.setKycStatus(KycStatus.PENDING);
    }

    private CustomerCreateRequest createSampleRequest() {
        CustomerCreateRequest request = new CustomerCreateRequest();
        request.setDateOfBirth(LocalDate.of(1992, 5, 14));
        request.setPhoneNumber("+1-212-555-0192");
        request.setAddress("742 Evergreen Terrace");
        request.setCity("New York");
        request.setState("NY");
        request.setPostalCode("10001");
        request.setCountry("USA");
        request.setOccupation("Software Engineer");
        request.setGender(Gender.FEMALE);
        return request;
    }

    // 1. Successful customer creation
    @Test
    @DisplayName("1. createCustomer: Successfully creates and persists customer profile")
    void testCreateCustomer_Success() {
        CustomerCreateRequest request = createSampleRequest();

        when(userRepository.findById(10L)).thenReturn(Optional.of(customerUser));
        when(customerRepository.existsByUserId(10L)).thenReturn(false);
        when(customerCodeGenerator.generateUniqueCode()).thenReturn("FIN-CUS-XYZ98765");
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> {
            Customer c = invocation.getArgument(0);
            c.setId(200L);
            return c;
        });

        CustomerResponse response = customerService.createCustomer(request, 10L);

        assertNotNull(response);
        assertEquals("FIN-CUS-XYZ98765", response.getCustomerCode());
        assertEquals("Sophia Rodriguez", response.getFullName());
        assertEquals("customer@finova.bank", response.getEmail());
        assertEquals(KycStatus.PENDING, response.getKycStatus());
        verify(customerRepository).save(any(Customer.class));
    }

    // 2. User not found on creation
    @Test
    @DisplayName("2. createCustomer: Throws UserNotFoundException when user does not exist")
    void testCreateCustomer_UserNotFound() {
        CustomerCreateRequest request = createSampleRequest();
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> customerService.createCustomer(request, 99L));
        verify(customerRepository, never()).save(any());
    }

    // 3. User role is not CUSTOMER
    @Test
    @DisplayName("3. createCustomer: Throws IllegalArgumentException when user role is not CUSTOMER")
    void testCreateCustomer_NonCustomerRole() {
        CustomerCreateRequest request = createSampleRequest();
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));

        assertThrows(IllegalArgumentException.class, () -> customerService.createCustomer(request, 1L));
        verify(customerRepository, never()).save(any());
    }

    // 4. Duplicate customer profile prevention
    @Test
    @DisplayName("4. createCustomer: Throws CustomerAlreadyExistsException when profile already exists")
    void testCreateCustomer_DuplicateProfile() {
        CustomerCreateRequest request = createSampleRequest();
        when(userRepository.findById(10L)).thenReturn(Optional.of(customerUser));
        when(customerRepository.existsByUserId(10L)).thenReturn(true);

        assertThrows(CustomerAlreadyExistsException.class, () -> customerService.createCustomer(request, 10L));
        verify(customerRepository, never()).save(any());
    }

    // 5. Successful customer retrieval by ID
    @Test
    @DisplayName("5. getCustomerById: Profile owner can retrieve own profile")
    void testGetCustomerById_Success_Owner() {
        when(customerRepository.findById(100L)).thenReturn(Optional.of(testCustomer));
        when(userRepository.findById(10L)).thenReturn(Optional.of(customerUser));

        CustomerResponse response = customerService.getCustomerById(100L, 10L);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals("FIN-CUS-ABC12345", response.getCustomerCode());
    }

    // 6. Customer not found by ID
    @Test
    @DisplayName("6. getCustomerById: Throws CustomerNotFoundException when ID is invalid")
    void testGetCustomerById_NotFound() {
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () -> customerService.getCustomerById(999L, 10L));
    }

    // 7. Customer cannot access another customer's profile
    @Test
    @DisplayName("7. getCustomerById: Customer cannot access another customer's profile")
    void testGetCustomerById_Forbidden() {
        when(customerRepository.findById(100L)).thenReturn(Optional.of(testCustomer));
        when(userRepository.findById(11L)).thenReturn(Optional.of(otherCustomerUser));

        assertThrows(UnauthorizedCustomerAccessException.class, () -> customerService.getCustomerById(100L, 11L));
    }

    // 8. Authorized staff retrieval
    @Test
    @DisplayName("8. getCustomerById: Authorized banking staff (Admin, Manager, Employee, Compliance) can retrieve profile")
    void testGetCustomerById_AuthorizedStaff() {
        when(customerRepository.findById(100L)).thenReturn(Optional.of(testCustomer));

        // Super Admin
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        assertNotNull(customerService.getCustomerById(100L, 1L));

        // Branch Manager
        when(userRepository.findById(2L)).thenReturn(Optional.of(managerUser));
        assertNotNull(customerService.getCustomerById(100L, 2L));

        // Employee
        when(userRepository.findById(3L)).thenReturn(Optional.of(employeeUser));
        assertNotNull(customerService.getCustomerById(100L, 3L));

        // Compliance Officer
        when(userRepository.findById(4L)).thenReturn(Optional.of(complianceUser));
        assertNotNull(customerService.getCustomerById(100L, 4L));
    }

    // 9. Successful own-profile retrieval (getMyProfile)
    @Test
    @DisplayName("9. getMyProfile: Authenticated customer retrieves own profile")
    void testGetMyProfile_Success() {
        when(userRepository.existsById(10L)).thenReturn(true);
        when(customerRepository.findByUserId(10L)).thenReturn(Optional.of(testCustomer));

        CustomerProfileResponse profile = customerService.getMyProfile(10L);

        assertNotNull(profile);
        assertEquals(100L, profile.getCustomerId());
        assertEquals(10L, profile.getUserId());
        assertEquals("FIN-CUS-ABC12345", profile.getCustomerCode());
        assertEquals("Sophia Rodriguez", profile.getFullName());
    }

    // 10. Profile not found for authenticated user
    @Test
    @DisplayName("10. getMyProfile: Throws CustomerNotFoundException when no customer record exists")
    void testGetMyProfile_NotFound() {
        when(userRepository.existsById(10L)).thenReturn(true);
        when(customerRepository.findByUserId(10L)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () -> customerService.getMyProfile(10L));
    }

    // 11. Successful customer update
    @Test
    @DisplayName("11. updateCustomer: Updates permitted fields successfully")
    void testUpdateCustomer_Success() {
        CustomerUpdateRequest updateRequest = new CustomerUpdateRequest();
        updateRequest.setPhoneNumber("+1-212-555-9999");
        updateRequest.setAddress("800 Fifth Avenue");
        updateRequest.setCity("Manhattan");

        when(customerRepository.findById(100L)).thenReturn(Optional.of(testCustomer));
        when(userRepository.findById(10L)).thenReturn(Optional.of(customerUser));
        when(customerRepository.save(any(Customer.class))).thenAnswer(i -> i.getArgument(0));

        CustomerResponse response = customerService.updateCustomer(100L, updateRequest, 10L);

        assertNotNull(response);
        assertEquals("+1-212-555-9999", response.getPhoneNumber());
        assertEquals("800 Fifth Avenue", response.getAddress());
        assertEquals("Manhattan", response.getCity());
    }

    // 12. Unauthorized customer update
    @Test
    @DisplayName("12. updateCustomer: Throws UnauthorizedCustomerAccessException when another customer attempts update")
    void testUpdateCustomer_Forbidden() {
        CustomerUpdateRequest updateRequest = new CustomerUpdateRequest();
        updateRequest.setCity("Unauthorized City");

        when(customerRepository.findById(100L)).thenReturn(Optional.of(testCustomer));
        when(userRepository.findById(11L)).thenReturn(Optional.of(otherCustomerUser));

        assertThrows(UnauthorizedCustomerAccessException.class, () -> customerService.updateCustomer(100L, updateRequest, 11L));
        verify(customerRepository, never()).save(any());
    }

    // 13. Protected KYC status remains unchanged
    @Test
    @DisplayName("13. updateCustomer: KYC status and customer code remain strictly unchanged during update")
    void testUpdateCustomer_ProtectedFieldsUnchanged() {
        testCustomer.setKycStatus(KycStatus.VERIFIED);
        CustomerUpdateRequest updateRequest = new CustomerUpdateRequest();
        updateRequest.setOccupation("Principal Architect");

        when(customerRepository.findById(100L)).thenReturn(Optional.of(testCustomer));
        when(userRepository.findById(10L)).thenReturn(Optional.of(customerUser));
        when(customerRepository.save(any(Customer.class))).thenAnswer(i -> i.getArgument(0));

        CustomerResponse response = customerService.updateCustomer(100L, updateRequest, 10L);

        assertEquals(KycStatus.VERIFIED, response.getKycStatus(), "KYC status must remain VERIFIED");
        assertEquals("FIN-CUS-ABC12345", response.getCustomerCode(), "Customer code must remain unchanged");
    }

    // 14. Retrieval by Customer Code
    @Test
    @DisplayName("14. getCustomerByCode: Retrieves customer by unique code for owner and staff")
    void testGetCustomerByCode_Success() {
        when(customerRepository.findByCustomerCode("FIN-CUS-ABC12345")).thenReturn(Optional.of(testCustomer));
        when(userRepository.findById(10L)).thenReturn(Optional.of(customerUser));

        CustomerResponse response = customerService.getCustomerByCode("FIN-CUS-ABC12345", 10L);
        assertNotNull(response);
        assertEquals("FIN-CUS-ABC12345", response.getCustomerCode());

        // Forbidden check
        when(userRepository.findById(11L)).thenReturn(Optional.of(otherCustomerUser));
        assertThrows(UnauthorizedCustomerAccessException.class,
                () -> customerService.getCustomerByCode("FIN-CUS-ABC12345", 11L));
    }

    // 15. Listing Customers - Authorized Staff
    @Test
    @DisplayName("15. getAllCustomers: Authorized staff can retrieve paginated customers")
    void testGetAllCustomers_AuthorizedStaff_Success() {
        Page<Customer> page = new PageImpl<>(List.of(testCustomer), PageRequest.of(0, 10), 1);
        when(userRepository.findById(12L)).thenReturn(Optional.of(adminUser));
        when(customerRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        Page<CustomerResponse> result = customerService.getAllCustomers(null, null, null, PageRequest.of(0, 10), 12L);
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("FIN-CUS-ABC12345", result.getContent().get(0).getCustomerCode());
    }

    // 16. Listing Customers - Forbidden for Customer
    @Test
    @DisplayName("16. getAllCustomers: Customer is forbidden from listing customers")
    void testGetAllCustomers_Customer_ThrowsForbidden() {
        when(userRepository.findById(10L)).thenReturn(Optional.of(customerUser));

        assertThrows(UnauthorizedCustomerAccessException.class,
                () -> customerService.getAllCustomers(null, null, null, PageRequest.of(0, 10), 10L));
    }
}

