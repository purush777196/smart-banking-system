package com.finova.repository;

import com.finova.entity.BankAccount;
import com.finova.entity.BankTransaction;
import com.finova.entity.Customer;
import com.finova.entity.User;
import com.finova.entity.UserRole;
import com.finova.entity.enums.AccountStatus;
import com.finova.entity.enums.AccountType;
import com.finova.entity.enums.TransactionStatus;
import com.finova.entity.enums.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class BankTransactionRepositoryTest {

    @Autowired
    private BankTransactionRepository bankTransactionRepository;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private UserRepository userRepository;

    private BankAccount primaryAccount;
    private BankAccount secondaryAccount;

    @BeforeEach
    void setUp() {
        User user = new User("Ledger User", "ledger.test@finova.bank", "hash123", UserRole.CUSTOMER);
        userRepository.save(user);

        Customer customer = new Customer();
        customer.setCustomerCode("CUST-TX-REPO-01");
        customer.setUser(user);
        customer.setDateOfBirth(LocalDate.of(1989, 8, 12));
        customer.setPhoneNumber("+1-555-888-9999");
        customer.setAddress("100 Wall Street");
        customer.setCity("New York");
        customer.setState("NY");
        customer.setPostalCode("10005");
        customer.setCountry("USA");
        customerRepository.save(customer);

        primaryAccount = new BankAccount("FIN1000000001", customer, AccountType.SAVINGS);
        primaryAccount.setBalance(new BigDecimal("10000.00"));
        primaryAccount.setAccountStatus(AccountStatus.ACTIVE);
        bankAccountRepository.save(primaryAccount);

        secondaryAccount = new BankAccount("FIN2000000002", customer, AccountType.CURRENT);
        secondaryAccount.setBalance(new BigDecimal("25000.00"));
        secondaryAccount.setAccountStatus(AccountStatus.ACTIVE);
        bankAccountRepository.save(secondaryAccount);
    }

    private BankTransaction buildTransaction(String ref,
                                             BankAccount account,
                                             TransactionType type,
                                             TransactionStatus status,
                                             BigDecimal amount,
                                             BigDecimal before,
                                             BigDecimal after,
                                             String groupRef) {
        BankTransaction tx = new BankTransaction();
        tx.setTransactionReference(ref);
        tx.setAccount(account);
        tx.setTransactionType(type);
        tx.setTransactionStatus(status);
        tx.setAmount(amount);
        tx.setCurrency("INR");
        tx.setBalanceBefore(before);
        tx.setBalanceAfter(after);
        tx.setDescription("Test ledger entry");
        tx.setTransferGroupReference(groupRef);
        return tx;
    }

    // 1. Successful transaction persistence
    @Test
    @DisplayName("1. Persistence: Saves bank transaction and generates ID and timestamp")
    void testSaveBankTransaction() {
        BankTransaction tx = buildTransaction(
                "FIN-TXN-0000000000000001",
                primaryAccount,
                TransactionType.DEPOSIT,
                TransactionStatus.COMPLETED,
                new BigDecimal("500.00"),
                new BigDecimal("10000.00"),
                new BigDecimal("10500.00"),
                null
        );

        BankTransaction saved = bankTransactionRepository.save(tx);

        assertNotNull(saved.getId(), "Primary key must be generated");
        assertNotNull(saved.getCreatedAt(), "createdAt must be populated by @PrePersist");
        assertEquals("FIN-TXN-0000000000000001", saved.getTransactionReference());
        assertEquals(TransactionType.DEPOSIT, saved.getTransactionType());
        assertEquals(TransactionStatus.COMPLETED, saved.getTransactionStatus());
        assertEquals(new BigDecimal("500.00"), saved.getAmount());
    }

    // 2. Retrieval by transaction reference
    @Test
    @DisplayName("2. Reference Lookup: Finds transaction by unique transaction reference")
    void testFindByTransactionReference() {
        BankTransaction tx = buildTransaction(
                "FIN-TXN-REF-LOOKUP-01",
                primaryAccount,
                TransactionType.WITHDRAWAL,
                TransactionStatus.COMPLETED,
                new BigDecimal("200.00"),
                new BigDecimal("10000.00"),
                new BigDecimal("9800.00"),
                null
        );
        bankTransactionRepository.save(tx);

        Optional<BankTransaction> found = bankTransactionRepository.findByTransactionReference("FIN-TXN-REF-LOOKUP-01");
        assertTrue(found.isPresent());
        assertEquals(new BigDecimal("200.00"), found.get().getAmount());
        assertEquals(primaryAccount.getId(), found.get().getAccount().getId());

        assertTrue(bankTransactionRepository.existsByTransactionReference("FIN-TXN-REF-LOOKUP-01"));
        assertFalse(bankTransactionRepository.existsByTransactionReference("NON_EXISTENT_REF"));
    }

    // 3. Unique transaction reference enforcement
    @Test
    @DisplayName("3. Uniqueness Enforcement: Duplicate transaction reference throws DataIntegrityViolationException")
    void testUniqueTransactionReferenceEnforcement() {
        BankTransaction tx1 = buildTransaction(
                "FIN-TXN-DUPLICATE-01",
                primaryAccount,
                TransactionType.DEPOSIT,
                TransactionStatus.COMPLETED,
                new BigDecimal("100.00"),
                new BigDecimal("1000.00"),
                new BigDecimal("1100.00"),
                null
        );
        bankTransactionRepository.saveAndFlush(tx1);

        BankTransaction tx2 = buildTransaction(
                "FIN-TXN-DUPLICATE-01",
                secondaryAccount,
                TransactionType.WITHDRAWAL,
                TransactionStatus.COMPLETED,
                new BigDecimal("50.00"),
                new BigDecimal("500.00"),
                new BigDecimal("450.00"),
                null
        );

        assertThrows(DataIntegrityViolationException.class, () -> {
            bankTransactionRepository.saveAndFlush(tx2);
        });
    }

    // 4. Retrieval by account ID
    @Test
    @DisplayName("4. Account Lookup: Retrieves all transactions belonging to an account")
    void testFindByAccountId() {
        BankTransaction tx1 = buildTransaction("FIN-TXN-ACC1-01", primaryAccount, TransactionType.DEPOSIT, TransactionStatus.COMPLETED, new BigDecimal("100.00"), new BigDecimal("1000.00"), new BigDecimal("1100.00"), null);
        BankTransaction tx2 = buildTransaction("FIN-TXN-ACC1-02", primaryAccount, TransactionType.WITHDRAWAL, TransactionStatus.COMPLETED, new BigDecimal("50.00"), new BigDecimal("1100.00"), new BigDecimal("1050.00"), null);
        BankTransaction tx3 = buildTransaction("FIN-TXN-ACC2-01", secondaryAccount, TransactionType.DEPOSIT, TransactionStatus.COMPLETED, new BigDecimal("500.00"), new BigDecimal("2000.00"), new BigDecimal("2500.00"), null);

        bankTransactionRepository.save(tx1);
        bankTransactionRepository.save(tx2);
        bankTransactionRepository.save(tx3);

        List<BankTransaction> primaryTxList = bankTransactionRepository.findByAccountId(primaryAccount.getId());
        assertEquals(2, primaryTxList.size());

        List<BankTransaction> secondaryTxList = bankTransactionRepository.findByAccountId(secondaryAccount.getId());
        assertEquals(1, secondaryTxList.size());
    }

    // 5. Retrieval by account and transaction status
    @Test
    @DisplayName("5. Status Filter: Retrieves transactions by account ID and status")
    void testFindByAccountIdAndStatus() {
        BankTransaction completedTx = buildTransaction("FIN-TXN-ST-01", primaryAccount, TransactionType.DEPOSIT, TransactionStatus.COMPLETED, new BigDecimal("100.00"), new BigDecimal("1000.00"), new BigDecimal("1100.00"), null);
        BankTransaction pendingTx = buildTransaction("FIN-TXN-ST-02", primaryAccount, TransactionType.DEPOSIT, TransactionStatus.PENDING, new BigDecimal("200.00"), new BigDecimal("1100.00"), new BigDecimal("1100.00"), null);
        BankTransaction failedTx = buildTransaction("FIN-TXN-ST-03", primaryAccount, TransactionType.WITHDRAWAL, TransactionStatus.FAILED, new BigDecimal("50000.00"), new BigDecimal("1100.00"), new BigDecimal("1100.00"), null);

        bankTransactionRepository.save(completedTx);
        bankTransactionRepository.save(pendingTx);
        bankTransactionRepository.save(failedTx);

        List<BankTransaction> completedList = bankTransactionRepository.findByAccountIdAndTransactionStatus(primaryAccount.getId(), TransactionStatus.COMPLETED);
        assertEquals(1, completedList.size());
        assertEquals("FIN-TXN-ST-01", completedList.get(0).getTransactionReference());

        List<BankTransaction> pendingList = bankTransactionRepository.findByAccountIdAndTransactionStatus(primaryAccount.getId(), TransactionStatus.PENDING);
        assertEquals(1, pendingList.size());
        assertEquals("FIN-TXN-ST-02", pendingList.get(0).getTransactionReference());

        List<BankTransaction> reversedList = bankTransactionRepository.findByAccountIdAndTransactionStatus(primaryAccount.getId(), TransactionStatus.REVERSED);
        assertTrue(reversedList.isEmpty());
    }

    // 6. Retrieval by account and transaction type
    @Test
    @DisplayName("6. Type Filter: Retrieves transactions by account ID and transaction type")
    void testFindByAccountIdAndType() {
        BankTransaction depositTx = buildTransaction("FIN-TXN-TYP-01", primaryAccount, TransactionType.DEPOSIT, TransactionStatus.COMPLETED, new BigDecimal("100.00"), new BigDecimal("1000.00"), new BigDecimal("1100.00"), null);
        BankTransaction withdrawTx = buildTransaction("FIN-TXN-TYP-02", primaryAccount, TransactionType.WITHDRAWAL, TransactionStatus.COMPLETED, new BigDecimal("50.00"), new BigDecimal("1100.00"), new BigDecimal("1050.00"), null);
        BankTransaction debitTx = buildTransaction("FIN-TXN-TYP-03", primaryAccount, TransactionType.TRANSFER_DEBIT, TransactionStatus.COMPLETED, new BigDecimal("75.00"), new BigDecimal("1050.00"), new BigDecimal("975.00"), "GRP-01");

        bankTransactionRepository.save(depositTx);
        bankTransactionRepository.save(withdrawTx);
        bankTransactionRepository.save(debitTx);

        List<BankTransaction> deposits = bankTransactionRepository.findByAccountIdAndTransactionType(primaryAccount.getId(), TransactionType.DEPOSIT);
        assertEquals(1, deposits.size());

        List<BankTransaction> withdrawals = bankTransactionRepository.findByAccountIdAndTransactionType(primaryAccount.getId(), TransactionType.WITHDRAWAL);
        assertEquals(1, withdrawals.size());

        List<BankTransaction> transferCredits = bankTransactionRepository.findByAccountIdAndTransactionType(primaryAccount.getId(), TransactionType.TRANSFER_CREDIT);
        assertTrue(transferCredits.isEmpty());
    }

    // 7. Transfer group reference lookup
    @Test
    @DisplayName("7. Group Reference: Retrieves paired double-entry records by transfer group reference")
    void testFindByTransferGroupReference() {
        String transferGroup = "GRP-TRANSFER-20261106-0001";

        BankTransaction debit = buildTransaction(
                "FIN-TXN-TR-DEBIT-01",
                primaryAccount,
                TransactionType.TRANSFER_DEBIT,
                TransactionStatus.COMPLETED,
                new BigDecimal("1000.00"),
                new BigDecimal("10000.00"),
                new BigDecimal("9000.00"),
                transferGroup
        );

        BankTransaction credit = buildTransaction(
                "FIN-TXN-TR-CREDIT-01",
                secondaryAccount,
                TransactionType.TRANSFER_CREDIT,
                TransactionStatus.COMPLETED,
                new BigDecimal("1000.00"),
                new BigDecimal("25000.00"),
                new BigDecimal("26000.00"),
                transferGroup
        );

        bankTransactionRepository.save(debit);
        bankTransactionRepository.save(credit);

        List<BankTransaction> groupRecords = bankTransactionRepository.findByTransferGroupReference(transferGroup);
        assertEquals(2, groupRecords.size());
        assertTrue(groupRecords.stream().anyMatch(t -> t.getTransactionType() == TransactionType.TRANSFER_DEBIT));
        assertTrue(groupRecords.stream().anyMatch(t -> t.getTransactionType() == TransactionType.TRANSFER_CREDIT));
    }

    // 8. Pagination and sorting
    @Test
    @DisplayName("8. Pagination: Paginates and sorts account transactions at database level")
    void testPaginationAndSorting() {
        for (int i = 1; i <= 5; i++) {
            BankTransaction tx = buildTransaction(
                    String.format("FIN-TXN-PAGE-%04d", i),
                    primaryAccount,
                    TransactionType.DEPOSIT,
                    TransactionStatus.COMPLETED,
                    new BigDecimal(i * 100),
                    new BigDecimal("1000.00"),
                    new BigDecimal(1000 + (i * 100)),
                    null
            );
            bankTransactionRepository.save(tx);
        }

        Page<BankTransaction> firstPage = bankTransactionRepository.findByAccountId(
                primaryAccount.getId(),
                PageRequest.of(0, 2, Sort.by(Sort.Direction.ASC, "transactionReference"))
        );

        assertEquals(5, firstPage.getTotalElements());
        assertEquals(3, firstPage.getTotalPages());
        assertEquals(2, firstPage.getContent().size());
        assertEquals("FIN-TXN-PAGE-0001", firstPage.getContent().get(0).getTransactionReference());
        assertEquals("FIN-TXN-PAGE-0002", firstPage.getContent().get(1).getTransactionReference());
    }

    // 9. Foreign-key relationship enforcement
    @Test
    @DisplayName("9. FK Relationship: Links correctly to BankAccount entity")
    void testForeignKeyRelationship() {
        BankTransaction tx = buildTransaction(
                "FIN-TXN-FK-01",
                primaryAccount,
                TransactionType.DEPOSIT,
                TransactionStatus.COMPLETED,
                new BigDecimal("300.00"),
                new BigDecimal("10000.00"),
                new BigDecimal("10300.00"),
                null
        );

        BankTransaction saved = bankTransactionRepository.save(tx);
        assertNotNull(saved.getAccount());
        assertEquals(primaryAccount.getId(), saved.getAccount().getId());
        assertEquals(primaryAccount.getAccountNumber(), saved.getAccount().getAccountNumber());
    }

    // 10. Account deletion protection when transaction history exists
    @Test
    @DisplayName("10. Ledger Immutability: Deleting an account with transactions is rejected by foreign-key constraint")
    void testAccountDeletionProtectionWhenTransactionsExist() {
        BankTransaction tx = buildTransaction(
                "FIN-TXN-PROT-01",
                primaryAccount,
                TransactionType.DEPOSIT,
                TransactionStatus.COMPLETED,
                new BigDecimal("100.00"),
                new BigDecimal("10000.00"),
                new BigDecimal("10100.00"),
                null
        );
        bankTransactionRepository.saveAndFlush(tx);

        assertThrows(DataAccessException.class, () -> {
            bankAccountRepository.delete(primaryAccount);
            bankAccountRepository.flush();
        }, "Deleting a BankAccount with existing BankTransaction records must be rejected by foreign-key constraint or persistence lifecycle");
    }
}

