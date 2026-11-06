package com.finova.service;

import com.finova.dto.transaction.TransactionMapper;
import com.finova.dto.transaction.TransactionResponse;
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
import com.finova.exception.BankAccountNotFoundException;
import com.finova.exception.IdempotencyConflictException;
import com.finova.exception.InactiveAccountException;
import com.finova.exception.InsufficientBalanceException;
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
public class WithdrawalServiceTest {

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
    private WithdrawalService withdrawalService;

    private User ownerUser;
    private Customer ownerCustomer;
    private BankAccount activeAccount;

    @BeforeEach
    void setUp() {
        ownerUser = new User("Bob Doe", "bob@finova.bank", "hashPassword", UserRole.CUSTOMER);
        ownerUser.setId(102L);

        ownerCustomer = new Customer();
        ownerCustomer.setId(202L);
        ownerCustomer.setUser(ownerUser);

        activeAccount = new BankAccount("FIN1000000002", ownerCustomer, AccountType.CURRENT);
        activeAccount.setId(302L);
        activeAccount.setBalance(new BigDecimal("1000.00").setScale(2, RoundingMode.HALF_UP));
        activeAccount.setAccountStatus(AccountStatus.ACTIVE);
        activeAccount.setCurrency("INR");
    }

    // 1. Successful withdrawal
    @Test
    @DisplayName("1. Successful Withdrawal: Atomically debits amount and returns completed transaction response")
    void testSuccessfulWithdrawal() {
        WithdrawalRequest request = new WithdrawalRequest(new BigDecimal("400.00"), "ATM withdrawal", "IDEMP-WTH-001");

        when(userRepository.findById(102L)).thenReturn(Optional.of(ownerUser));
        when(bankAccountRepository.findByIdForUpdate(302L)).thenReturn(Optional.of(activeAccount));
        when(bankTransactionRepository.findByIdempotencyKey("IDEMP-WTH-001")).thenReturn(Optional.empty());
        when(transactionReferenceGenerator.generateReference()).thenReturn("FIN-TXN-WTH-001");
        when(bankTransactionRepository.save(any(BankTransaction.class))).thenAnswer(invocation -> {
            BankTransaction tx = invocation.getArgument(0);
            tx.setId(2L);
            tx.setCreatedAt(LocalDateTime.now());
            return tx;
        });

        TransactionResponse response = withdrawalService.withdraw(302L, 102L, request);

        assertNotNull(response);
        assertEquals(TransactionType.WITHDRAWAL, response.getTransactionType());
        assertEquals(TransactionStatus.COMPLETED, response.getTransactionStatus());
        assertEquals(new BigDecimal("400.00"), response.getAmount());
        assertEquals(new BigDecimal("1000.00"), response.getBalanceBefore());
        assertEquals(new BigDecimal("600.00"), response.getBalanceAfter());
        assertEquals("FIN1000000002", response.getAccountNumber());
        assertEquals("FIN-TXN-WTH-001", response.getTransactionReference());

        // Verify account balance updated
        assertEquals(new BigDecimal("600.00"), activeAccount.getBalance());
        verify(bankAccountRepository).save(activeAccount);
        verify(bankTransactionRepository).save(any(BankTransaction.class));
    }

    // 2. Correct balance calculation
    @Test
    @DisplayName("2. Balance Calculation: Strictly computes new balance using BigDecimal subtraction")
    void testCorrectBalanceCalculation() {
        WithdrawalRequest request = new WithdrawalRequest(new BigDecimal("0.50"), "Micro debit", "IDEMP-WTH-002");

        when(userRepository.findById(102L)).thenReturn(Optional.of(ownerUser));
        when(bankAccountRepository.findByIdForUpdate(302L)).thenReturn(Optional.of(activeAccount));
        when(bankTransactionRepository.findByIdempotencyKey("IDEMP-WTH-002")).thenReturn(Optional.empty());
        when(transactionReferenceGenerator.generateReference()).thenReturn("FIN-TXN-WTH-002");
        when(bankTransactionRepository.save(any(BankTransaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TransactionResponse response = withdrawalService.withdraw(302L, 102L, request);

        assertEquals(new BigDecimal("999.50"), response.getBalanceAfter());
        assertEquals(new BigDecimal("999.50"), activeAccount.getBalance());
    }

    // 3. Correct transaction type
    @Test
    @DisplayName("3. Transaction Type: Verifies ledger entry has TransactionType.WITHDRAWAL")
    void testCorrectTransactionType() {
        WithdrawalRequest request = new WithdrawalRequest(new BigDecimal("150.00"), "IDEMP-WTH-003");

        when(userRepository.findById(102L)).thenReturn(Optional.of(ownerUser));
        when(bankAccountRepository.findByIdForUpdate(302L)).thenReturn(Optional.of(activeAccount));
        when(bankTransactionRepository.findByIdempotencyKey("IDEMP-WTH-003")).thenReturn(Optional.empty());
        when(transactionReferenceGenerator.generateReference()).thenReturn("FIN-TXN-WTH-003");

        ArgumentCaptor<BankTransaction> txCaptor = ArgumentCaptor.forClass(BankTransaction.class);
        when(bankTransactionRepository.save(txCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        withdrawalService.withdraw(302L, 102L, request);

        BankTransaction captured = txCaptor.getValue();
        assertEquals(TransactionType.WITHDRAWAL, captured.getTransactionType());
    }

    // 4. Insufficient balance rejected
    @Test
    @DisplayName("4. Insufficient Balance: Throws InsufficientBalanceException when withdrawal exceeds available funds")
    void testInsufficientBalanceRejected() {
        WithdrawalRequest request = new WithdrawalRequest(new BigDecimal("1000.01"), "Overdraft attempt", "IDEMP-WTH-004");

        when(userRepository.findById(102L)).thenReturn(Optional.of(ownerUser));
        when(bankAccountRepository.findByIdForUpdate(302L)).thenReturn(Optional.of(activeAccount));
        when(bankTransactionRepository.findByIdempotencyKey("IDEMP-WTH-004")).thenReturn(Optional.empty());

        InsufficientBalanceException ex = assertThrows(InsufficientBalanceException.class,
                () -> withdrawalService.withdraw(302L, 102L, request));
        assertTrue(ex.getMessage().contains("Insufficient funds"));
        verify(bankAccountRepository, never()).save(any());
        verify(bankTransactionRepository, never()).save(any());
    }

    // 5. Exact-balance withdrawal succeeds
    @Test
    @DisplayName("5. Exact Balance: Successfully withdraws entire balance reducing account balance to zero")
    void testExactBalanceWithdrawalSucceeds() {
        WithdrawalRequest request = new WithdrawalRequest(new BigDecimal("1000.00"), "Full withdrawal", "IDEMP-WTH-005");

        when(userRepository.findById(102L)).thenReturn(Optional.of(ownerUser));
        when(bankAccountRepository.findByIdForUpdate(302L)).thenReturn(Optional.of(activeAccount));
        when(bankTransactionRepository.findByIdempotencyKey("IDEMP-WTH-005")).thenReturn(Optional.empty());
        when(transactionReferenceGenerator.generateReference()).thenReturn("FIN-TXN-WTH-005");
        when(bankTransactionRepository.save(any(BankTransaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TransactionResponse response = withdrawalService.withdraw(302L, 102L, request);

        assertEquals(new BigDecimal("0.00"), response.getBalanceAfter());
        assertEquals(new BigDecimal("0.00"), activeAccount.getBalance());
    }

    // 6. Invalid zero amount
    @Test
    @DisplayName("6. Zero Amount Rejection: Throws IllegalArgumentException when amount is zero")
    void testInvalidZeroAmount() {
        WithdrawalRequest request = new WithdrawalRequest(BigDecimal.ZERO, "IDEMP-WTH-006");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> withdrawalService.withdraw(302L, 102L, request));
        assertTrue(ex.getMessage().contains("strictly positive"));
    }

    // 7. Negative amount
    @Test
    @DisplayName("7. Negative Amount Rejection: Throws IllegalArgumentException when amount is negative")
    void testNegativeAmount() {
        WithdrawalRequest request = new WithdrawalRequest(new BigDecimal("-50.00"), "IDEMP-WTH-007");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> withdrawalService.withdraw(302L, 102L, request));
        assertTrue(ex.getMessage().contains("strictly positive"));
    }

    // 8. Excess decimal precision
    @Test
    @DisplayName("8. Scale Enforcement: Throws IllegalArgumentException when decimal scale exceeds 2")
    void testExcessDecimalPrecision() {
        WithdrawalRequest request = new WithdrawalRequest(new BigDecimal("10.999"), "IDEMP-WTH-008");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> withdrawalService.withdraw(302L, 102L, request));
        assertTrue(ex.getMessage().contains("2 decimal places"));
    }

    // 9. Nonexistent account
    @Test
    @DisplayName("9. Nonexistent Account: Throws BankAccountNotFoundException when account ID is not found")
    void testNonexistentAccount() {
        WithdrawalRequest request = new WithdrawalRequest(new BigDecimal("50.00"), "IDEMP-WTH-009");

        when(userRepository.findById(102L)).thenReturn(Optional.of(ownerUser));
        when(bankAccountRepository.findByIdForUpdate(999L)).thenReturn(Optional.empty());

        assertThrows(BankAccountNotFoundException.class,
                () -> withdrawalService.withdraw(999L, 102L, request));
    }

    // 10. Unauthorized customer
    @Test
    @DisplayName("10. Ownership Enforcement: Throws UnauthorizedAccountAccessException when caller is not account owner")
    void testUnauthorizedCustomer() {
        User otherUser = new User("Mallory", "mallory@finova.bank", "hashPassword", UserRole.CUSTOMER);
        otherUser.setId(999L);

        WithdrawalRequest request = new WithdrawalRequest(new BigDecimal("50.00"), "IDEMP-WTH-010");

        when(userRepository.findById(999L)).thenReturn(Optional.of(otherUser));
        when(bankAccountRepository.findByIdForUpdate(302L)).thenReturn(Optional.of(activeAccount));

        UnauthorizedAccountAccessException ex = assertThrows(UnauthorizedAccountAccessException.class,
                () -> withdrawalService.withdraw(302L, 999L, request));
        assertTrue(ex.getMessage().contains("not authorized"));
    }

    // 11. Frozen account rejection
    @Test
    @DisplayName("11. Frozen Account Rejection: Throws InactiveAccountException when account status is FROZEN")
    void testFrozenAccountRejection() {
        activeAccount.setAccountStatus(AccountStatus.FROZEN);
        WithdrawalRequest request = new WithdrawalRequest(new BigDecimal("50.00"), "IDEMP-WTH-011");

        when(userRepository.findById(102L)).thenReturn(Optional.of(ownerUser));
        when(bankAccountRepository.findByIdForUpdate(302L)).thenReturn(Optional.of(activeAccount));

        InactiveAccountException ex = assertThrows(InactiveAccountException.class,
                () -> withdrawalService.withdraw(302L, 102L, request));
        assertTrue(ex.getMessage().contains("frozen"));
    }

    // 12. Closed account rejection
    @Test
    @DisplayName("12. Closed Account Rejection: Throws InactiveAccountException when account status is CLOSED")
    void testClosedAccountRejection() {
        activeAccount.setAccountStatus(AccountStatus.CLOSED);
        WithdrawalRequest request = new WithdrawalRequest(new BigDecimal("50.00"), "IDEMP-WTH-012");

        when(userRepository.findById(102L)).thenReturn(Optional.of(ownerUser));
        when(bankAccountRepository.findByIdForUpdate(302L)).thenReturn(Optional.of(activeAccount));

        InactiveAccountException ex = assertThrows(InactiveAccountException.class,
                () -> withdrawalService.withdraw(302L, 102L, request));
        assertTrue(ex.getMessage().contains("closed"));
    }

    // 13. Repeated identical idempotency request
    @Test
    @DisplayName("13. Idempotency Replay: Returns previous completed transaction without mutating account balance")
    void testRepeatedIdenticalIdempotencyRequest() {
        WithdrawalRequest request = new WithdrawalRequest(new BigDecimal("100.00"), "IDEMP-WTH-013");

        BankTransaction existingTx = new BankTransaction();
        existingTx.setId(77L);
        existingTx.setTransactionReference("FIN-TXN-WTH-PREV-01");
        existingTx.setAccount(activeAccount);
        existingTx.setTransactionType(TransactionType.WITHDRAWAL);
        existingTx.setTransactionStatus(TransactionStatus.COMPLETED);
        existingTx.setAmount(new BigDecimal("100.00"));
        existingTx.setBalanceBefore(new BigDecimal("1000.00"));
        existingTx.setBalanceAfter(new BigDecimal("900.00"));
        existingTx.setIdempotencyKey("IDEMP-WTH-013");
        existingTx.setCreatedAt(LocalDateTime.now());

        when(userRepository.findById(102L)).thenReturn(Optional.of(ownerUser));
        when(bankAccountRepository.findByIdForUpdate(302L)).thenReturn(Optional.of(activeAccount));
        when(bankTransactionRepository.findByIdempotencyKey("IDEMP-WTH-013")).thenReturn(Optional.of(existingTx));

        TransactionResponse response = withdrawalService.withdraw(302L, 102L, request);

        assertNotNull(response);
        assertEquals("FIN-TXN-WTH-PREV-01", response.getTransactionReference());
        assertEquals(new BigDecimal("100.00"), response.getAmount());

        // Verify account balance was NOT mutated
        assertEquals(new BigDecimal("1000.00"), activeAccount.getBalance());
        verify(bankAccountRepository, never()).save(any());
        verify(bankTransactionRepository, never()).save(any());
    }

    // 14. Same idempotency key with different amount rejected
    @Test
    @DisplayName("14. Idempotency Conflict: Throws IdempotencyConflictException when key reused with different amount")
    void testSameIdempotencyKeyWithDifferentAmountRejected() {
        WithdrawalRequest request = new WithdrawalRequest(new BigDecimal("150.00"), "IDEMP-WTH-014");

        BankTransaction existingTx = new BankTransaction();
        existingTx.setAccount(activeAccount);
        existingTx.setTransactionType(TransactionType.WITHDRAWAL);
        existingTx.setAmount(new BigDecimal("100.00")); // Different amount
        existingTx.setIdempotencyKey("IDEMP-WTH-014");

        when(userRepository.findById(102L)).thenReturn(Optional.of(ownerUser));
        when(bankAccountRepository.findByIdForUpdate(302L)).thenReturn(Optional.of(activeAccount));
        when(bankTransactionRepository.findByIdempotencyKey("IDEMP-WTH-014")).thenReturn(Optional.of(existingTx));

        assertThrows(IdempotencyConflictException.class,
                () -> withdrawalService.withdraw(302L, 102L, request));
        verify(bankAccountRepository, never()).save(any());
    }

    // 15. Transaction persistence failure rolls back balance update
    @Test
    @DisplayName("15. Persistence Failure: Throws exception when transaction repository save fails")
    void testTransactionPersistenceFailureRollsBackBalance() {
        WithdrawalRequest request = new WithdrawalRequest(new BigDecimal("100.00"), "IDEMP-WTH-015");

        when(userRepository.findById(102L)).thenReturn(Optional.of(ownerUser));
        when(bankAccountRepository.findByIdForUpdate(302L)).thenReturn(Optional.of(activeAccount));
        when(bankTransactionRepository.findByIdempotencyKey("IDEMP-WTH-015")).thenReturn(Optional.empty());
        when(transactionReferenceGenerator.generateReference()).thenReturn("FIN-TXN-WTH-015");
        when(bankTransactionRepository.save(any(BankTransaction.class)))
                .thenThrow(new DataIntegrityViolationException("Database constraint failure"));

        assertThrows(Exception.class,
                () -> withdrawalService.withdraw(302L, 102L, request));
    }
}

