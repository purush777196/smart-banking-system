package com.finova.customer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finova.controller.GlobalExceptionHandler;
import com.finova.dto.customer.*;
import com.finova.entity.Gender;
import com.finova.entity.KycStatus;
import com.finova.entity.User;
import com.finova.entity.UserRole;
import com.finova.exception.CustomerAlreadyExistsException;
import com.finova.exception.CustomerNotFoundException;
import com.finova.exception.UnauthorizedCustomerAccessException;
import com.finova.repository.UserRepository;
import com.finova.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class CustomerControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CustomerService customerService;

    @Mock
    private UserRepository userRepository;

    private ObjectMapper objectMapper;

    private User customerUser;
    private User staffUser;
    private CustomerResponse sampleCustomerResponse;
    private CustomerProfileResponse sampleProfileResponse;

    @BeforeEach
    void setUp() {
        CustomerController customerController = new CustomerController(customerService, userRepository);
        this.mockMvc = MockMvcBuilders.standaloneSetup(customerController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        this.objectMapper = new ObjectMapper();
        this.objectMapper.findAndRegisterModules();

        customerUser = new User("Alice Johnson", "customer@finova.bank", "hash", UserRole.CUSTOMER);
        customerUser.setId(1L);

        staffUser = new User("Robert Branch", "manager@finova.bank", "hash", UserRole.BRANCH_MANAGER);
        staffUser.setId(5L);

        sampleCustomerResponse = new CustomerResponse();
        sampleCustomerResponse.setId(10L);
        sampleCustomerResponse.setCustomerCode("FIN-CUS-1001");
        sampleCustomerResponse.setFullName("Alice Johnson");
        sampleCustomerResponse.setEmail("customer@finova.bank");
        sampleCustomerResponse.setPhoneNumber("+1234567890");
        sampleCustomerResponse.setDateOfBirth(LocalDate.of(1990, 5, 15));
        sampleCustomerResponse.setGender(Gender.FEMALE);
        sampleCustomerResponse.setAddress("123 Main St");
        sampleCustomerResponse.setCity("Metropolis");
        sampleCustomerResponse.setState("NY");
        sampleCustomerResponse.setPostalCode("10001");
        sampleCustomerResponse.setCountry("USA");
        sampleCustomerResponse.setKycStatus(KycStatus.PENDING);
        sampleCustomerResponse.setCreatedAt(LocalDateTime.now());
        sampleCustomerResponse.setUpdatedAt(LocalDateTime.now());

        sampleProfileResponse = new CustomerProfileResponse();
        sampleProfileResponse.setCustomerId(10L);
        sampleProfileResponse.setUserId(1L);
        sampleProfileResponse.setCustomerCode("FIN-CUS-1001");
        sampleProfileResponse.setFullName("Alice Johnson");
        sampleProfileResponse.setEmail("customer@finova.bank");
        sampleProfileResponse.setPhoneNumber("+1234567890");
        sampleProfileResponse.setDateOfBirth(LocalDate.of(1990, 5, 15));
        sampleProfileResponse.setGender(Gender.FEMALE);
        sampleProfileResponse.setAddress("123 Main St");
        sampleProfileResponse.setCity("Metropolis");
        sampleProfileResponse.setState("NY");
        sampleProfileResponse.setPostalCode("10001");
        sampleProfileResponse.setCountry("USA");
        sampleProfileResponse.setKycStatus(KycStatus.PENDING);
        sampleProfileResponse.setMembershipTier("Finova Signature Tier");
    }

    private Authentication createAuth(String email, String role) {
        return new UsernamePasswordAuthenticationToken(
                email,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
        );
    }

    // 1. Successful customer creation
    @Test
    @DisplayName("1. POST /api/customers - Successfully creates customer profile (201 Created)")
    void testCreateCustomerSuccess() throws Exception {
        String requestJson = """
                {
                  "dateOfBirth": "1990-05-15",
                  "phoneNumber": "+1234567890",
                  "address": "123 Main St",
                  "city": "Metropolis",
                  "state": "NY",
                  "postalCode": "10001",
                  "country": "USA"
                }
                """;

        when(userRepository.findByEmail("customer@finova.bank")).thenReturn(Optional.of(customerUser));
        when(customerService.createCustomer(any(CustomerCreateRequest.class), eq(1L))).thenReturn(sampleCustomerResponse);

        mockMvc.perform(post("/api/customers")
                        .principal(createAuth("customer@finova.bank", "CUSTOMER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.customerCode").value("FIN-CUS-1001"))
                .andExpect(jsonPath("$.email").value("customer@finova.bank"))
                .andExpect(jsonPath("$.kycStatus").value("PENDING"));
    }

    // 2. Successful own-profile retrieval
    @Test
    @DisplayName("2. GET /api/customers/me - Successfully retrieves authenticated user's own profile (200 OK)")
    void testGetMyProfileSuccess() throws Exception {
        when(userRepository.findByEmail("customer@finova.bank")).thenReturn(Optional.of(customerUser));
        when(customerService.getMyProfile(1L)).thenReturn(sampleProfileResponse);

        mockMvc.perform(get("/api/customers/me")
                        .principal(createAuth("customer@finova.bank", "CUSTOMER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(10))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.customerCode").value("FIN-CUS-1001"))
                .andExpect(jsonPath("$.membershipTier").value("Finova Signature Tier"));
    }

    // 3. Customer cannot access another customer's profile
    @Test
    @DisplayName("3. GET /api/customers/{id} - Customer cannot access another customer's profile (403 Forbidden)")
    void testCustomerCannotAccessAnotherCustomerProfile() throws Exception {
        when(userRepository.findByEmail("customer@finova.bank")).thenReturn(Optional.of(customerUser));
        when(customerService.getCustomerById(eq(99L), eq(1L)))
                .thenThrow(new UnauthorizedCustomerAccessException("Access denied: You are not authorized to access this customer profile"));

        mockMvc.perform(get("/api/customers/99")
                        .principal(createAuth("customer@finova.bank", "CUSTOMER")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Access denied: You are not authorized to access this customer profile"));
    }

    // 4. Authorized staff can access permitted customer profiles
    @Test
    @DisplayName("4. GET /api/customers/{id} - Authorized staff can access permitted customer profiles (200 OK)")
    void testAuthorizedStaffCanAccessCustomerProfile() throws Exception {
        when(userRepository.findByEmail("manager@finova.bank")).thenReturn(Optional.of(staffUser));
        when(customerService.getCustomerById(eq(10L), eq(5L))).thenReturn(sampleCustomerResponse);

        mockMvc.perform(get("/api/customers/10")
                        .principal(createAuth("manager@finova.bank", "BRANCH_MANAGER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.customerCode").value("FIN-CUS-1001"));
    }

    // 5. Successful profile update
    @Test
    @DisplayName("5. PUT /api/customers/{id} - Successfully updates allowed profile fields (200 OK)")
    void testUpdateCustomerSuccess() throws Exception {
        CustomerUpdateRequest updateRequest = new CustomerUpdateRequest();
        updateRequest.setPhoneNumber("+1987654321");
        updateRequest.setCity("Gotham");

        CustomerResponse updatedResponse = new CustomerResponse();
        updatedResponse.setId(10L);
        updatedResponse.setCustomerCode("FIN-CUS-1001");
        updatedResponse.setPhoneNumber("+1987654321");
        updatedResponse.setCity("Gotham");

        when(userRepository.findByEmail("customer@finova.bank")).thenReturn(Optional.of(customerUser));
        when(customerService.updateCustomer(eq(10L), any(CustomerUpdateRequest.class), eq(1L))).thenReturn(updatedResponse);

        mockMvc.perform(put("/api/customers/10")
                        .principal(createAuth("customer@finova.bank", "CUSTOMER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.city").value("Gotham"))
                .andExpect(jsonPath("$.phoneNumber").value("+1987654321"));
    }

    // 6. Unauthorized profile update
    @Test
    @DisplayName("6. PUT /api/customers/{id} - Unauthorized profile update rejected (403 Forbidden)")
    void testUnauthorizedProfileUpdate() throws Exception {
        CustomerUpdateRequest updateRequest = new CustomerUpdateRequest();
        updateRequest.setCity("UnauthorizedCity");

        when(userRepository.findByEmail("customer@finova.bank")).thenReturn(Optional.of(customerUser));
        when(customerService.updateCustomer(eq(99L), any(CustomerUpdateRequest.class), eq(1L)))
                .thenThrow(new UnauthorizedCustomerAccessException("Access denied: You are not authorized to modify this customer profile"));

        mockMvc.perform(put("/api/customers/99")
                        .principal(createAuth("customer@finova.bank", "CUSTOMER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    // 7. Customer listing pagination
    @Test
    @DisplayName("7. GET /api/customers - Paginated customer listing for authorized staff (200 OK)")
    void testCustomerListingPagination() throws Exception {
        Page<CustomerResponse> page = new PageImpl<>(List.of(sampleCustomerResponse), PageRequest.of(0, 10), 1);

        when(userRepository.findByEmail("manager@finova.bank")).thenReturn(Optional.of(staffUser));
        when(customerService.getAllCustomers(isNull(), isNull(), isNull(), any(Pageable.class), eq(5L)))
                .thenReturn(page);

        mockMvc.perform(get("/api/customers?page=0&size=10")
                        .principal(createAuth("manager@finova.bank", "BRANCH_MANAGER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].customerCode").value("FIN-CUS-1001"));
    }

    // 8. Customer listing search
    @Test
    @DisplayName("8. GET /api/customers?search=Alice - Filters by search term (200 OK)")
    void testCustomerListingSearch() throws Exception {
        Page<CustomerResponse> page = new PageImpl<>(List.of(sampleCustomerResponse), PageRequest.of(0, 10), 1);

        when(userRepository.findByEmail("manager@finova.bank")).thenReturn(Optional.of(staffUser));
        when(customerService.getAllCustomers(eq("Alice"), any(), any(), any(Pageable.class), eq(5L)))
                .thenReturn(page);

        mockMvc.perform(get("/api/customers?search=Alice")
                        .principal(createAuth("manager@finova.bank", "BRANCH_MANAGER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].fullName").value("Alice Johnson"));
    }

    // 9. KYC status filtering
    @Test
    @DisplayName("9. GET /api/customers?kycStatus=VERIFIED - Filters by KYC status (200 OK)")
    void testCustomerListingKycStatusFiltering() throws Exception {
        Page<CustomerResponse> page = new PageImpl<>(List.of(sampleCustomerResponse), PageRequest.of(0, 10), 1);

        when(userRepository.findByEmail("manager@finova.bank")).thenReturn(Optional.of(staffUser));
        when(customerService.getAllCustomers(any(), eq(KycStatus.VERIFIED), any(), any(Pageable.class), eq(5L)))
                .thenReturn(page);

        mockMvc.perform(get("/api/customers?kycStatus=VERIFIED")
                        .principal(createAuth("manager@finova.bank", "BRANCH_MANAGER")))
                .andExpect(status().isOk());
    }

    // 10. City filtering
    @Test
    @DisplayName("10. GET /api/customers?city=Metropolis - Filters by city (200 OK)")
    void testCustomerListingCityFiltering() throws Exception {
        Page<CustomerResponse> page = new PageImpl<>(List.of(sampleCustomerResponse), PageRequest.of(0, 10), 1);

        when(userRepository.findByEmail("manager@finova.bank")).thenReturn(Optional.of(staffUser));
        when(customerService.getAllCustomers(any(), any(), eq("Metropolis"), any(Pageable.class), eq(5L)))
                .thenReturn(page);

        mockMvc.perform(get("/api/customers?city=Metropolis")
                        .principal(createAuth("manager@finova.bank", "BRANCH_MANAGER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].city").value("Metropolis"));
    }

    // 11. Invalid page and size parameters
    @Test
    @DisplayName("11. GET /api/customers - Rejects invalid page index and size (400 Bad Request)")
    void testInvalidPageAndSizeParameters() throws Exception {
        // Negative page index
        mockMvc.perform(get("/api/customers?page=-1&size=10")
                        .principal(createAuth("manager@finova.bank", "BRANCH_MANAGER")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Page index must not be less than zero"));

        // Size > 100
        mockMvc.perform(get("/api/customers?page=0&size=101")
                        .principal(createAuth("manager@finova.bank", "BRANCH_MANAGER")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Page size must be between 1 and 100"));

        // Size <= 0
        mockMvc.perform(get("/api/customers?page=0&size=0")
                        .principal(createAuth("manager@finova.bank", "BRANCH_MANAGER")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // 12. Invalid sort fields
    @Test
    @DisplayName("12. GET /api/customers?sortBy=unsupported - Rejects invalid sort property (400 Bad Request)")
    void testInvalidSortFields() throws Exception {
        mockMvc.perform(get("/api/customers?sortBy=secretPassword")
                        .principal(createAuth("manager@finova.bank", "BRANCH_MANAGER")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Invalid sort field")));

        // Invalid sort direction
        mockMvc.perform(get("/api/customers?sortBy=id&sortDir=sideways")
                        .principal(createAuth("manager@finova.bank", "BRANCH_MANAGER")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid sort direction: 'sideways'. Allowed values: asc, desc"));
    }

    // 13. Validation error response format
    @Test
    @DisplayName("13. POST /api/customers - Missing required fields returns validation error map (400 Bad Request)")
    void testValidationErrorResponseFormat() throws Exception {
        // Empty payload violating @NotNull and @NotBlank constraints
        CustomerCreateRequest emptyRequest = new CustomerCreateRequest();

        mockMvc.perform(post("/api/customers")
                        .principal(createAuth("customer@finova.bank", "CUSTOMER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors.phoneNumber").exists())
                .andExpect(jsonPath("$.path").value("/api/customers"));
    }

    // 14. Customer not found response
    @Test
    @DisplayName("14. GET /api/customers/{id} - Non-existent ID returns 404 Not Found")
    void testCustomerNotFoundResponse() throws Exception {
        when(userRepository.findByEmail("customer@finova.bank")).thenReturn(Optional.of(customerUser));
        when(customerService.getCustomerById(eq(999L), eq(1L)))
                .thenThrow(new CustomerNotFoundException("Customer not found with ID: 999"));

        mockMvc.perform(get("/api/customers/999")
                        .principal(createAuth("customer@finova.bank", "CUSTOMER")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Customer not found with ID: 999"))
                .andExpect(jsonPath("$.path").value("/api/customers/999"));
    }

    // 15. Duplicate customer response
    @Test
    @DisplayName("15. POST /api/customers - User with existing profile returns 409 Conflict")
    void testDuplicateCustomerResponse() throws Exception {
        String requestJson = """
                {
                  "dateOfBirth": "1990-05-15",
                  "phoneNumber": "+1234567890",
                  "address": "123 Main St",
                  "city": "Metropolis",
                  "state": "NY",
                  "postalCode": "10001",
                  "country": "USA"
                }
                """;

        when(userRepository.findByEmail("customer@finova.bank")).thenReturn(Optional.of(customerUser));
        when(customerService.createCustomer(any(CustomerCreateRequest.class), eq(1L)))
                .thenThrow(new CustomerAlreadyExistsException("A customer profile already exists for user ID: 1"));

        mockMvc.perform(post("/api/customers")
                        .principal(createAuth("customer@finova.bank", "CUSTOMER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("A customer profile already exists for user ID: 1"));
    }

    // 16. Unexpected exception response
    @Test
    @DisplayName("16. GET /api/customers/{id} - Unexpected runtime exception returns sanitized 500 error")
    void testUnexpectedExceptionResponse() throws Exception {
        when(userRepository.findByEmail("customer@finova.bank")).thenReturn(Optional.of(customerUser));
        when(customerService.getCustomerById(eq(10L), eq(1L)))
                .thenThrow(new RuntimeException("Database connection timeout or internal fault"));

        mockMvc.perform(get("/api/customers/10")
                        .principal(createAuth("customer@finova.bank", "CUSTOMER")))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("An unexpected internal server error occurred."))
                .andExpect(jsonPath("$.stackTrace").doesNotExist());
    }

    // 17. Ordinary customer cannot list customers
    @Test
    @DisplayName("17. GET /api/customers - Ordinary customer cannot list customers (403 Forbidden)")
    void testOrdinaryCustomerCannotListCustomers() throws Exception {
        when(userRepository.findByEmail("customer@finova.bank")).thenReturn(Optional.of(customerUser));
        when(customerService.getAllCustomers(any(), any(), any(), any(), eq(1L)))
                .thenThrow(new UnauthorizedCustomerAccessException("Access denied: Only authorized banking staff can list customer profiles"));

        mockMvc.perform(get("/api/customers")
                        .principal(createAuth("customer@finova.bank", "CUSTOMER")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    // 18. Get customer by customer code
    @Test
    @DisplayName("18. GET /api/customers/code/{customerCode} - Retrieves customer by code (200 OK)")
    void testGetCustomerByCodeSuccess() throws Exception {
        when(userRepository.findByEmail("customer@finova.bank")).thenReturn(Optional.of(customerUser));
        when(customerService.getCustomerByCode(eq("FIN-CUS-1001"), eq(1L))).thenReturn(sampleCustomerResponse);

        mockMvc.perform(get("/api/customers/code/FIN-CUS-1001")
                        .principal(createAuth("customer@finova.bank", "CUSTOMER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerCode").value("FIN-CUS-1001"));
    }
}
