package com.finova.transaction.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finova.dto.transaction.DepositRequest;
import com.finova.dto.transaction.TransactionHistoryResponse;
import com.finova.dto.transaction.TransactionResponse;
import com.finova.dto.transaction.WithdrawalRequest;
import com.finova.entity.User;
import com.finova.entity.UserRole;
import com.finova.entity.enums.TransactionStatus;
import com.finova.entity.enums.TransactionType;
import com.finova.repository.UserRepository;
import com.finova.service.DepositService;
import com.finova.service.TransactionService;
import com.finova.service.WithdrawalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class TransactionControllerTest {

    @Mock
    private DepositService depositService;

    @Mock
    private WithdrawalService withdrawalService;

    @Mock
    private TransactionService transactionService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TransactionController transactionController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private User testUser;
    private Authentication mockAuth;

    @BeforeEach
    void setUp() {
        this.objectMapper = new ObjectMapper();
        this.mockMvc = MockMvcBuilders.standaloneSetup(transactionController).build();

        testUser = new User("Alice Tester", "alice.test@finova.bank", "hashPwd", UserRole.CUSTOMER);
        testUser.setId(42L);

        mockAuth = new UsernamePasswordAuthenticationToken(
                "alice.test@finova.bank",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
        );
    }

    @Test
    @DisplayName("1. Controller Deposit: Delegates to DepositService and returns 201 Created")
    void testDepositEndpoint() throws Exception {
        when(userRepository.findByEmail("alice.test@finova.bank")).thenReturn(Optional.of(testUser));

        TransactionResponse response = new TransactionResponse();
        response.setId(1L);
        response.setTransactionReference("FIN-TXN-DEP-111");
        response.setAmount(new BigDecimal("100.00"));
        response.setTransactionType(TransactionType.DEPOSIT);
        response.setTransactionStatus(TransactionStatus.COMPLETED);

        when(depositService.deposit(eq(10L), eq(42L), any(DepositRequest.class))).thenReturn(response);

        DepositRequest request = new DepositRequest(new BigDecimal("100.00"), "Deposit test", "KEY-DEP-001");

        mockMvc.perform(post("/api/accounts/10/deposits")
                        .principal(mockAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionReference", is("FIN-TXN-DEP-111")))
                .andExpect(jsonPath("$.amount", is(100.00)));

        verify(depositService).deposit(eq(10L), eq(42L), any(DepositRequest.class));
    }

    @Test
    @DisplayName("2. Controller Withdrawal: Delegates to WithdrawalService and returns 200 OK")
    void testWithdrawEndpoint() throws Exception {
        when(userRepository.findByEmail("alice.test@finova.bank")).thenReturn(Optional.of(testUser));

        TransactionResponse response = new TransactionResponse();
        response.setId(2L);
        response.setTransactionReference("FIN-TXN-WTH-222");
        response.setAmount(new BigDecimal("50.00"));
        response.setTransactionType(TransactionType.WITHDRAWAL);
        response.setTransactionStatus(TransactionStatus.COMPLETED);

        when(withdrawalService.withdraw(eq(10L), eq(42L), any(WithdrawalRequest.class))).thenReturn(response);

        WithdrawalRequest request = new WithdrawalRequest(new BigDecimal("50.00"), "Withdraw test", "KEY-WTH-001");

        mockMvc.perform(post("/api/accounts/10/withdrawals")
                        .principal(mockAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionReference", is("FIN-TXN-WTH-222")))
                .andExpect(jsonPath("$.amount", is(50.00)));

        verify(withdrawalService).withdraw(eq(10L), eq(42L), any(WithdrawalRequest.class));
    }

    @Test
    @DisplayName("3. Controller History: Delegates to TransactionService and returns 200 OK")
    void testHistoryEndpoint() throws Exception {
        when(userRepository.findByEmail("alice.test@finova.bank")).thenReturn(Optional.of(testUser));

        TransactionHistoryResponse response = new TransactionHistoryResponse(
                List.of(), 0, 20, 0, 0, true, true, true
        );

        when(transactionService.getTransactionHistory(
                eq(10L), eq(42L), eq(0), eq(20), eq("createdAt"), eq("desc"),
                isNull(), isNull(), isNull(), isNull()
        )).thenReturn(response);

        mockMvc.perform(get("/api/accounts/10/transactions")
                        .principal(mockAuth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page", is(0)))
                .andExpect(jsonPath("$.totalElements", is(0)));

        verify(transactionService).getTransactionHistory(
                eq(10L), eq(42L), eq(0), eq(20), eq("createdAt"), eq("desc"),
                isNull(), isNull(), isNull(), isNull()
        );
    }

    @Test
    @DisplayName("4. Controller Lookup: Delegates to TransactionService and returns 200 OK")
    void testLookupByReferenceEndpoint() throws Exception {
        when(userRepository.findByEmail("alice.test@finova.bank")).thenReturn(Optional.of(testUser));

        TransactionResponse response = new TransactionResponse();
        response.setId(3L);
        response.setTransactionReference("FIN-TXN-REF-333");
        response.setAmount(new BigDecimal("200.00"));

        when(transactionService.getTransactionByReference("FIN-TXN-REF-333", 42L)).thenReturn(response);

        mockMvc.perform(get("/api/transactions/FIN-TXN-REF-333")
                        .principal(mockAuth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionReference", is("FIN-TXN-REF-333")))
                .andExpect(jsonPath("$.amount", is(200.00)));

        verify(transactionService).getTransactionByReference("FIN-TXN-REF-333", 42L);
    }
}

