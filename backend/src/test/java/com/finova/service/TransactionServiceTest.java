package com.finova.service;

import com.finova.dto.transaction.TransactionHistoryResponse;
import com.finova.dto.transaction.TransactionMapper;
import com.finova.dto.transaction.TransactionResponse;
import com.finova.entity.BankAccount;
import com.finova.entity.BankTransaction;
import com.finova.entity.Customer;
import com.finova.entity.User;
import com.finova.entity.UserRole;
import com.finova.entity.enums.AccountStatus;
import com.finova.entity.enums.AccountType;
import com.finova.entity.enums.TransactionStatus;
import com.finova.entity.enums.TransactionType;
import com.finova.exception.BankAccountNotFoundException;
import com.finova.exception.TransactionNotFoundException;
import com.finova.exception.UnauthorizedAccountAccessException;
import com.finova.repository.BankAccountRepository;
import com.finova.repository.BankTransactionRepository;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock
    private BankTransactionRepository bankTransactionRepository;

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountService accountService;

    @Spy
    private TransactionMapper transactionMapper = new TransactionMapper();

    @InjectMocks
    private TransactionService transactionService;

    private User ownerUser;
    private User otherUser;
    private User staffUser;
    private Customer ownerCustomer;
    private BankAccount testAccount;
    private BankTransaction testTransaction;

    @BeforeEach
    void setUp() {
        ownerUser = new User("Alice", "alice@finova.bank", "pwd", UserRole.CUSTOMER);
        ownerUser.setId(1L);

        otherUser = new User("Bob", "bob@finova.bank", "pwd", UserRole.CUSTOMER);
        otherUser.setId(2L);

        staffUser = new User("Manager", "manager@finova.bank", "pwd", UserRole.BRANCH_MANAGER);
        staffUser.setId(3L);

        ownerCustomer = new Customer();
        ownerCustomer.setId(10L);
        ownerCustomer.setUser(ownerUser);

        testAccount = new BankAccount("FIN1234567890", ownerCustomer, AccountType.SAVINGS);
        testAccount.setId(100L);
        testAccount.setBalance(new BigDecimal("5000.00"));
        testAccount.setAccountStatus(AccountStatus.ACTIVE);

        testTransaction = new BankTransaction();
        testTransaction.setId(500L);
        testTransaction.setTransactionReference("FIN-TXN-REF-001");
        testTransaction.setAccount(testAccount);
        testTransaction.setTransactionType(TransactionType.DEPOSIT);
        testTransaction.setTransactionStatus(TransactionStatus.COMPLETED);
        testTransaction.setAmount(new BigDecimal("1000.00"));
        testTransaction.setBalanceBefore(new BigDecimal("4000.00"));
        testTransaction.setBalanceAfter(new BigDecimal("5000.00"));
        testTransaction.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("1. Owner History: Customer can access their own account transaction history")
    void testOwnerHistoryAccess() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(ownerUser));
        when(bankAccountRepository.findById(100L)).thenReturn(Optional.of(testAccount));

        Page<BankTransaction> page = new PageImpl<>(List.of(testTransaction));
        when(bankTransactionRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        TransactionHistoryResponse response = transactionService.getTransactionHistory(
                100L, 1L, 0, 20, "createdAt", "desc", null, null, null, null
        );

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals("FIN-TXN-REF-001", response.getContent().get(0).getTransactionReference());
    }

    @Test
    @DisplayName("2. Staff History: Authorized staff can access account transaction history")
    void testStaffHistoryAccess() {
        when(userRepository.findById(3L)).thenReturn(Optional.of(staffUser));
        when(bankAccountRepository.findById(100L)).thenReturn(Optional.of(testAccount));
        when(accountService.isAuthorizedStaff(UserRole.BRANCH_MANAGER)).thenReturn(true);

        Page<BankTransaction> page = new PageImpl<>(List.of(testTransaction));
        when(bankTransactionRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        TransactionHistoryResponse response = transactionService.getTransactionHistory(
                100L, 3L, 0, 20, "createdAt", "desc", null, null, null, null
        );

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
    }

    @Test
    @DisplayName("3. Unauthorized History: Non-owner customer cannot access another customer's history")
    void testUnauthorizedCustomerHistoryAccess() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(otherUser));
        when(bankAccountRepository.findById(100L)).thenReturn(Optional.of(testAccount));
        when(accountService.isAuthorizedStaff(UserRole.CUSTOMER)).thenReturn(false);

        UnauthorizedAccountAccessException ex = assertThrows(UnauthorizedAccountAccessException.class, () ->
                transactionService.getTransactionHistory(100L, 2L, 0, 20, "createdAt", "desc", null, null, null, null)
        );
        assertTrue(ex.getMessage().contains("not authorized"));
    }

    @Test
    @DisplayName("4. Nonexistent Account History: Throws BankAccountNotFoundException when account ID not found")
    void testNonexistentAccountHistory() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(ownerUser));
        when(bankAccountRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BankAccountNotFoundException.class, () ->
                transactionService.getTransactionHistory(999L, 1L, 0, 20, "createdAt", "desc", null, null, null, null)
        );
    }

    @Test
    @DisplayName("5. Invalid Pagination: Rejects negative page and out-of-range size")
    void testInvalidPaginationParameters() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(ownerUser));
        when(bankAccountRepository.findById(100L)).thenReturn(Optional.of(testAccount));

        // Negative page
        assertThrows(IllegalArgumentException.class, () ->
                transactionService.getTransactionHistory(100L, 1L, -1, 20, "createdAt", "desc", null, null, null, null)
        );

        // Size > 100
        assertThrows(IllegalArgumentException.class, () ->
                transactionService.getTransactionHistory(100L, 1L, 0, 101, "createdAt", "desc", null, null, null, null)
        );

        // Size < 1
        assertThrows(IllegalArgumentException.class, () ->
                transactionService.getTransactionHistory(100L, 1L, 0, 0, "createdAt", "desc", null, null, null, null)
        );
    }

    @Test
    @DisplayName("6. Invalid Sort Field: Rejects disallowed sorting property")
    void testInvalidSortField() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(ownerUser));
        when(bankAccountRepository.findById(100L)).thenReturn(Optional.of(testAccount));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                transactionService.getTransactionHistory(100L, 1L, 0, 20, "unauthorizedColumn", "desc", null, null, null, null)
        );
        assertTrue(ex.getMessage().contains("Invalid sort field"));
    }

    @Test
    @DisplayName("7. Invalid Sort Direction: Rejects invalid direction string")
    void testInvalidSortDirection() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(ownerUser));
        when(bankAccountRepository.findById(100L)).thenReturn(Optional.of(testAccount));

        assertThrows(IllegalArgumentException.class, () ->
                transactionService.getTransactionHistory(100L, 1L, 0, 20, "createdAt", "invalidDir", null, null, null, null)
        );
    }

    @Test
    @DisplayName("8. Invalid Date Range: Rejects start date after end date")
    void testInvalidDateRange() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(ownerUser));
        when(bankAccountRepository.findById(100L)).thenReturn(Optional.of(testAccount));

        LocalDateTime start = LocalDateTime.of(2026, 11, 10, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 11, 5, 0, 0);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                transactionService.getTransactionHistory(100L, 1L, 0, 20, "createdAt", "desc", null, null, start, end)
        );
        assertTrue(ex.getMessage().contains("Start date cannot be after end date"));
    }

    @Test
    @DisplayName("9. Lookup by Reference: Owner successfully retrieves their transaction by reference")
    void testOwnerLookupByReference() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(ownerUser));
        when(bankTransactionRepository.findByTransactionReference("FIN-TXN-REF-001")).thenReturn(Optional.of(testTransaction));

        TransactionResponse response = transactionService.getTransactionByReference("FIN-TXN-REF-001", 1L);

        assertNotNull(response);
        assertEquals("FIN-TXN-REF-001", response.getTransactionReference());
        assertEquals(new BigDecimal("1000.00"), response.getAmount());
    }

    @Test
    @DisplayName("10. Lookup by Reference by Staff: Authorized staff retrieves transaction")
    void testStaffLookupByReference() {
        when(userRepository.findById(3L)).thenReturn(Optional.of(staffUser));
        when(bankTransactionRepository.findByTransactionReference("FIN-TXN-REF-001")).thenReturn(Optional.of(testTransaction));
        when(accountService.isAuthorizedStaff(UserRole.BRANCH_MANAGER)).thenReturn(true);

        TransactionResponse response = transactionService.getTransactionByReference("FIN-TXN-REF-001", 3L);

        assertNotNull(response);
        assertEquals("FIN-TXN-REF-001", response.getTransactionReference());
    }

    @Test
    @DisplayName("11. Unauthorized Lookup: Customer attempting to retrieve another customer's transaction is blocked")
    void testUnauthorizedCustomerLookupByReference() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(otherUser));
        when(bankTransactionRepository.findByTransactionReference("FIN-TXN-REF-001")).thenReturn(Optional.of(testTransaction));
        when(accountService.isAuthorizedStaff(UserRole.CUSTOMER)).thenReturn(false);

        UnauthorizedAccountAccessException ex = assertThrows(UnauthorizedAccountAccessException.class, () ->
                transactionService.getTransactionByReference("FIN-TXN-REF-001", 2L)
        );
        assertTrue(ex.getMessage().contains("not authorized"));
    }

    @Test
    @DisplayName("12. Nonexistent Reference: Throws TransactionNotFoundException when reference does not exist")
    void testNonexistentTransactionReference() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(ownerUser));
        when(bankTransactionRepository.findByTransactionReference("FIN-TXN-MISSING")).thenReturn(Optional.empty());

        assertThrows(TransactionNotFoundException.class, () ->
                transactionService.getTransactionByReference("FIN-TXN-MISSING", 1L)
        );
    }
}

