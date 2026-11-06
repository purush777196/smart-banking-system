package com.finova.service;

import com.finova.dto.account.AccountCreateRequest;
import com.finova.dto.account.AccountMapper;
import com.finova.dto.account.AccountResponse;
import com.finova.entity.BankAccount;
import com.finova.entity.Customer;
import com.finova.entity.User;
import com.finova.entity.UserRole;
import com.finova.entity.enums.AccountStatus;
import com.finova.entity.enums.AccountType;
import com.finova.exception.AccountCreationException;
import com.finova.exception.BankAccountNotFoundException;
import com.finova.exception.CustomerNotFoundException;
import com.finova.exception.InvalidAccountOperationException;
import com.finova.exception.UnauthorizedAccountAccessException;
import com.finova.exception.UserNotFoundException;
import com.finova.repository.BankAccountRepository;
import com.finova.repository.CustomerRepository;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountNumberGenerator accountNumberGenerator;

    @Spy
    private AccountMapper accountMapper = new AccountMapper();

    @InjectMocks
    private AccountService accountService;

    private User customerUser;
    private User otherCustomerUser;
    private User adminUser;
    private User managerUser;
    private User employeeUser;
    private User complianceUser;

    private Customer customer;
    private Customer otherCustomer;

    private BankAccount testAccount;

    @BeforeEach
    void setUp() {
        customerUser = new User("Sophia Rodriguez", "customer@finova.bank", "hash123", UserRole.CUSTOMER);
        customerUser.setId(10L);

        otherCustomerUser = new User("David Miller", "david@example.com", "hash123", UserRole.CUSTOMER);
        otherCustomerUser.setId(11L);

        adminUser = new User("Eleanor Vance", "admin@finova.bank", "hash123", UserRole.SUPER_ADMIN);
        adminUser.setId(1L);

        managerUser = new User("Marcus Sterling", "manager@finova.bank", "hash123", UserRole.BRANCH_MANAGER);
        managerUser.setId(2L);

        employeeUser = new User("Daniel Wright", "employee@finova.bank", "hash123", UserRole.EMPLOYEE);
        employeeUser.setId(3L);

        complianceUser = new User("David Ross", "compliance@finova.bank", "hash123", UserRole.COMPLIANCE_OFFICER);
        complianceUser.setId(4L);

        customer = new Customer();
        customer.setId(100L);
        customer.setCustomerCode("FIN-CUS-ABC12345");
        customer.setUser(customerUser);
        customer.setDateOfBirth(LocalDate.of(1992, 5, 14));
        customer.setPhoneNumber("+1-212-555-0192");
        customer.setAddress("742 Evergreen Terrace");
        customer.setCity("New York");
        customer.setState("NY");
        customer.setPostalCode("10001");
        customer.setCountry("USA");

        otherCustomer = new Customer();
        otherCustomer.setId(101L);
        otherCustomer.setCustomerCode("FIN-CUS-XYZ98765");
        otherCustomer.setUser(otherCustomerUser);
        otherCustomer.setDateOfBirth(LocalDate.of(1988, 3, 20));
        otherCustomer.setPhoneNumber("+1-212-555-0199");
        otherCustomer.setAddress("123 Main Street");
        otherCustomer.setCity("Boston");
        otherCustomer.setState("MA");
        otherCustomer.setPostalCode("02101");
        otherCustomer.setCountry("USA");

        testAccount = new BankAccount("FIN1029384756", customer, AccountType.SAVINGS);
        testAccount.setId(500L);
        testAccount.setBalance(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        testAccount.setAccountStatus(AccountStatus.ACTIVE);
        testAccount.setCurrency("INR");
        testAccount.setBranchName("Finova Manhattan Branch");
        testAccount.setBranchCode("BR-MAN-01");
        testAccount.setIfscCode("FINO0001234");
        testAccount.setCreatedAt(LocalDateTime.now());
        testAccount.setUpdatedAt(LocalDateTime.now());
    }

    // 1. Successful account creation
    @Test
    @DisplayName("1. Successful account creation: Creates and persists bank account with server-generated number")
    void testCreateAccount_Success() {
        AccountCreateRequest request = new AccountCreateRequest(AccountType.SAVINGS, "Finova Manhattan Branch");
        when(userRepository.findById(10L)).thenReturn(Optional.of(customerUser));
        when(customerRepository.findByUserId(10L)).thenReturn(Optional.of(customer));
        when(accountNumberGenerator.generateUniqueAccountNumber()).thenReturn("FIN1029384756");
        when(bankAccountRepository.save(any(BankAccount.class))).thenAnswer(inv -> {
            BankAccount b = inv.getArgument(0);
            b.setId(500L);
            return b;
        });

        AccountResponse response = accountService.createAccount(request, 10L);

        assertNotNull(response);
        assertEquals(500L, response.getId());
        assertEquals("FIN1029384756", response.getAccountNumber());
        assertEquals(AccountType.SAVINGS, response.getAccountType());
        assertEquals(AccountStatus.ACTIVE, response.getAccountStatus());
        assertEquals(new BigDecimal("0.00"), response.getBalance());
        assertEquals("INR", response.getCurrency());
        assertEquals("Finova Manhattan Branch", response.getBranchName());
        verify(bankAccountRepository).save(any(BankAccount.class));
    }

    // 2. Account creation rejects missing user
    @Test
    @DisplayName("2. Account creation rejects missing user: Throws UserNotFoundException")
    void testCreateAccount_UserNotFound() {
        AccountCreateRequest request = new AccountCreateRequest(AccountType.SAVINGS);
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> accountService.createAccount(request, 999L));
        verify(bankAccountRepository, never()).save(any());
    }

    // 3. Account creation rejects non-CUSTOMER role
    @Test
    @DisplayName("3. Account creation rejects non-CUSTOMER role: Throws IllegalArgumentException")
    void testCreateAccount_NonCustomerRole() {
        AccountCreateRequest request = new AccountCreateRequest(AccountType.SAVINGS);
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> accountService.createAccount(request, 1L));
        assertTrue(ex.getMessage().contains("Only users with role CUSTOMER"));
        verify(bankAccountRepository, never()).save(any());
    }

    // 4. Account creation rejects missing customer profile
    @Test
    @DisplayName("4. Account creation rejects missing customer profile: Throws CustomerNotFoundException")
    void testCreateAccount_MissingCustomerProfile() {
        AccountCreateRequest request = new AccountCreateRequest(AccountType.SAVINGS);
        when(userRepository.findById(10L)).thenReturn(Optional.of(customerUser));
        when(customerRepository.findByUserId(10L)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () -> accountService.createAccount(request, 10L));
        verify(bankAccountRepository, never()).save(any());
    }

    // 5. Account starts with zero balance
    @Test
    @DisplayName("5. Account starts with zero balance: Balance is strictly initialized to BigDecimal.ZERO (0.00)")
    void testCreateAccount_StartsZeroBalance() {
        AccountCreateRequest request = new AccountCreateRequest(AccountType.CURRENT);
        when(userRepository.findById(10L)).thenReturn(Optional.of(customerUser));
        when(customerRepository.findByUserId(10L)).thenReturn(Optional.of(customer));
        when(accountNumberGenerator.generateUniqueAccountNumber()).thenReturn("FIN9999999999");
        when(bankAccountRepository.save(any(BankAccount.class))).thenAnswer(inv -> inv.getArgument(0));

        AccountResponse response = accountService.createAccount(request, 10L);

        assertEquals(0, BigDecimal.ZERO.compareTo(response.getBalance()));
        assertEquals(new BigDecimal("0.00"), response.getBalance());
    }

    // 6. Account starts ACTIVE
    @Test
    @DisplayName("6. Account starts ACTIVE: Initial account status is strictly ACTIVE")
    void testCreateAccount_StartsActive() {
        AccountCreateRequest request = new AccountCreateRequest(AccountType.SAVINGS);
        when(userRepository.findById(10L)).thenReturn(Optional.of(customerUser));
        when(customerRepository.findByUserId(10L)).thenReturn(Optional.of(customer));
        when(accountNumberGenerator.generateUniqueAccountNumber()).thenReturn("FIN1111111111");
        when(bankAccountRepository.save(any(BankAccount.class))).thenAnswer(inv -> inv.getArgument(0));

        AccountResponse response = accountService.createAccount(request, 10L);

        assertEquals(AccountStatus.ACTIVE, response.getAccountStatus());
    }

    // 7. Account type is stored correctly
    @Test
    @DisplayName("7. Account type is stored correctly: SAVINGS, CURRENT, FIXED_DEPOSIT are stored as requested")
    void testCreateAccount_AccountTypeStoredCorrectly() {
        when(userRepository.findById(10L)).thenReturn(Optional.of(customerUser));
        when(customerRepository.findByUserId(10L)).thenReturn(Optional.of(customer));
        when(accountNumberGenerator.generateUniqueAccountNumber()).thenReturn("FIN2222222222");
        when(bankAccountRepository.save(any(BankAccount.class))).thenAnswer(inv -> inv.getArgument(0));

        for (AccountType type : AccountType.values()) {
            AccountCreateRequest request = new AccountCreateRequest(type);
            AccountResponse response = accountService.createAccount(request, 10L);
            assertEquals(type, response.getAccountType(), "Account type must match requested type " + type);
        }
    }

    // 8. Customer retrieves own account
    @Test
    @DisplayName("8. Customer retrieves own account: Allowed by account ID and account number")
    void testCustomerRetrievesOwnAccount() {
        when(bankAccountRepository.findById(500L)).thenReturn(Optional.of(testAccount));
        when(userRepository.findById(10L)).thenReturn(Optional.of(customerUser));

        AccountResponse responseById = accountService.getAccountById(500L, 10L);
        assertNotNull(responseById);
        assertEquals("FIN1029384756", responseById.getAccountNumber());

        when(bankAccountRepository.findByAccountNumber("FIN1029384756")).thenReturn(Optional.of(testAccount));
        AccountResponse responseByNumber = accountService.getAccountByNumber("FIN1029384756", 10L);
        assertNotNull(responseByNumber);
        assertEquals(500L, responseByNumber.getId());
    }

    // 9. Customer cannot retrieve another customer's account
    @Test
    @DisplayName("9. Customer cannot retrieve another customer's account: Throws UnauthorizedAccountAccessException")
    void testCustomerCannotRetrieveAnotherAccount() {
        when(bankAccountRepository.findById(500L)).thenReturn(Optional.of(testAccount));
        when(userRepository.findById(11L)).thenReturn(Optional.of(otherCustomerUser));

        assertThrows(UnauthorizedAccountAccessException.class,
                () -> accountService.getAccountById(500L, 11L));

        when(bankAccountRepository.findByAccountNumber("FIN1029384756")).thenReturn(Optional.of(testAccount));
        assertThrows(UnauthorizedAccountAccessException.class,
                () -> accountService.getAccountByNumber("FIN1029384756", 11L));
    }

    // 10. Authorized staff retrieves permitted account
    @Test
    @DisplayName("10. Authorized staff retrieves permitted account: SUPER_ADMIN, BRANCH_MANAGER, EMPLOYEE, COMPLIANCE_OFFICER")
    void testAuthorizedStaffRetrievesPermittedAccount() {
        when(bankAccountRepository.findById(500L)).thenReturn(Optional.of(testAccount));

        // SUPER_ADMIN
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        assertNotNull(accountService.getAccountById(500L, 1L));

        // BRANCH_MANAGER
        when(userRepository.findById(2L)).thenReturn(Optional.of(managerUser));
        assertNotNull(accountService.getAccountById(500L, 2L));

        // EMPLOYEE
        when(userRepository.findById(3L)).thenReturn(Optional.of(employeeUser));
        assertNotNull(accountService.getAccountById(500L, 3L));

        // COMPLIANCE_OFFICER
        when(userRepository.findById(4L)).thenReturn(Optional.of(complianceUser));
        assertNotNull(accountService.getAccountById(500L, 4L));
    }

    // 11. Missing account returns not-found exception
    @Test
    @DisplayName("11. Missing account returns not-found exception: Throws BankAccountNotFoundException")
    void testMissingAccountThrowsNotFound() {
        when(bankAccountRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(BankAccountNotFoundException.class, () -> accountService.getAccountById(999L, 10L));

        when(bankAccountRepository.findByAccountNumber("NON_EXISTENT")).thenReturn(Optional.empty());
        assertThrows(BankAccountNotFoundException.class, () -> accountService.getAccountByNumber("NON_EXISTENT", 10L));
    }

    // 12. Customer retrieves own account list
    @Test
    @DisplayName("12. Customer retrieves own account list: Returns accounts belonging to customer, never another's")
    void testCustomerRetrievesOwnAccountList() {
        when(userRepository.existsById(10L)).thenReturn(true);
        when(customerRepository.findByUserId(10L)).thenReturn(Optional.of(customer));
        when(bankAccountRepository.findByCustomerId(100L)).thenReturn(List.of(testAccount));

        List<AccountResponse> accounts = accountService.getMyAccounts(10L);

        assertNotNull(accounts);
        assertEquals(1, accounts.size());
        assertEquals("FIN1029384756", accounts.get(0).getAccountNumber());
    }

    // 13. Staff account listing supports pagination
    @Test
    @DisplayName("13. Staff account listing supports pagination: Returns database-paginated AccountResponses")
    void testStaffAccountListingSupportsPagination() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<BankAccount> page = new PageImpl<>(List.of(testAccount), pageable, 1);

        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(bankAccountRepository.findAll(pageable)).thenReturn(page);

        Page<AccountResponse> result = accountService.getAllAccounts(1L, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("FIN1029384756", result.getContent().get(0).getAccountNumber());
        verify(bankAccountRepository).findAll(pageable);
    }

    // 14. Customer cannot list all accounts
    @Test
    @DisplayName("14. Customer cannot list all accounts: Throws UnauthorizedAccountAccessException")
    void testCustomerCannotListAllAccounts() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findById(10L)).thenReturn(Optional.of(customerUser));

        assertThrows(UnauthorizedAccountAccessException.class,
                () -> accountService.getAllAccounts(10L, pageable));
        verify(bankAccountRepository, never()).findAll(pageable);
    }

    // 15. Staff can freeze an ACTIVE account
    @Test
    @DisplayName("15. Staff can freeze an ACTIVE account: Transitions status to FROZEN")
    void testStaffCanFreezeActiveAccount() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(bankAccountRepository.findById(500L)).thenReturn(Optional.of(testAccount));
        when(bankAccountRepository.save(any(BankAccount.class))).thenAnswer(inv -> inv.getArgument(0));

        AccountResponse response = accountService.freezeAccount(500L, 1L);

        assertNotNull(response);
        assertEquals(AccountStatus.FROZEN, response.getAccountStatus());
        verify(bankAccountRepository).save(testAccount);
    }

    // 16. Customer cannot freeze an account
    @Test
    @DisplayName("16. Customer cannot freeze an account: Throws UnauthorizedAccountAccessException")
    void testCustomerCannotFreezeAccount() {
        when(userRepository.findById(10L)).thenReturn(Optional.of(customerUser));

        assertThrows(UnauthorizedAccountAccessException.class,
                () -> accountService.freezeAccount(500L, 10L));
        verify(bankAccountRepository, never()).save(any());
    }

    // 17. CLOSED account cannot be frozen
    @Test
    @DisplayName("17. CLOSED account cannot be frozen: Throws InvalidAccountOperationException")
    void testClosedAccountCannotBeFrozen() {
        testAccount.setAccountStatus(AccountStatus.CLOSED);

        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(bankAccountRepository.findById(500L)).thenReturn(Optional.of(testAccount));

        InvalidAccountOperationException ex = assertThrows(InvalidAccountOperationException.class,
                () -> accountService.freezeAccount(500L, 1L));
        assertTrue(ex.getMessage().contains("Cannot freeze a closed bank account"));
        verify(bankAccountRepository, never()).save(any());
    }

    // 18. Account number collisions are handled correctly
    @Test
    @DisplayName("18. Account number collisions are handled correctly: Propagates AccountCreationException if generator exhausts retries")
    void testAccountNumberCollisionsHandledCorrectly() {
        AccountCreateRequest request = new AccountCreateRequest(AccountType.SAVINGS);
        when(userRepository.findById(10L)).thenReturn(Optional.of(customerUser));
        when(customerRepository.findByUserId(10L)).thenReturn(Optional.of(customer));
        when(accountNumberGenerator.generateUniqueAccountNumber())
                .thenThrow(new AccountCreationException("Failed to generate a unique account number after 5 attempts"));

        assertThrows(AccountCreationException.class,
                () -> accountService.createAccount(request, 10L));
        verify(bankAccountRepository, never()).save(any());
    }
}

