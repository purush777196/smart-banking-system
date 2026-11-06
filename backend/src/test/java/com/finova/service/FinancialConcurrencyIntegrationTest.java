package com.finova.service;

import com.finova.dto.transaction.DepositRequest;
import com.finova.dto.transaction.TransactionResponse;
import com.finova.dto.transaction.WithdrawalRequest;
import com.finova.entity.BankAccount;
import com.finova.entity.BankTransaction;
import com.finova.entity.Customer;
import com.finova.entity.User;
import com.finova.entity.UserRole;
import com.finova.entity.enums.AccountStatus;
import com.finova.entity.enums.AccountType;
import com.finova.entity.enums.TransactionType;
import com.finova.exception.InsufficientBalanceException;
import com.finova.repository.BankAccountRepository;
import com.finova.repository.BankTransactionRepository;
import com.finova.repository.CustomerRepository;
import com.finova.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class FinancialConcurrencyIntegrationTest {

    @Autowired
    private DepositService depositService;

    @Autowired
    private WithdrawalService withdrawalService;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private BankTransactionRepository bankTransactionRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private Customer testCustomer;
    private BankAccount testAccount;

    @BeforeEach
    void setUp() {
        cleanDatabase();

        testUser = new User("Concurrency User", "concurrency.test@finova.bank", "hashSecure", UserRole.CUSTOMER);
        testUser = userRepository.save(testUser);

        testCustomer = new Customer();
        testCustomer.setCustomerCode("CUST-CONC-01");
        testCustomer.setUser(testUser);
        testCustomer.setDateOfBirth(LocalDate.of(1992, 4, 15));
        testCustomer.setPhoneNumber("+1-555-444-3333");
        testCustomer.setAddress("100 Financial Way");
        testCustomer.setCity("Mumbai");
        testCustomer.setState("Maharashtra");
        testCustomer.setPostalCode("400001");
        testCustomer.setCountry("India");
        testCustomer = customerRepository.save(testCustomer);

        testAccount = new BankAccount("FINCONC000001", testCustomer, AccountType.SAVINGS);
        testAccount.setBalance(new BigDecimal("100.00").setScale(2, RoundingMode.HALF_UP));
        testAccount.setAccountStatus(AccountStatus.ACTIVE);
        testAccount.setCurrency("INR");
        testAccount = bankAccountRepository.save(testAccount);
    }

    @AfterEach
    void tearDown() {
        cleanDatabase();
    }

    private void cleanDatabase() {
        userRepository.findByEmail("concurrency.test@finova.bank").ifPresent(user -> {
            customerRepository.findByUserId(user.getId()).ifPresent(cust -> {
                List<BankAccount> accounts = bankAccountRepository.findByCustomerId(cust.getId());
                for (BankAccount acc : accounts) {
                    bankTransactionRepository.deleteAll(bankTransactionRepository.findByAccountId(acc.getId()));
                }
                bankAccountRepository.deleteAll(accounts);
                customerRepository.delete(cust);
            });
            userRepository.delete(user);
        });
    }

    // 1. Two concurrent withdrawals against one account cannot overdraw it
    @Test
    @DisplayName("1. Concurrency: Two concurrent withdrawals cannot overdraw account balance")
    void testConcurrentWithdrawalsCannotOverdrawAccount() throws Exception {
        // Initial balance = 100.00. Two threads try to withdraw 80.00 each simultaneously.
        // Total attempted = 160.00. Exactly ONE must succeed and ONE must fail with InsufficientBalanceException.
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch readyLatch = new CountDownLatch(2);
        CountDownLatch startLatch = new CountDownLatch(1);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger insufficientCount = new AtomicInteger(0);
        List<Throwable> unexpectedErrors = Collections.synchronizedList(new ArrayList<>());

        Runnable task1 = () -> {
            readyLatch.countDown();
            try {
                startLatch.await();
                withdrawalService.withdraw(
                        testAccount.getId(),
                        testUser.getId(),
                        new WithdrawalRequest(new BigDecimal("80.00"), "Thread 1", "CONC-WTH-KEY-A")
                );
                successCount.incrementAndGet();
            } catch (InsufficientBalanceException ex) {
                insufficientCount.incrementAndGet();
            } catch (Throwable t) {
                unexpectedErrors.add(t);
            }
        };

        Runnable task2 = () -> {
            readyLatch.countDown();
            try {
                startLatch.await();
                withdrawalService.withdraw(
                        testAccount.getId(),
                        testUser.getId(),
                        new WithdrawalRequest(new BigDecimal("80.00"), "Thread 2", "CONC-WTH-KEY-B")
                );
                successCount.incrementAndGet();
            } catch (InsufficientBalanceException ex) {
                insufficientCount.incrementAndGet();
            } catch (Throwable t) {
                unexpectedErrors.add(t);
            }
        };

        Future<?> f1 = executor.submit(task1);
        Future<?> f2 = executor.submit(task2);

        readyLatch.await(5, TimeUnit.SECONDS);
        startLatch.countDown();

        f1.get(10, TimeUnit.SECONDS);
        f2.get(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertTrue(unexpectedErrors.isEmpty(), "Unexpected errors during concurrent withdrawals: " + unexpectedErrors);
        assertEquals(1, successCount.get(), "Exactly one withdrawal must succeed");
        assertEquals(1, insufficientCount.get(), "Exactly one withdrawal must be rejected due to insufficient funds");

        // Verify account balance in database is exactly 20.00 (100.00 - 80.00)
        BankAccount refreshedAccount = bankAccountRepository.findById(testAccount.getId()).orElseThrow();
        assertEquals(new BigDecimal("20.00"), refreshedAccount.getBalance(), "Final balance must reflect exactly one withdrawal");

        // Verify exactly one transaction persisted
        List<BankTransaction> transactions = bankTransactionRepository.findByAccountId(testAccount.getId());
        assertEquals(1, transactions.size(), "Exactly one withdrawal transaction must be recorded");
        assertEquals(new BigDecimal("80.00"), transactions.get(0).getAmount());
    }

    // 2. Two concurrent deposits preserve both balance increases
    @Test
    @DisplayName("2. Concurrency: Two concurrent deposits serialize and preserve both balance increases")
    void testConcurrentDepositsPreserveBothBalanceIncreases() throws Exception {
        // Initial balance = 100.00. Concurrent deposits of 50.00 and 75.00 -> final balance must be 225.00.
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch readyLatch = new CountDownLatch(2);
        CountDownLatch startLatch = new CountDownLatch(1);

        AtomicInteger successCount = new AtomicInteger(0);
        List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());

        Runnable task1 = () -> {
            readyLatch.countDown();
            try {
                startLatch.await();
                depositService.deposit(
                        testAccount.getId(),
                        testUser.getId(),
                        new DepositRequest(new BigDecimal("50.00"), "Deposit A", "CONC-DEP-KEY-A")
                );
                successCount.incrementAndGet();
            } catch (Throwable t) {
                errors.add(t);
            }
        };

        Runnable task2 = () -> {
            readyLatch.countDown();
            try {
                startLatch.await();
                depositService.deposit(
                        testAccount.getId(),
                        testUser.getId(),
                        new DepositRequest(new BigDecimal("75.00"), "Deposit B", "CONC-DEP-KEY-B")
                );
                successCount.incrementAndGet();
            } catch (Throwable t) {
                errors.add(t);
            }
        };

        Future<?> f1 = executor.submit(task1);
        Future<?> f2 = executor.submit(task2);

        readyLatch.await(5, TimeUnit.SECONDS);
        startLatch.countDown();

        f1.get(10, TimeUnit.SECONDS);
        f2.get(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertTrue(errors.isEmpty(), "Unexpected errors during concurrent deposits: " + errors);
        assertEquals(2, successCount.get(), "Both deposits must succeed");

        // Verify final balance is 225.00 (100.00 + 50.00 + 75.00)
        BankAccount refreshedAccount = bankAccountRepository.findById(testAccount.getId()).orElseThrow();
        assertEquals(new BigDecimal("225.00"), refreshedAccount.getBalance(), "Both deposits must be atomically applied without lost updates");

        // Verify exactly two transaction records persisted
        List<BankTransaction> transactions = bankTransactionRepository.findByAccountId(testAccount.getId());
        assertEquals(2, transactions.size());
    }

    // 3. Concurrent identical idempotency keys cannot create duplicate transactions
    @Test
    @DisplayName("3. Idempotency Concurrency: Concurrent requests with identical idempotency key do not mutate balance twice")
    void testConcurrentIdenticalIdempotencyKeys() throws Exception {
        // Initial balance = 100.00. Two threads submit identical deposit of 50.00 with SAME idempotency key.
        // Balance must become 150.00, NOT 200.00.
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch readyLatch = new CountDownLatch(2);
        CountDownLatch startLatch = new CountDownLatch(1);

        List<TransactionResponse> responses = Collections.synchronizedList(new ArrayList<>());
        List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < 2; i++) {
            executor.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();
                    TransactionResponse res = depositService.deposit(
                            testAccount.getId(),
                            testUser.getId(),
                            new DepositRequest(new BigDecimal("50.00"), "Identical retry", "CONC-SHARED-IDEMP-001")
                    );
                    responses.add(res);
                } catch (Throwable t) {
                    errors.add(t);
                }
            });
        }

        readyLatch.await(5, TimeUnit.SECONDS);
        startLatch.countDown();

        executor.shutdown();
        assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));

        // Verify final balance is 150.00, NOT 200.00
        BankAccount refreshedAccount = bankAccountRepository.findById(testAccount.getId()).orElseThrow();
        assertEquals(new BigDecimal("150.00"), refreshedAccount.getBalance(), "Idempotent operation must only credit balance once");

        // Verify exactly 1 transaction was persisted for this idempotency key
        List<BankTransaction> transactions = bankTransactionRepository.findByAccountId(testAccount.getId());
        assertEquals(1, transactions.size(), "Only one ledger record must exist for the idempotency key");
        assertEquals("CONC-SHARED-IDEMP-001", transactions.get(0).getIdempotencyKey());
    }

    // 4. Transaction balance snapshots remain consistent
    @Test
    @DisplayName("4. Ledger Snapshot Consistency: Balance before and after snapshots strictly match operations")
    void testTransactionBalanceSnapshotsRemainConsistent() {
        // Initial: 100.00
        // Op 1: Deposit 50.00 -> before 100.00, after 150.00
        TransactionResponse dep = depositService.deposit(
                testAccount.getId(),
                testUser.getId(),
                new DepositRequest(new BigDecimal("50.00"), "Step 1", "SNAP-DEP-001")
        );
        assertEquals(new BigDecimal("100.00"), dep.getBalanceBefore());
        assertEquals(new BigDecimal("150.00"), dep.getBalanceAfter());

        // Op 2: Withdraw 30.00 -> before 150.00, after 120.00
        TransactionResponse wth = withdrawalService.withdraw(
                testAccount.getId(),
                testUser.getId(),
                new WithdrawalRequest(new BigDecimal("30.00"), "Step 2", "SNAP-WTH-001")
        );
        assertEquals(new BigDecimal("150.00"), wth.getBalanceBefore());
        assertEquals(new BigDecimal("120.00"), wth.getBalanceAfter());

        // Final persisted account balance must match last transaction balanceAfter
        BankAccount refreshed = bankAccountRepository.findById(testAccount.getId()).orElseThrow();
        assertEquals(wth.getBalanceAfter(), refreshed.getBalance());
        assertEquals(new BigDecimal("120.00"), refreshed.getBalance());
    }
}

