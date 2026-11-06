package com.finova.account.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finova.dto.account.AccountCreateRequest;
import com.finova.entity.BankAccount;
import com.finova.entity.Customer;
import com.finova.entity.User;
import com.finova.entity.UserRole;
import com.finova.entity.enums.AccountStatus;
import com.finova.entity.enums.AccountType;
import com.finova.repository.BankAccountRepository;
import com.finova.repository.CustomerRepository;
import com.finova.repository.UserRepository;
import com.finova.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
public class AccountControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private String customerToken;
    private String otherCustomerToken;
    private String staffToken;

    private User customerUser;
    private User otherCustomerUser;
    private Customer primaryCustomer;
    private Customer otherCustomer;

    @BeforeEach
    void setUp() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.findAndRegisterModules();

        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        // Retrieve seeded customer user & generate token
        customerUser = userRepository.findByEmail("customer@finova.bank")
                .orElseThrow(() -> new IllegalStateException("customer@finova.bank not seeded"));
        customerToken = jwtService.generateToken(customerUser);

        primaryCustomer = customerRepository.findByUserId(customerUser.getId())
                .orElseThrow(() -> new IllegalStateException("Primary customer profile not seeded"));

        // Retrieve seeded manager user & generate token
        User managerUser = userRepository.findByEmail("manager@finova.bank")
                .orElseThrow(() -> new IllegalStateException("manager@finova.bank not seeded"));
        staffToken = jwtService.generateToken(managerUser);

        // Provision distinct second customer for cross-customer authorization testing
        otherCustomerUser = userRepository.findByEmail("other.account.customer@finova.bank").orElseGet(() -> {
            User u = new User("Bob Smith", "other.account.customer@finova.bank", "passwordHash", UserRole.CUSTOMER);
            return userRepository.save(u);
        });
        otherCustomerToken = jwtService.generateToken(otherCustomerUser);

        if (!customerRepository.existsByUserId(otherCustomerUser.getId())) {
            Customer otherCust = new Customer(
                    "CUST-ACC-INT-02",
                    otherCustomerUser,
                    LocalDate.of(1991, 7, 21),
                    "+1-555-090-8888",
                    "789 Broadway",
                    "New York",
                    "NY",
                    "10003",
                    "USA"
            );
            otherCustomer = customerRepository.save(otherCust);
        } else {
            otherCustomer = customerRepository.findByUserId(otherCustomerUser.getId()).get();
        }

        // Clean up accounts to ensure isolation
        bankAccountRepository.deleteAll();
    }

    private BankAccount createPersistedAccount(Customer customer, String number, AccountType type, AccountStatus status) {
        BankAccount account = new BankAccount(number, customer, type);
        account.setAccountStatus(status);
        account.setBalance(new BigDecimal("100.00"));
        account.setCurrency("INR");
        account.setBranchName("Finova Manhattan Branch");
        account.setBranchCode("BR-MAN-01");
        account.setIfscCode("FINO0001234");
        return bankAccountRepository.save(account);
    }

    // ==========================================
    // 1. Account Creation Tests
    // ==========================================

    @Test
    @DisplayName("Creation 1: Authenticated CUSTOMER creates an account successfully (201 Created)")
    void testCustomerCreatesAccountSuccessfully() throws Exception {
        AccountCreateRequest request = new AccountCreateRequest(AccountType.SAVINGS, "Finova Manhattan Branch", "BR-MAN-01", "FINO0001234");

        mockMvc.perform(post("/api/accounts")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.accountNumber", startsWith("FIN")))
                .andExpect(jsonPath("$.accountType").value("SAVINGS"))
                .andExpect(jsonPath("$.accountStatus").value("ACTIVE"))
                .andExpect(jsonPath("$.balance").value(0.00))
                .andExpect(jsonPath("$.currency").value("INR"))
                .andExpect(jsonPath("$.branchName").value("Finova Manhattan Branch"))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    @DisplayName("Creation 2: Unauthenticated request is rejected with 401 Unauthorized")
    void testUnauthenticatedAccountCreationReturns401() throws Exception {
        AccountCreateRequest request = new AccountCreateRequest(AccountType.SAVINGS);

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Creation 3: Staff cannot create a customer account (403 Forbidden)")
    void testStaffCannotCreateCustomerAccount() throws Exception {
        AccountCreateRequest request = new AccountCreateRequest(AccountType.SAVINGS);

        mockMvc.perform(post("/api/accounts")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Creation 4: Invalid account type returns 400 Bad Request")
    void testInvalidAccountTypeReturns400() throws Exception {
        String invalidJson = """
                {
                    "accountType": "NON_EXISTENT_TYPE"
                }
                """;

        mockMvc.perform(post("/api/accounts")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Creation 5: Client-supplied financial or identifier fields cannot override server-generated values")
    void testClientSuppliedFieldsCannotOverrideServerGeneratedValues() throws Exception {
        String forgedPayload = """
                {
                    "accountType": "CURRENT",
                    "accountNumber": "FORGED-ACC-9999",
                    "customerId": 99999,
                    "balance": 999999.99,
                    "accountStatus": "CLOSED"
                }
                """;

        mockMvc.perform(post("/api/accounts")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(forgedPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountNumber", startsWith("FIN")))
                .andExpect(jsonPath("$.accountNumber", not("FORGED-ACC-9999")))
                .andExpect(jsonPath("$.balance").value(0.00))
                .andExpect(jsonPath("$.accountStatus").value("ACTIVE"));
    }

    // ==========================================
    // 2. Account Retrieval Tests
    // ==========================================

    @Test
    @DisplayName("Retrieval 1: Customer retrieves their own accounts (GET /api/accounts/me - 200 OK)")
    void testCustomerRetrievesOwnAccounts() throws Exception {
        createPersistedAccount(primaryCustomer, "FIN1111111111", AccountType.SAVINGS, AccountStatus.ACTIVE);
        createPersistedAccount(primaryCustomer, "FIN2222222222", AccountType.CURRENT, AccountStatus.ACTIVE);

        mockMvc.perform(get("/api/accounts/me")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].accountNumber", startsWith("FIN")))
                .andExpect(jsonPath("$[1].accountNumber", startsWith("FIN")));
    }

    @Test
    @DisplayName("Retrieval 2: Customer retrieves an account by ID when they own it (200 OK)")
    void testCustomerRetrievesAccountByIdWhenOwned() throws Exception {
        BankAccount account = createPersistedAccount(primaryCustomer, "FIN3333333333", AccountType.SAVINGS, AccountStatus.ACTIVE);

        mockMvc.perform(get("/api/accounts/" + account.getId())
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(account.getId()))
                .andExpect(jsonPath("$.accountNumber").value("FIN3333333333"));
    }

    @Test
    @DisplayName("Retrieval 3: Customer cannot retrieve another customer's account by ID (403 Forbidden)")
    void testCustomerCannotRetrieveAnotherCustomerAccountById() throws Exception {
        BankAccount account = createPersistedAccount(primaryCustomer, "FIN4444444444", AccountType.SAVINGS, AccountStatus.ACTIVE);

        mockMvc.perform(get("/api/accounts/" + account.getId())
                        .header("Authorization", "Bearer " + otherCustomerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Retrieval 4: Customer cannot retrieve another customer's account by number (403 Forbidden)")
    void testCustomerCannotRetrieveAnotherCustomerAccountByNumber() throws Exception {
        BankAccount account = createPersistedAccount(primaryCustomer, "FIN5555555555", AccountType.SAVINGS, AccountStatus.ACTIVE);

        mockMvc.perform(get("/api/accounts/number/" + account.getAccountNumber())
                        .header("Authorization", "Bearer " + otherCustomerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Retrieval 5: Authorized staff can retrieve accounts (200 OK)")
    void testAuthorizedStaffCanRetrieveAccounts() throws Exception {
        BankAccount account = createPersistedAccount(primaryCustomer, "FIN6666666666", AccountType.SAVINGS, AccountStatus.ACTIVE);

        mockMvc.perform(get("/api/accounts/" + account.getId())
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("FIN6666666666"));

        mockMvc.perform(get("/api/accounts/number/" + account.getAccountNumber())
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(account.getId()));
    }

    @Test
    @DisplayName("Retrieval 6: Nonexistent account returns 404 Not Found")
    void testNonexistentAccountReturns404() throws Exception {
        mockMvc.perform(get("/api/accounts/999999")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/accounts/number/FIN9999999999")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isNotFound());
    }

    // ==========================================
    // 3. Staff Listing Tests
    // ==========================================

    @Test
    @DisplayName("Listing 1: Authorized staff can retrieve paginated accounts (200 OK)")
    void testAuthorizedStaffCanRetrievePaginatedAccounts() throws Exception {
        createPersistedAccount(primaryCustomer, "FIN7777777771", AccountType.SAVINGS, AccountStatus.ACTIVE);
        createPersistedAccount(primaryCustomer, "FIN7777777772", AccountType.CURRENT, AccountStatus.ACTIVE);

        mockMvc.perform(get("/api/accounts?page=0&size=10")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    @DisplayName("Listing 2: Pagination parameters work correctly")
    void testPaginationParametersWorkCorrectly() throws Exception {
        createPersistedAccount(primaryCustomer, "FIN8888888881", AccountType.SAVINGS, AccountStatus.ACTIVE);
        createPersistedAccount(primaryCustomer, "FIN8888888882", AccountType.CURRENT, AccountStatus.ACTIVE);
        createPersistedAccount(primaryCustomer, "FIN8888888883", AccountType.FIXED_DEPOSIT, AccountStatus.ACTIVE);

        mockMvc.perform(get("/api/accounts?page=0&size=2")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.number").value(0));
    }

    @Test
    @DisplayName("Listing 3: Sorting works for allowed fields (200 OK)")
    void testSortingWorksForAllowedFields() throws Exception {
        createPersistedAccount(primaryCustomer, "FIN1000000001", AccountType.SAVINGS, AccountStatus.ACTIVE);
        createPersistedAccount(primaryCustomer, "FIN2000000002", AccountType.CURRENT, AccountStatus.ACTIVE);

        mockMvc.perform(get("/api/accounts?sortBy=accountNumber&sortDir=asc")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].accountNumber").value("FIN1000000001"))
                .andExpect(jsonPath("$.content[1].accountNumber").value("FIN2000000002"));
    }

    @Test
    @DisplayName("Listing 4: Invalid sorting fields are rejected safely (400 Bad Request)")
    void testInvalidSortingFieldsRejected() throws Exception {
        mockMvc.perform(get("/api/accounts?sortBy=maliciousPayload")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Invalid sort field")));
    }

    @Test
    @DisplayName("Listing 5: CUSTOMER receives 403 Forbidden on staff listing")
    void testCustomerReceives403OnStaffListing() throws Exception {
        mockMvc.perform(get("/api/accounts")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Listing 6: Unauthenticated requests receive 401 Unauthorized")
    void testUnauthenticatedRequestsReceive401OnListing() throws Exception {
        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isUnauthorized());
    }

    // ==========================================
    // 4. Account Freezing Tests
    // ==========================================

    @Test
    @DisplayName("Freezing 1: Authorized staff can freeze an ACTIVE account (200 OK)")
    void testAuthorizedStaffCanFreezeActiveAccount() throws Exception {
        BankAccount account = createPersistedAccount(primaryCustomer, "FIN9990001111", AccountType.SAVINGS, AccountStatus.ACTIVE);

        mockMvc.perform(patch("/api/accounts/" + account.getId() + "/freeze")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountStatus").value("FROZEN"))
                .andExpect(jsonPath("$.accountNumber").value("FIN9990001111"));
    }

    @Test
    @DisplayName("Freezing 2: CUSTOMER cannot freeze an account (403 Forbidden)")
    void testCustomerCannotFreezeAccount() throws Exception {
        BankAccount account = createPersistedAccount(primaryCustomer, "FIN9990002222", AccountType.SAVINGS, AccountStatus.ACTIVE);

        mockMvc.perform(patch("/api/accounts/" + account.getId() + "/freeze")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Freezing 3: Unauthenticated request receives 401 Unauthorized")
    void testUnauthenticatedRequestReceives401OnFreeze() throws Exception {
        BankAccount account = createPersistedAccount(primaryCustomer, "FIN9990003333", AccountType.SAVINGS, AccountStatus.ACTIVE);

        mockMvc.perform(patch("/api/accounts/" + account.getId() + "/freeze"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Freezing 4: Nonexistent account returns 404 Not Found")
    void testNonexistentAccountFreezeReturns404() throws Exception {
        mockMvc.perform(patch("/api/accounts/999999/freeze")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Freezing 5: CLOSED account cannot be frozen (400 Bad Request)")
    void testClosedAccountCannotBeFrozen() throws Exception {
        BankAccount account = createPersistedAccount(primaryCustomer, "FIN9990004444", AccountType.SAVINGS, AccountStatus.CLOSED);

        mockMvc.perform(patch("/api/accounts/" + account.getId() + "/freeze")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Cannot freeze a closed bank account")));
    }

    @Test
    @DisplayName("Freezing 6: Repeated freezing behaves consistently with existing service rules (200 OK)")
    void testRepeatedFreezingBehavesConsistently() throws Exception {
        BankAccount account = createPersistedAccount(primaryCustomer, "FIN9990005555", AccountType.SAVINGS, AccountStatus.ACTIVE);

        // First freeze
        mockMvc.perform(patch("/api/accounts/" + account.getId() + "/freeze")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountStatus").value("FROZEN"));

        // Second freeze on already frozen account
        mockMvc.perform(patch("/api/accounts/" + account.getId() + "/freeze")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountStatus").value("FROZEN"));
    }
}
