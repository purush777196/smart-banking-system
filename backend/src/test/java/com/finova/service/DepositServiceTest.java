package com.finova.service;

import com.finova.dto.transaction.DepositRequest;
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
import com.finova.exception.IdempotencyConflictException;
import com.finova.exception.InactiveAccountException;
import com.finova.exception.UnauthorizedAccountAccessException;
import com.finova.repository.BankAccountRepository;
import com.finova.repository.BankTransactionRepository;
import com.finova.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DepositServiceTest {

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private BankTransactionRepository bankTransactionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionReferenceGenerator transactionReferenceGenerator;

    @Spy
    private TransactionMapper transactionMapper = new TransactionMapper();

    @InjectMocks
    private DepositService depositService;

    private User ownerUser;
    private Customer ownerCustomer;
    private BankAccount activeAccount;

    @BeforeEach
    void setUp() {
        ownerUser = new User("Alice Doe", "alice@finova.bank", "hashPassword", UserRole.CUSTOMER);
        ownerUser.setId(101L);

        ownerCustomer = new Customer();
        ownerCustomer.setId(201L);
        ownerCustomer.setUser(ownerUser);

        activeAccount = new BankAccount("FIN1000000001", ownerCustomer, AccountType.SAVINGS);
        activeAccount.setId(301L);
        activeAccount.setBalance(new BigDecimal("1000.00").setScale(2, RoundingMode.HALF_UP));
        activeAccount.setAccountStatus(AccountStatus.ACTIVE);
        activeAccount.setCurrency("INR");
    }

    // 1. Successful deposit
    @Test
    @DisplayName("1. Successful Deposit: Atomically deposits amount and returns completed transaction response")
    void testSuccessfulDeposit() {
        DepositRequest request = new DepositRequest(new BigDecimal("500.00"), "Salary deposit", "IDEMP-DEP-001");

        when(userRepository.findById(101L)).thenReturn(Optional.of(ownerUser));
        when(bankAccountRepository.findByIdForUpdate(301L)).thenReturn(Optional.of(activeAccount));
        when(bankTransactionRepository.findByIdempotencyKey("IDEMP-DEP-001")).thenReturn(Optional.empty());
        when(transactionReferenceGenerator.generateReference()).thenReturn("FIN-TXN-DEP-001");
        when(bankTransactionRepository.save(any(BankTransaction.class))).thenAnswer(invocation -> {
            BankTransaction tx = invocation.getArgument(0);
            tx.setId(1L);
            tx.setCreatedAt(LocalDateTime.now());
            return tx;
        });

        TransactionResponse response = depositService.deposit(301L, 101L, request);

        assertNotNull(response);
        assertEquals(TransactionType.DEPOSIT, response.getTransactionType());
        assertEquals(TransactionStatus.COMPLETED, response.getTransactionStatus());
        assertEquals(new BigDecimal("500.00"), response.getAmount());
        assertEquals(new BigDecimal("1000.00"), response.getBalanceBefore());
        assertEquals(new BigDecimal("1500.00"), response.getBalanceAfter());
        assertEquals("FIN1000000001", response.getAccountNumber());
        assertEquals("FIN-TXN-DEP-001", response.getTransactionReference());

        // Verify account balance updated
        assertEquals(new BigDecimal("1500.00"), activeAccount.getBalance());
        verify(bankAccountRepository).save(activeAccount);
        verify(bankTransactionRepository).save(any(BankTransaction.class));
    }

    // 2. Correct balance calculation
    @Test
    @DisplayName("2. Balance Calculation: Strictly computes new balance using BigDecimal addition")
    void testCorrectBalanceCalculation() {
        activeAccount.setBalance(new BigDecimal("0.00"));
        DepositRequest request = new DepositRequest(new BigDecimal("0.01"), "Micro deposit", "IDEMP-DEP-002");

        when(userRepository.findById(101L)).thenReturn(Optional.of(ownerUser));
        when(bankAccountRepository.findByIdForUpdate(301L)).thenReturn(Optional.of(activeAccount));
        when(bankTransactionRepository.findByIdempotencyKey("IDEMP-DEP-002")).thenReturn(Optional.empty());
        when(transactionReferenceGenerator.generateReference()).thenReturn("FIN-TXN-DEP-002");
        when(bankTransactionRepository.save(any(BankTransaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TransactionResponse response = depositService.deposit(301L, 101L, request);

        assertEquals(new BigDecimal("0.01"), response.getBalanceAfter());
        assertEquals(new BigDecimal("0.01"), activeAccount.getBalance());
    }

    // 3. Correct transaction type
    @Test
    @DisplayName("3. Transaction Type: Verifies ledger entry has TransactionType.DEPOSIT")
    void testCorrectTransactionType() {
        DepositRequest request = new DepositRequest(new BigDecimal("250.00"), "IDEMP-DEP-003");

        when(userRepository.findById(101L)).thenReturn(Optional.of(ownerUser));
        when(bankAccountRepository.findByIdForUpdate(301L)).thenReturn(Optional.of(activeAccount));
        when(bankTransactionRepository.findByIdempotencyKey("IDEMP-DEP-003")).thenReturn(Optional.empty());
        when(transactionReferenceGenerator.generateReference()).thenReturn("FIN-TXN-DEP-003");

        ArgumentCaptor<BankTransaction> txCaptor = ArgumentCaptor.forClass(BankTransaction.class);
        when(bankTransactionRepository.save(txCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        depositService.deposit(301L, 101L, request);

        BankTransaction captured = txCaptor.getValue();
        assertEquals(TransactionType.DEPOSIT, captured.getTransactionType());
    }

    // 4. Correct balanceBefore and balanceAfter
    @Test
    @DisplayName("4. Balance Snapshots: Accurately records balance before and after deposit")
    void testCorrectBalanceBeforeAndAfter() {
        activeAccount.setBalance(new BigDecimal("1234.56"));
        DepositRequest request = new DepositRequest(new BigDecimal("654.32"), "IDEMP-DEP-004");

        when(userRepository.findById(101L)).thenReturn(Optional.of(ownerUser));
        when(bankAccountRepository.findByIdForUpdate(301L)).thenReturn(Optional.of(activeAccount));
        when(bankTransactionRepository.findByIdempotencyKey("IDEMP-DEP-004")).thenReturn(Optional.empty());
        when(transactionReferenceGenerator.generateReference()).thenReturn("FIN-TXN-DEP-004");
        when(bankTransactionRepository.save(any(BankTransaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TransactionResponse response = depositService.deposit(301L, 101L, request);

        assertEquals(new BigDecimal("1234.56"), response.getBalanceBefore());
        assertEquals(new BigDecimal("1888.88"), response.getBalanceAfter());
    }

    // 5. Invalid zero amount
    @Test
    @DisplayName("5. Zero Amount Rejection: Throws IllegalArgumentException when amount is zero")
    void testInvalidZeroAmount() {
        DepositRequest request = new DepositRequest(BigDecimal.ZERO, "IDEMP-DEP-005");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> depositService.deposit(301L, 101L, request));
        assertTrue(ex.getMessage().contains("strictly positive"));
    }

    // 6. Negative amount
    @Test
    @DisplayName("6. Negative Amount Rejection: Throws IllegalArgumentException when amount is negative")
    void testNegativeAmount() {
        DepositRequest request = new DepositRequest(new BigDecimal("-100.00"), "IDEMP-DEP-006");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> depositService.deposit(301L, 101L, request));
        assertTrue(ex.getMessage().contains("strictly positive"));
    }

    // 7. Excess decimal precision
    @Test
    @DisplayName("7. Scale Enforcement: Throws IllegalArgumentException when decimal scale exceeds 2")
    void testExcessDecimalPrecision() {
        DepositRequest request = new DepositRequest(new BigDecimal("100.123"), "IDEMP-DEP-007");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> depositService.deposit(301L, 101L, request));
        assertTrue(ex.getMessage().contains("2 decimal places"));
    }

    // 8. Nonexistent account
    @Test
    @DisplayName("8. Nonexistent Account: Throws BankAccountNotFoundException when account ID is not found")
    void testNonexistentAccount() {
        DepositRequest request = new DepositRequest(new BigDecimal("100.00"), "IDEMP-DEP-008");

        when(userRepository.findById(101L)).thenReturn(Optional.of(ownerUser));
        when(bankAccountRepository.findByIdForUpdate(999L)).thenReturn(Optional.empty());

        assertThrows(BankAccountNotFoundException.class,
                () -> depositService.deposit(999L, 101L, request));
    }

    // 9. Unauthorized customer
    @Test
    @DisplayName("9. Ownership Enforcement: Throws UnauthorizedAccountAccessException when caller is not account owner")
    void testUnauthorizedCustomer() {
        User otherUser = new User("Bob Smith", "bob@finova.bank", "hashPassword", UserRole.CUSTOMER);
        otherUser.setId(999L);

        DepositRequest request = new DepositRequest(new BigDecimal("100.00"), "IDEMP-DEP-009");

        when(userRepository.findById(999L)).thenReturn(Optional.of(otherUser));
        when(bankAccountRepository.findByIdForUpdate(301L)).thenReturn(Optional.of(activeAccount));

        UnauthorizedAccountAccessException ex = assertThrows(UnauthorizedAccountAccessException.class,
                () -> depositService.deposit(301L, 999L, request));
        assertTrue(ex.getMessage().contains("not authorized"));
    }

    // 10. Frozen account rejection
    @Test
    @DisplayName("10. Frozen Account Rejection: Throws InactiveAccountException when account status is FROZEN")
    void testFrozenAccountRejection() {
        activeAccount.setAccountStatus(AccountStatus.FROZEN);
        DepositRequest request = new DepositRequest(new BigDecimal("100.00"), "IDEMP-DEP-010");

        when(userRepository.findById(101L)).thenReturn(Optional.of(ownerUser));
        when(bankAccountRepository.findByIdForUpdate(301L)).thenReturn(Optional.of(activeAccount));

        InactiveAccountException ex = assertThrows(InactiveAccountException.class,
                () -> depositService.deposit(301L, 101L, request));
        assertTrue(ex.getMessage().contains("frozen"));
    }

    // 11. Closed account rejection
    @Test
    @DisplayName("11. Closed Account Rejection: Throws InactiveAccountException when account status is CLOSED")
    void testClosedAccountRejection() {
        activeAccount.setAccountStatus(AccountStatus.CLOSED);
        DepositRequest request = new DepositRequest(new BigDecimal("100.00"), "IDEMP-DEP-011");

        when(userRepository.findById(101L)).thenReturn(Optional.of(ownerUser));
        when(bankAccountRepository.findByIdForUpdate(301L)).thenReturn(Optional.of(activeAccount));

        InactiveAccountException ex = assertThrows(InactiveAccountException.class,
                () -> depositService.deposit(301L, 101L, request));
        assertTrue(ex.getMessage().contains("closed"));
    }

    // 12. Repeated identical idempotency request
    @Test
    @DisplayName("12. Idempotency Replay: Returns previous completed transaction without mutating account balance")
    void testRepeatedIdenticalIdempotencyRequest() {
        DepositRequest request = new DepositRequest(new BigDecimal("200.00"), "IDEMP-DEP-012");

        BankTransaction existingTx = new BankTransaction();
        existingTx.setId(55L);
        existingTx.setTransactionReference("FIN-TXN-PREV-01");
        existingTx.setAccount(activeAccount);
        existingTx.setTransactionType(TransactionType.DEPOSIT);
        existingTx.setTransactionStatus(TransactionStatus.COMPLETED);
        existingTx.setAmount(new BigDecimal("200.00"));
        existingTx.setBalanceBefore(new BigDecimal("1000.00"));
        existingTx.setBalanceAfter(new BigDecimal("1200.00"));
        existingTx.setIdempotencyKey("IDEMP-DEP-012");
        existingTx.setCreatedAt(LocalDateTime.now());

        when(userRepository.findById(101L)).thenReturn(Optional.of(ownerUser));
        when(bankAccountRepository.findByIdForUpdate(301L)).thenReturn(Optional.of(activeAccount));
        when(bankTransactionRepository.findByIdempotencyKey("IDEMP-DEP-012")).thenReturn(Optional.of(existingTx));

        TransactionResponse response = depositService.deposit(301L, 101L, request);

        assertNotNull(response);
        assertEquals("FIN-TXN-PREV-01", response.getTransactionReference());
        assertEquals(new BigDecimal("200.00"), response.getAmount());

        // Verify account balance was NOT mutated
        assertEquals(new BigDecimal("1000.00"), activeAccount.getBalance());
        verify(bankAccountRepository, never()).save(any());
        verify(bankTransactionRepository, never()).save(any());
    }

    // 13. Same idempotency key with different amount rejected
    @Test
    @DisplayName("13. Idempotency Conflict: Throws IdempotencyConflictException when key reused with different amount")
    void testSameIdempotencyKeyWithDifferentAmountRejected() {
        DepositRequest request = new DepositRequest(new BigDecimal("300.00"), "IDEMP-DEP-013");

        BankTransaction existingTx = new BankTransaction();
        existingTx.setAccount(activeAccount);
        existingTx.setTransactionType(TransactionType.DEPOSIT);
        existingTx.setAmount(new BigDecimal("200.00")); // Different amount
        existingTx.setIdempotencyKey("IDEMP-DEP-013");

        when(userRepository.findById(101L)).thenReturn(Optional.of(ownerUser));
        when(bankAccountRepository.findByIdForUpdate(301L)).thenReturn(Optional.of(activeAccount));
        when(bankTransactionRepository.findByIdempotencyKey("IDEMP-DEP-013")).thenReturn(Optional.of(existingTx));

        assertThrows(IdempotencyConflictException.class,
                () -> depositService.deposit(301L, 101L, request));
        verify(bankAccountRepository, never()).save(any());
    }

    // 14. Transaction persistence failure rolls back balance update
    @Test
    @DisplayName("14. Persistence Failure: Throws exception when transaction repository save fails")
    void testTransactionPersistenceFailureRollsBackBalance() {
        DepositRequest request = new DepositRequest(new BigDecimal("100.00"), "IDEMP-DEP-014");

        when(userRepository.findById(101L)).thenReturn(Optional.of(ownerUser));
        when(bankAccountRepository.findByIdForUpdate(301L)).thenReturn(Optional.of(activeAccount));
        when(bankTransactionRepository.findByIdempotencyKey("IDEMP-DEP-014")).thenReturn(Optional.empty());
        when(transactionReferenceGenerator.generateReference()).thenReturn("FIN-TXN-DEP-014");
        when(bankTransactionRepository.save(any(BankTransaction.class)))
                .thenThrow(new DataIntegrityViolationException("Database constraint failure"));

        assertThrows(Exception.class,
                () -> depositService.deposit(301L, 101L, request));
    }
}

