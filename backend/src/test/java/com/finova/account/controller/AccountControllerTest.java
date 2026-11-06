package com.finova.account.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finova.controller.GlobalExceptionHandler;
import com.finova.dto.account.AccountCreateRequest;
import com.finova.dto.account.AccountResponse;
import com.finova.entity.User;
import com.finova.entity.UserRole;
import com.finova.entity.enums.AccountStatus;
import com.finova.entity.enums.AccountType;
import com.finova.exception.BankAccountNotFoundException;
import com.finova.exception.InvalidAccountOperationException;
import com.finova.exception.UnauthorizedAccountAccessException;
import com.finova.repository.UserRepository;
import com.finova.service.AccountService;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class AccountControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AccountService accountService;

    @Mock
    private UserRepository userRepository;

    private ObjectMapper objectMapper;

    private User customerUser;
    private User staffUser;
    private AccountResponse sampleAccountResponse;

    @BeforeEach
    void setUp() {
        AccountController accountController = new AccountController(accountService, userRepository);
        this.mockMvc = MockMvcBuilders.standaloneSetup(accountController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        this.objectMapper = new ObjectMapper();
        this.objectMapper.findAndRegisterModules();

        customerUser = new User("Alice Johnson", "customer@finova.bank", "hash", UserRole.CUSTOMER);
        customerUser.setId(1L);

        staffUser = new User("Robert Branch", "manager@finova.bank", "hash", UserRole.BRANCH_MANAGER);
        staffUser.setId(5L);

        sampleAccountResponse = new AccountResponse();
        sampleAccountResponse.setId(100L);
        sampleAccountResponse.setAccountNumber("FIN1029384756");
        sampleAccountResponse.setAccountType(AccountType.SAVINGS);
        sampleAccountResponse.setAccountStatus(AccountStatus.ACTIVE);
        sampleAccountResponse.setBalance(new BigDecimal("0.00"));
        sampleAccountResponse.setCurrency("INR");
        sampleAccountResponse.setBranchName("Finova Manhattan Branch");
        sampleAccountResponse.setBranchCode("BR-MAN-01");
        sampleAccountResponse.setIfscCode("FINO0001234");
        sampleAccountResponse.setCreatedAt(LocalDateTime.now());
        sampleAccountResponse.setUpdatedAt(LocalDateTime.now());
    }

    private Authentication createAuth(String email, String role) {
        return new UsernamePasswordAuthenticationToken(
                email,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
        );
    }

    @Test
    @DisplayName("1. POST /api/accounts - Successfully creates account (201 Created)")
    void testCreateAccountSuccess() throws Exception {
        AccountCreateRequest request = new AccountCreateRequest(AccountType.SAVINGS, "Finova Manhattan Branch");

        when(userRepository.findByEmail("customer@finova.bank")).thenReturn(Optional.of(customerUser));
        when(accountService.createAccount(any(AccountCreateRequest.class), eq(1L))).thenReturn(sampleAccountResponse);

        mockMvc.perform(post("/api/accounts")
                        .principal(createAuth("customer@finova.bank", "CUSTOMER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.accountNumber").value("FIN1029384756"))
                .andExpect(jsonPath("$.accountType").value("SAVINGS"))
                .andExpect(jsonPath("$.balance").value(0.00));
    }

    @Test
    @DisplayName("2. POST /api/accounts - Missing accountType returns 400 Bad Request")
    void testCreateAccountMissingAccountType() throws Exception {
        AccountCreateRequest emptyRequest = new AccountCreateRequest();

        mockMvc.perform(post("/api/accounts")
                        .principal(createAuth("customer@finova.bank", "CUSTOMER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.accountType").exists());
    }

    @Test
    @DisplayName("3. POST /api/accounts - Staff cannot create customer account (403 Forbidden)")
    void testCreateAccountStaffForbidden() throws Exception {
        AccountCreateRequest request = new AccountCreateRequest(AccountType.SAVINGS);
        when(userRepository.findByEmail("manager@finova.bank")).thenReturn(Optional.of(staffUser));

        mockMvc.perform(post("/api/accounts")
                        .principal(createAuth("manager@finova.bank", "BRANCH_MANAGER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("4. GET /api/accounts/me - Customer retrieves own accounts (200 OK)")
    void testGetMyAccountsSuccess() throws Exception {
        when(userRepository.findByEmail("customer@finova.bank")).thenReturn(Optional.of(customerUser));
        when(accountService.getMyAccounts(1L)).thenReturn(List.of(sampleAccountResponse));

        mockMvc.perform(get("/api/accounts/me")
                        .principal(createAuth("customer@finova.bank", "CUSTOMER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].accountNumber").value("FIN1029384756"));
    }

    @Test
    @DisplayName("5. GET /api/accounts/{id} - Owner retrieves account by ID (200 OK)")
    void testGetAccountByIdSuccess() throws Exception {
        when(userRepository.findByEmail("customer@finova.bank")).thenReturn(Optional.of(customerUser));
        when(accountService.getAccountById(100L, 1L)).thenReturn(sampleAccountResponse);

        mockMvc.perform(get("/api/accounts/100")
                        .principal(createAuth("customer@finova.bank", "CUSTOMER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.accountNumber").value("FIN1029384756"));
    }

    @Test
    @DisplayName("6. GET /api/accounts/{id} - Unauthorized customer receives 403 Forbidden")
    void testGetAccountByIdUnauthorized() throws Exception {
        when(userRepository.findByEmail("customer@finova.bank")).thenReturn(Optional.of(customerUser));
        when(accountService.getAccountById(999L, 1L))
                .thenThrow(new UnauthorizedAccountAccessException("Access denied: You are not authorized to access this account"));

        mockMvc.perform(get("/api/accounts/999")
                        .principal(createAuth("customer@finova.bank", "CUSTOMER")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value(containsString("Access denied")));
    }

    @Test
    @DisplayName("7. GET /api/accounts/{id} - Missing account returns 404 Not Found")
    void testGetAccountByIdNotFound() throws Exception {
        when(userRepository.findByEmail("customer@finova.bank")).thenReturn(Optional.of(customerUser));
        when(accountService.getAccountById(999L, 1L))
                .thenThrow(new BankAccountNotFoundException("Bank account not found with ID: 999"));

        mockMvc.perform(get("/api/accounts/999")
                        .principal(createAuth("customer@finova.bank", "CUSTOMER")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("8. GET /api/accounts/number/{accountNumber} - Retrieves account by number (200 OK)")
    void testGetAccountByNumberSuccess() throws Exception {
        when(userRepository.findByEmail("customer@finova.bank")).thenReturn(Optional.of(customerUser));
        when(accountService.getAccountByNumber("FIN1029384756", 1L)).thenReturn(sampleAccountResponse);

        mockMvc.perform(get("/api/accounts/number/FIN1029384756")
                        .principal(createAuth("customer@finova.bank", "CUSTOMER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("FIN1029384756"));
    }

    @Test
    @DisplayName("9. GET /api/accounts - Staff paginated listing (200 OK)")
    void testGetAllAccountsStaffSuccess() throws Exception {
        Page<AccountResponse> page = new PageImpl<>(List.of(sampleAccountResponse), PageRequest.of(0, 10), 1);
        when(userRepository.findByEmail("manager@finova.bank")).thenReturn(Optional.of(staffUser));
        when(accountService.getAllAccounts(eq(5L), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/accounts?page=0&size=10")
                        .principal(createAuth("manager@finova.bank", "BRANCH_MANAGER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].accountNumber").value("FIN1029384756"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("10. GET /api/accounts - Customer listing returns 403 Forbidden")
    void testGetAllAccountsCustomerForbidden() throws Exception {
        when(userRepository.findByEmail("customer@finova.bank")).thenReturn(Optional.of(customerUser));
        when(accountService.getAllAccounts(eq(1L), any(Pageable.class)))
                .thenThrow(new UnauthorizedAccountAccessException("Access denied: Only authorized banking staff can list all bank accounts"));

        mockMvc.perform(get("/api/accounts")
                        .principal(createAuth("customer@finova.bank", "CUSTOMER")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("11. GET /api/accounts - Invalid page and size parameters return 400 Bad Request")
    void testGetAllAccountsInvalidPageAndSize() throws Exception {
        mockMvc.perform(get("/api/accounts?page=-1&size=10")
                        .principal(createAuth("manager@finova.bank", "BRANCH_MANAGER")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Page index must not be less than zero"));

        mockMvc.perform(get("/api/accounts?page=0&size=101")
                        .principal(createAuth("manager@finova.bank", "BRANCH_MANAGER")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Page size must be between 1 and 100"));
    }

    @Test
    @DisplayName("12. GET /api/accounts - Invalid sort field and direction return 400 Bad Request")
    void testGetAllAccountsInvalidSorting() throws Exception {
        mockMvc.perform(get("/api/accounts?sortBy=unauthorizedField")
                        .principal(createAuth("manager@finova.bank", "BRANCH_MANAGER")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Invalid sort field")));

        mockMvc.perform(get("/api/accounts?sortBy=createdAt&sortDir=diagonal")
                        .principal(createAuth("manager@finova.bank", "BRANCH_MANAGER")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Invalid sort direction")));
    }

    @Test
    @DisplayName("13. PATCH /api/accounts/{id}/freeze - Staff can freeze account (200 OK)")
    void testFreezeAccountSuccess() throws Exception {
        AccountResponse frozenResponse = new AccountResponse();
        frozenResponse.setId(100L);
        frozenResponse.setAccountNumber("FIN1029384756");
        frozenResponse.setAccountStatus(AccountStatus.FROZEN);

        when(userRepository.findByEmail("manager@finova.bank")).thenReturn(Optional.of(staffUser));
        when(accountService.freezeAccount(100L, 5L)).thenReturn(frozenResponse);

        mockMvc.perform(patch("/api/accounts/100/freeze")
                        .principal(createAuth("manager@finova.bank", "BRANCH_MANAGER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountStatus").value("FROZEN"));
    }

    @Test
    @DisplayName("14. PATCH /api/accounts/{id}/freeze - Customer cannot freeze account (403 Forbidden)")
    void testFreezeAccountCustomerForbidden() throws Exception {
        when(userRepository.findByEmail("customer@finova.bank")).thenReturn(Optional.of(customerUser));
        when(accountService.freezeAccount(100L, 1L))
                .thenThrow(new UnauthorizedAccountAccessException("Access denied: Only authorized banking staff can freeze bank accounts"));

        mockMvc.perform(patch("/api/accounts/100/freeze")
                        .principal(createAuth("customer@finova.bank", "CUSTOMER")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("15. PATCH /api/accounts/{id}/freeze - CLOSED account cannot be frozen (400 Bad Request)")
    void testFreezeClosedAccountBadRequest() throws Exception {
        when(userRepository.findByEmail("manager@finova.bank")).thenReturn(Optional.of(staffUser));
        when(accountService.freezeAccount(100L, 5L))
                .thenThrow(new InvalidAccountOperationException("Cannot freeze a closed bank account"));

        mockMvc.perform(patch("/api/accounts/100/freeze")
                        .principal(createAuth("manager@finova.bank", "BRANCH_MANAGER")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(containsString("Cannot freeze a closed bank account")));
    }

    @Test
    @DisplayName("16. Exception sanitization: Unexpected exception returns 500 without stack trace")
    void testUnexpectedExceptionReturns500() throws Exception {
        when(userRepository.findByEmail("customer@finova.bank")).thenReturn(Optional.of(customerUser));
        when(accountService.getAccountById(100L, 1L)).thenThrow(new RuntimeException("Database timeout"));

        mockMvc.perform(get("/api/accounts/100")
                        .principal(createAuth("customer@finova.bank", "CUSTOMER")))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("An unexpected internal server error occurred."))
                .andExpect(jsonPath("$.stackTrace").doesNotExist());
    }
}

