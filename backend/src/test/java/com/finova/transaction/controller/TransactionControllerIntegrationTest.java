package com.finova.transaction.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finova.dto.transaction.DepositRequest;
import com.finova.dto.transaction.WithdrawalRequest;
import com.finova.entity.BankAccount;
import com.finova.entity.BankTransaction;
import com.finova.entity.Customer;
import com.finova.entity.User;
import com.finova.entity.UserRole;
import com.finova.entity.enums.AccountStatus;
import com.finova.entity.enums.AccountType;
import com.finova.entity.enums.TransactionStatus;
import com.finova.entity.enums.TransactionType;
import com.finova.repository.BankAccountRepository;
import com.finova.repository.BankTransactionRepository;
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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
public class TransactionControllerIntegrationTest {

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

    @Autowired
    private BankTransactionRepository bankTransactionRepository;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private String customerToken;
    private String otherCustomerToken;
    private String staffToken;

    private User customerUser;
    private User otherCustomerUser;
    private Customer primaryCustomer;
    private Customer otherCustomer;

    private BankAccount primaryAccount;
    private BankAccount otherAccount;

    @BeforeEach
    void setUp() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.findAndRegisterModules();

        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        // 1. Retrieve or provision customer user & generate token
        customerUser = userRepository.findByEmail("customer@finova.bank").orElseGet(() -> {
            User u = new User("Sophia Rodriguez", "customer@finova.bank", "passwordHash", UserRole.CUSTOMER);
            return userRepository.save(u);
        });
        customerToken = jwtService.generateToken(customerUser);

        primaryCustomer = customerRepository.findByUserId(customerUser.getId()).orElseGet(() -> {
            Customer c = new Customer();
            c.setCustomerCode("CUST-ACC-INT-01");
            c.setUser(customerUser);
            c.setDateOfBirth(LocalDate.of(1990, 5, 15));
            c.setPhoneNumber("+1-555-010-9999");
            c.setAddress("123 Main St");
            c.setCity("New York");
            c.setState("NY");
            c.setPostalCode("10001");
            c.setCountry("USA");
            return customerRepository.save(c);
        });

        // 2. Retrieve or provision manager user & generate token
        User managerUser = userRepository.findByEmail("manager@finova.bank").orElseGet(() -> {
            User m = new User("Ethan Wright", "manager@finova.bank", "passwordHash", UserRole.BRANCH_MANAGER);
            return userRepository.save(m);
        });
        staffToken = jwtService.generateToken(managerUser);

        // 3. Provision distinct second customer for cross-customer authorization testing
        otherCustomerUser = userRepository.findByEmail("other.tx.customer@finova.bank").orElseGet(() -> {
            User u = new User("Bob TX", "other.tx.customer@finova.bank", "passwordHash", UserRole.CUSTOMER);
            return userRepository.save(u);
        });
        otherCustomerToken = jwtService.generateToken(otherCustomerUser);

        if (!customerRepository.existsByUserId(otherCustomerUser.getId())) {
            Customer otherCust = new Customer(
                    "CUST-TX-INT-02",
                    otherCustomerUser,
                    LocalDate.of(1991, 7, 21),
                    "+1-555-090-7777",
                    "456 Park Avenue",
                    "New York",
                    "NY",
                    "10022",
                    "USA"
            );
            otherCustomer = customerRepository.save(otherCust);
        } else {
            otherCustomer = customerRepository.findByUserId(otherCustomerUser.getId()).get();
        }

        // Clean transactions and accounts
        bankTransactionRepository.deleteAll();
        bankAccountRepository.deleteAll();

        // Provision primary test accounts
        primaryAccount = new BankAccount("FIN1111222233", primaryCustomer, AccountType.SAVINGS);
        primaryAccount.setBalance(new BigDecimal("1000.00").setScale(2, RoundingMode.HALF_UP));
        primaryAccount.setAccountStatus(AccountStatus.ACTIVE);
        primaryAccount.setCurrency("INR");
        primaryAccount = bankAccountRepository.save(primaryAccount);

        otherAccount = new BankAccount("FIN9999888877", otherCustomer, AccountType.SAVINGS);
        otherAccount.setBalance(new BigDecimal("500.00").setScale(2, RoundingMode.HALF_UP));
        otherAccount.setAccountStatus(AccountStatus.ACTIVE);
        otherAccount.setCurrency("INR");
        otherAccount = bankAccountRepository.save(otherAccount);
    }

    // ==========================================
    // 1. Deposit REST API Tests
    // ==========================================

    @Test
    @DisplayName("1. Deposit API: Successful deposit returns 201 Created with safe TransactionResponse")
    void testSuccessfulDepositApi() throws Exception {
        DepositRequest request = new DepositRequest(new BigDecimal("250.00"), "Deposit test", "INT-DEP-KEY-001");

        mockMvc.perform(post("/api/accounts/" + primaryAccount.getId() + "/deposits")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionReference", startsWith("FIN-TXN-")))
                .andExpect(jsonPath("$.transactionType", is("DEPOSIT")))
                .andExpect(jsonPath("$.transactionStatus", is("COMPLETED")))
                .andExpect(jsonPath("$.amount", is(250.00)))
                .andExpect(jsonPath("$.balanceBefore", is(1000.00)))
                .andExpect(jsonPath("$.balanceAfter", is(1250.00)))
                .andExpect(jsonPath("$.currency", is("INR")));

        // Verify account balance updated in DB
        BankAccount updated = bankAccountRepository.findById(primaryAccount.getId()).orElseThrow();
        assertEquals(new BigDecimal("1250.00"), updated.getBalance());
    }

    @Test
    @DisplayName("2. Deposit API: Rejects negative deposit amount with 400 Bad Request")
    void testNegativeDepositAmountRejected() throws Exception {
        DepositRequest request = new DepositRequest(new BigDecimal("-50.00"), "Negative test", "INT-DEP-KEY-002");

        mockMvc.perform(post("/api/accounts/" + primaryAccount.getId() + "/deposits")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("3. Deposit API: Rejects zero deposit amount with 400 Bad Request")
    void testZeroDepositAmountRejected() throws Exception {
        DepositRequest request = new DepositRequest(BigDecimal.ZERO, "Zero test", "INT-DEP-KEY-003");

        mockMvc.perform(post("/api/accounts/" + primaryAccount.getId() + "/deposits")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("4. Deposit API: Unauthenticated request returns 401 Unauthorized")
    void testUnauthenticatedDepositRejected() throws Exception {
        DepositRequest request = new DepositRequest(new BigDecimal("100.00"), "Unauth test", "INT-DEP-KEY-004");

        mockMvc.perform(post("/api/accounts/" + primaryAccount.getId() + "/deposits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("5. Deposit API: Unauthorized customer attempting to deposit into another customer's account returns 403")
    void testUnauthorizedAccountDepositForbidden() throws Exception {
        DepositRequest request = new DepositRequest(new BigDecimal("100.00"), "Cross account test", "INT-DEP-KEY-005");

        mockMvc.perform(post("/api/accounts/" + otherAccount.getId() + "/deposits")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("6. Deposit API: Idempotent replay with same key returns original response without mutating balance")
    void testDepositIdempotencyReplay() throws Exception {
        DepositRequest request = new DepositRequest(new BigDecimal("100.00"), "Idempotency test", "INT-DEP-IDEMP-001");

        // First call
        mockMvc.perform(post("/api/accounts/" + primaryAccount.getId() + "/deposits")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.balanceAfter", is(1100.00)));

        // Second identical call
        mockMvc.perform(post("/api/accounts/" + primaryAccount.getId() + "/deposits")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.balanceAfter", is(1100.00)));

        // Verify account balance was only incremented once
        BankAccount updated = bankAccountRepository.findById(primaryAccount.getId()).orElseThrow();
        assertEquals(new BigDecimal("1100.00"), updated.getBalance());
    }

    // ==========================================
    // 2. Withdrawal REST API Tests
    // ==========================================

    @Test
    @DisplayName("7. Withdrawal API: Successful withdrawal returns 200 OK with safe TransactionResponse")
    void testSuccessfulWithdrawalApi() throws Exception {
        WithdrawalRequest request = new WithdrawalRequest(new BigDecimal("300.00"), "Withdrawal test", "INT-WTH-KEY-001");

        mockMvc.perform(post("/api/accounts/" + primaryAccount.getId() + "/withdrawals")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionReference", startsWith("FIN-TXN-")))
                .andExpect(jsonPath("$.transactionType", is("WITHDRAWAL")))
                .andExpect(jsonPath("$.transactionStatus", is("COMPLETED")))
                .andExpect(jsonPath("$.amount", is(300.00)))
                .andExpect(jsonPath("$.balanceBefore", is(1000.00)))
                .andExpect(jsonPath("$.balanceAfter", is(700.00)));

        // Verify account balance updated in DB
        BankAccount updated = bankAccountRepository.findById(primaryAccount.getId()).orElseThrow();
        assertEquals(new BigDecimal("700.00"), updated.getBalance());
    }

    @Test
    @DisplayName("8. Withdrawal API: Insufficient balance returns 400 Bad Request")
    void testInsufficientBalanceWithdrawalRejected() throws Exception {
        WithdrawalRequest request = new WithdrawalRequest(new BigDecimal("5000.00"), "Overdraft test", "INT-WTH-KEY-002");

        mockMvc.perform(post("/api/accounts/" + primaryAccount.getId() + "/withdrawals")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Insufficient funds")));
    }

    @Test
    @DisplayName("9. Withdrawal API: Unauthenticated request returns 401 Unauthorized")
    void testUnauthenticatedWithdrawalRejected() throws Exception {
        WithdrawalRequest request = new WithdrawalRequest(new BigDecimal("100.00"), "Unauth test", "INT-WTH-KEY-003");

        mockMvc.perform(post("/api/accounts/" + primaryAccount.getId() + "/withdrawals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("10. Withdrawal API: Unauthorized customer attempting to withdraw from another customer's account returns 403")
    void testUnauthorizedAccountWithdrawalForbidden() throws Exception {
        WithdrawalRequest request = new WithdrawalRequest(new BigDecimal("50.00"), "Cross account test", "INT-WTH-KEY-004");

        mockMvc.perform(post("/api/accounts/" + otherAccount.getId() + "/withdrawals")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // ==========================================
    // 3. Customer Transaction History Tests
    // ==========================================

    @Test
    @DisplayName("11. History API: Customer can access their own account transaction history")
    void testCustomerAccessOwnTransactionHistory() throws Exception {
        seedSampleTransactions();

        mockMvc.perform(get("/api/accounts/" + primaryAccount.getId() + "/transactions")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.totalElements", is(3)))
                .andExpect(jsonPath("$.page", is(0)));
    }

    @Test
    @DisplayName("12. History API: Customer accessing another customer's history returns 403 Forbidden")
    void testCustomerAccessOtherCustomerHistoryForbidden() throws Exception {
        mockMvc.perform(get("/api/accounts/" + otherAccount.getId() + "/transactions")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("13. History API: Staff member can view any customer's account transaction history")
    void testStaffAccessTransactionHistory() throws Exception {
        seedSampleTransactions();

        mockMvc.perform(get("/api/accounts/" + primaryAccount.getId() + "/transactions")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.totalElements", is(3)));
    }

    @Test
    @DisplayName("14. History API: Supports pagination with page and size")
    void testTransactionHistoryPagination() throws Exception {
        seedSampleTransactions();

        mockMvc.perform(get("/api/accounts/" + primaryAccount.getId() + "/transactions")
                        .header("Authorization", "Bearer " + customerToken)
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements", is(3)))
                .andExpect(jsonPath("$.totalPages", is(2)));
    }

    @Test
    @DisplayName("15. History API: Supports sorting by amount ascending")
    void testTransactionHistorySorting() throws Exception {
        seedSampleTransactions();

        mockMvc.perform(get("/api/accounts/" + primaryAccount.getId() + "/transactions")
                        .header("Authorization", "Bearer " + customerToken)
                        .param("sortBy", "amount")
                        .param("direction", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].amount", is(100.00)))
                .andExpect(jsonPath("$.content[1].amount", is(200.00)))
                .andExpect(jsonPath("$.content[2].amount", is(300.00)));
    }

    @Test
    @DisplayName("16. History API: Supports filtering by transaction type (DEPOSIT)")
    void testTransactionHistoryFilterByType() throws Exception {
        seedSampleTransactions();

        mockMvc.perform(get("/api/accounts/" + primaryAccount.getId() + "/transactions")
                        .header("Authorization", "Bearer " + customerToken)
                        .param("type", "DEPOSIT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].transactionType", is("DEPOSIT")))
                .andExpect(jsonPath("$.content[1].transactionType", is("DEPOSIT")));
    }

    @Test
    @DisplayName("17. History API: Supports filtering by transaction status (COMPLETED)")
    void testTransactionHistoryFilterByStatus() throws Exception {
        seedSampleTransactions();

        mockMvc.perform(get("/api/accounts/" + primaryAccount.getId() + "/transactions")
                        .header("Authorization", "Bearer " + customerToken)
                        .param("status", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)));
    }

    @Test
    @DisplayName("18. History API: Supports date range filtering")
    void testTransactionHistoryFilterByDateRange() throws Exception {
        seedSampleTransactions();

        String today = LocalDate.now().toString();

        mockMvc.perform(get("/api/accounts/" + primaryAccount.getId() + "/transactions")
                        .header("Authorization", "Bearer " + customerToken)
                        .param("startDate", today)
                        .param("endDate", today))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)));
    }

    @Test
    @DisplayName("19. History API: Rejects invalid date range (startDate after endDate) with 400 Bad Request")
    void testTransactionHistoryInvalidDateRange() throws Exception {
        mockMvc.perform(get("/api/accounts/" + primaryAccount.getId() + "/transactions")
                        .header("Authorization", "Bearer " + customerToken)
                        .param("startDate", "2026-11-20")
                        .param("endDate", "2026-11-10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Start date cannot be after end date")));
    }

    // ==========================================
    // 4. Transaction Reference Lookup Tests
    // ==========================================

    @Test
    @DisplayName("20. Lookup API: Customer can retrieve their own transaction by reference")
    void testLookupTransactionByReference() throws Exception {
        BankTransaction tx = new BankTransaction();
        tx.setTransactionReference("FIN-TXN-LOOKUP-001");
        tx.setAccount(primaryAccount);
        tx.setTransactionType(TransactionType.DEPOSIT);
        tx.setTransactionStatus(TransactionStatus.COMPLETED);
        tx.setAmount(new BigDecimal("150.00"));
        tx.setCurrency("INR");
        tx.setBalanceBefore(new BigDecimal("1000.00"));
        tx.setBalanceAfter(new BigDecimal("1150.00"));
        tx.setDescription("Lookup test");
        bankTransactionRepository.save(tx);

        mockMvc.perform(get("/api/transactions/FIN-TXN-LOOKUP-001")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionReference", is("FIN-TXN-LOOKUP-001")))
                .andExpect(jsonPath("$.amount", is(150.00)))
                .andExpect(jsonPath("$.accountNumber", is(primaryAccount.getAccountNumber())));
    }

    @Test
    @DisplayName("21. Lookup API: Nonexistent reference returns 404 Not Found")
    void testLookupNonexistentReferenceNotFound() throws Exception {
        mockMvc.perform(get("/api/transactions/FIN-TXN-DOES-NOT-EXIST")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("22. Lookup API: Customer attempting to retrieve another customer's transaction returns 403 Forbidden")
    void testLookupOtherCustomerTransactionForbidden() throws Exception {
        BankTransaction otherTx = new BankTransaction();
        otherTx.setTransactionReference("FIN-TXN-OTHER-001");
        otherTx.setAccount(otherAccount);
        otherTx.setTransactionType(TransactionType.DEPOSIT);
        otherTx.setTransactionStatus(TransactionStatus.COMPLETED);
        otherTx.setAmount(new BigDecimal("50.00"));
        otherTx.setCurrency("INR");
        otherTx.setBalanceBefore(new BigDecimal("500.00"));
        otherTx.setBalanceAfter(new BigDecimal("550.00"));
        bankTransactionRepository.save(otherTx);

        mockMvc.perform(get("/api/transactions/FIN-TXN-OTHER-001")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }

    private void seedSampleTransactions() {
        // Transaction 1: Deposit 100.00
        BankTransaction t1 = new BankTransaction();
        t1.setTransactionReference("FIN-TXN-SEED-001");
        t1.setAccount(primaryAccount);
        t1.setTransactionType(TransactionType.DEPOSIT);
        t1.setTransactionStatus(TransactionStatus.COMPLETED);
        t1.setAmount(new BigDecimal("100.00"));
        t1.setCurrency("INR");
        t1.setBalanceBefore(new BigDecimal("1000.00"));
        t1.setBalanceAfter(new BigDecimal("1100.00"));
        bankTransactionRepository.save(t1);

        // Transaction 2: Withdrawal 200.00
        BankTransaction t2 = new BankTransaction();
        t2.setTransactionReference("FIN-TXN-SEED-002");
        t2.setAccount(primaryAccount);
        t2.setTransactionType(TransactionType.WITHDRAWAL);
        t2.setTransactionStatus(TransactionStatus.COMPLETED);
        t2.setAmount(new BigDecimal("200.00"));
        t2.setCurrency("INR");
        t2.setBalanceBefore(new BigDecimal("1100.00"));
        t2.setBalanceAfter(new BigDecimal("900.00"));
        bankTransactionRepository.save(t2);

        // Transaction 3: Deposit 300.00
        BankTransaction t3 = new BankTransaction();
        t3.setTransactionReference("FIN-TXN-SEED-003");
        t3.setAccount(primaryAccount);
        t3.setTransactionType(TransactionType.DEPOSIT);
        t3.setTransactionStatus(TransactionStatus.COMPLETED);
        t3.setAmount(new BigDecimal("300.00"));
        t3.setCurrency("INR");
        t3.setBalanceBefore(new BigDecimal("900.00"));
        t3.setBalanceAfter(new BigDecimal("1200.00"));
        bankTransactionRepository.save(t3);
    }
}

