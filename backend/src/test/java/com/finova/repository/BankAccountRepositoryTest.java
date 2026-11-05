package com.finova.repository;

import com.finova.entity.BankAccount;
import com.finova.entity.Customer;
import com.finova.entity.Gender;
import com.finova.entity.KycStatus;
import com.finova.entity.User;
import com.finova.entity.UserRole;
import com.finova.entity.enums.AccountStatus;
import com.finova.entity.enums.AccountType;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
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
public class BankAccountRepositoryTest {

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    private Customer primaryCustomer;
    private Customer secondaryCustomer;

    @BeforeEach
    void setUp() {
        User user1 = new User("Robert Langdon", "robert.langdon@finova.bank", "passwordHash", UserRole.CUSTOMER);
        userRepository.save(user1);

        primaryCustomer = new Customer();
        primaryCustomer.setCustomerCode("CUST-ACC-TEST-01");
        primaryCustomer.setUser(user1);
        primaryCustomer.setDateOfBirth(LocalDate.of(1985, 4, 12));
        primaryCustomer.setPhoneNumber("+1-555-010-2030");
        primaryCustomer.setAddress("100 Harvard Yard");
        primaryCustomer.setCity("Cambridge");
        primaryCustomer.setState("MA");
        primaryCustomer.setPostalCode("02138");
        primaryCustomer.setCountry("USA");
        primaryCustomer.setKycStatus(KycStatus.VERIFIED);
        customerRepository.save(primaryCustomer);

        User user2 = new User("Sophie Neveu", "sophie.neveu@finova.bank", "passwordHash", UserRole.CUSTOMER);
        userRepository.save(user2);

        secondaryCustomer = new Customer();
        secondaryCustomer.setCustomerCode("CUST-ACC-TEST-02");
        secondaryCustomer.setUser(user2);
        secondaryCustomer.setDateOfBirth(LocalDate.of(1992, 9, 25));
        secondaryCustomer.setPhoneNumber("+1-555-010-4050");
        secondaryCustomer.setAddress("200 Louvre Boulevard");
        secondaryCustomer.setCity("Paris");
        secondaryCustomer.setState("FR");
        secondaryCustomer.setPostalCode("75001");
        secondaryCustomer.setCountry("France");
        secondaryCustomer.setKycStatus(KycStatus.PENDING);
        customerRepository.save(secondaryCustomer);
    }

    private BankAccount buildAccount(String accountNumber, Customer customer, AccountType type, BigDecimal balance) {
        BankAccount account = new BankAccount(accountNumber, customer, type);
        account.setBalance(balance);
        account.setCurrency("INR");
        account.setBranchName("Finova Cambridge Branch");
        account.setBranchCode("FIN-CAM-01");
        account.setIfscCode("FINO0009876");
        return account;
    }

    // 9. Customer-to-account relationship
    @Test
    @DisplayName("9. Customer Relationship: Account correctly links to its parent customer")
    void testCustomerToAccountRelationship() {
        BankAccount account = buildAccount("100010001001", primaryCustomer, AccountType.SAVINGS, new BigDecimal("5000.00"));
        BankAccount saved = bankAccountRepository.save(account);

        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt(), "createdAt must be populated by @PrePersist");
        assertNotNull(saved.getUpdatedAt(), "updatedAt must be populated by @PrePersist");
        assertEquals(primaryCustomer.getId(), saved.getCustomer().getId());
        assertEquals("CUST-ACC-TEST-01", saved.getCustomer().getCustomerCode());
    }

    // 10. Multiple accounts belonging to one customer
    @Test
    @DisplayName("10. Multiple Accounts: One customer can own multiple bank accounts")
    void testMultipleAccountsPerCustomer() {
        BankAccount savings = buildAccount("100010001002", primaryCustomer, AccountType.SAVINGS, new BigDecimal("10000.00"));
        BankAccount current = buildAccount("100010001003", primaryCustomer, AccountType.CURRENT, new BigDecimal("25000.00"));
        BankAccount fd = buildAccount("100010001004", primaryCustomer, AccountType.FIXED_DEPOSIT, new BigDecimal("50000.00"));

        bankAccountRepository.save(savings);
        bankAccountRepository.save(current);
        bankAccountRepository.save(fd);

        List<BankAccount> customerAccounts = bankAccountRepository.findByCustomerId(primaryCustomer.getId());
        assertEquals(3, customerAccounts.size(), "Customer must have exactly 3 associated accounts");
    }

    // 11. Unique account number enforcement
    @Test
    @DisplayName("11. Unique Account Number: Rejects duplicate account numbers with DataIntegrityViolationException")
    void testUniqueAccountNumberEnforcement() {
        BankAccount account1 = buildAccount("DUPLICATE-ACC-01", primaryCustomer, AccountType.SAVINGS, new BigDecimal("100.00"));
        bankAccountRepository.saveAndFlush(account1);

        BankAccount account2 = buildAccount("DUPLICATE-ACC-01", secondaryCustomer, AccountType.CURRENT, new BigDecimal("200.00"));
        assertThrows(DataIntegrityViolationException.class, () -> {
            bankAccountRepository.saveAndFlush(account2);
        });
    }

    // 15. Repository lookup by account number
    @Test
    @DisplayName("15. findByAccountNumber & existsByAccountNumber: Finds account by unique account number")
    void testLookupByAccountNumber() {
        BankAccount account = buildAccount("ACC-LOOKUP-9999", primaryCustomer, AccountType.SAVINGS, new BigDecimal("1500.00"));
        bankAccountRepository.save(account);

        Optional<BankAccount> found = bankAccountRepository.findByAccountNumber("ACC-LOOKUP-9999");
        assertTrue(found.isPresent());
        assertEquals("ACC-LOOKUP-9999", found.get().getAccountNumber());
        assertEquals(new BigDecimal("1500.00"), found.get().getBalance());

        assertTrue(bankAccountRepository.existsByAccountNumber("ACC-LOOKUP-9999"));
        assertFalse(bankAccountRepository.existsByAccountNumber("NON-EXISTENT-ACC"));
    }

    // 16. Repository lookup by customer ID
    @Test
    @DisplayName("16. findByCustomerId: Retrieves only accounts for specified customer")
    void testLookupByCustomerId() {
        BankAccount acc1 = buildAccount("ACC-CUST1-01", primaryCustomer, AccountType.SAVINGS, new BigDecimal("100.00"));
        BankAccount acc2 = buildAccount("ACC-CUST2-01", secondaryCustomer, AccountType.SAVINGS, new BigDecimal("200.00"));
        bankAccountRepository.save(acc1);
        bankAccountRepository.save(acc2);

        List<BankAccount> customer1Accounts = bankAccountRepository.findByCustomerId(primaryCustomer.getId());
        assertEquals(1, customer1Accounts.size());
        assertEquals("ACC-CUST1-01", customer1Accounts.get(0).getAccountNumber());

        List<BankAccount> customer2Accounts = bankAccountRepository.findByCustomerId(secondaryCustomer.getId());
        assertEquals(1, customer2Accounts.size());
        assertEquals("ACC-CUST2-01", customer2Accounts.get(0).getAccountNumber());
    }

    // 17. Repository filtering by account status
    @Test
    @DisplayName("17. findByCustomerIdAndAccountStatus: Filters accounts by lifecycle status")
    void testFilterByAccountStatus() {
        BankAccount activeAcc = buildAccount("ACC-STATUS-01", primaryCustomer, AccountType.SAVINGS, new BigDecimal("500.00"));
        activeAcc.setAccountStatus(AccountStatus.ACTIVE);

        BankAccount frozenAcc = buildAccount("ACC-STATUS-02", primaryCustomer, AccountType.CURRENT, new BigDecimal("1000.00"));
        frozenAcc.setAccountStatus(AccountStatus.FROZEN);

        BankAccount closedAcc = buildAccount("ACC-STATUS-03", primaryCustomer, AccountType.SAVINGS, new BigDecimal("0.00"));
        closedAcc.setAccountStatus(AccountStatus.CLOSED);

        bankAccountRepository.save(activeAcc);
        bankAccountRepository.save(frozenAcc);
        bankAccountRepository.save(closedAcc);

        List<BankAccount> activeList = bankAccountRepository.findByCustomerIdAndAccountStatus(primaryCustomer.getId(), AccountStatus.ACTIVE);
        assertEquals(1, activeList.size());
        assertEquals("ACC-STATUS-01", activeList.get(0).getAccountNumber());

        List<BankAccount> frozenList = bankAccountRepository.findByCustomerIdAndAccountStatus(primaryCustomer.getId(), AccountStatus.FROZEN);
        assertEquals(1, frozenList.size());
        assertEquals("ACC-STATUS-02", frozenList.get(0).getAccountNumber());

        List<BankAccount> closedList = bankAccountRepository.findByCustomerIdAndAccountStatus(primaryCustomer.getId(), AccountStatus.CLOSED);
        assertEquals(1, closedList.size());
        assertEquals("ACC-STATUS-03", closedList.get(0).getAccountNumber());
    }

    // 18. Repository filtering by account type
    @Test
    @DisplayName("18. findByCustomerIdAndAccountType: Filters accounts by product classification")
    void testFilterByAccountType() {
        BankAccount savings1 = buildAccount("ACC-TYPE-SAV-01", primaryCustomer, AccountType.SAVINGS, new BigDecimal("100.00"));
        BankAccount savings2 = buildAccount("ACC-TYPE-SAV-02", primaryCustomer, AccountType.SAVINGS, new BigDecimal("200.00"));
        BankAccount current = buildAccount("ACC-TYPE-CUR-01", primaryCustomer, AccountType.CURRENT, new BigDecimal("5000.00"));

        bankAccountRepository.save(savings1);
        bankAccountRepository.save(savings2);
        bankAccountRepository.save(current);

        List<BankAccount> savingsList = bankAccountRepository.findByCustomerIdAndAccountType(primaryCustomer.getId(), AccountType.SAVINGS);
        assertEquals(2, savingsList.size());

        List<BankAccount> currentList = bankAccountRepository.findByCustomerIdAndAccountType(primaryCustomer.getId(), AccountType.CURRENT);
        assertEquals(1, currentList.size());
        assertEquals("ACC-TYPE-CUR-01", currentList.get(0).getAccountNumber());

        List<BankAccount> fdList = bankAccountRepository.findByCustomerIdAndAccountType(primaryCustomer.getId(), AccountType.FIXED_DEPOSIT);
        assertTrue(fdList.isEmpty());
    }
}

