package com.finova.service;

import com.finova.dto.account.AccountCreateRequest;
import com.finova.dto.account.AccountMapper;
import com.finova.dto.account.AccountResponse;
import com.finova.entity.BankAccount;
import com.finova.entity.Customer;
import com.finova.entity.User;
import com.finova.entity.UserRole;
import com.finova.entity.enums.AccountStatus;
import com.finova.exception.BankAccountNotFoundException;
import com.finova.exception.CustomerNotFoundException;
import com.finova.exception.InvalidAccountOperationException;
import com.finova.exception.UnauthorizedAccountAccessException;
import com.finova.exception.UserNotFoundException;
import com.finova.repository.BankAccountRepository;
import com.finova.repository.CustomerRepository;
import com.finova.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Service implementing business logic, referential integrity, and role-based authorization
 * for Bank Account management.
 */
@Service
public class AccountService {

    private static final Set<UserRole> AUTHORIZED_STAFF_ROLES = EnumSet.of(
            UserRole.SUPER_ADMIN,
            UserRole.BRANCH_MANAGER,
            UserRole.EMPLOYEE,
            UserRole.COMPLIANCE_OFFICER
    );

    private final BankAccountRepository bankAccountRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final AccountNumberGenerator accountNumberGenerator;
    private final AccountMapper accountMapper;

    public AccountService(BankAccountRepository bankAccountRepository,
                          CustomerRepository customerRepository,
                          UserRepository userRepository,
                          AccountNumberGenerator accountNumberGenerator,
                          AccountMapper accountMapper) {
        this.bankAccountRepository = bankAccountRepository;
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.accountNumberGenerator = accountNumberGenerator;
        this.accountMapper = accountMapper;
    }

    /**
     * Creates a new bank account for an authenticated user with role CUSTOMER.
     * Enforces server-side account number generation, zero balance initialization (0.00),
     * ACTIVE status, and links the account to the customer profile.
     *
     * @param request               account creation parameters
     * @param authenticatedUserId   id of authenticated principal
     * @return safe account response DTO
     */
    @Transactional
    public AccountResponse createAccount(AccountCreateRequest request, Long authenticatedUserId) {
        if (request == null || request.getAccountType() == null) {
            throw new IllegalArgumentException("Account type is required");
        }

        User user = userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + authenticatedUserId));

        if (user.getRole() != UserRole.CUSTOMER) {
            throw new IllegalArgumentException("Only users with role CUSTOMER can create a bank account");
        }

        Customer customer = customerRepository.findByUserId(authenticatedUserId)
                .orElseThrow(() -> new CustomerNotFoundException("No customer profile found for user ID: " + authenticatedUserId));

        String accountNumber = accountNumberGenerator.generateUniqueAccountNumber();
        BankAccount account = accountMapper.toEntity(request, customer, accountNumber);
        BankAccount savedAccount = bankAccountRepository.save(account);

        return accountMapper.toResponse(savedAccount);
    }

    /**
     * Retrieves a bank account by its primary key ID.
     * Accessible by the account owner or authorized banking staff.
     *
     * @param accountId             database primary key
     * @param authenticatedUserId   id of authenticated principal
     * @return safe account response DTO
     */
    @Transactional(readOnly = true)
    public AccountResponse getAccountById(Long accountId, Long authenticatedUserId) {
        BankAccount account = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new BankAccountNotFoundException("Bank account not found with ID: " + accountId));

        User authUser = userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new UserNotFoundException("Authenticated user not found with ID: " + authenticatedUserId));

        validateAccessPermission(account, authUser);

        return accountMapper.toResponse(account);
    }

    /**
     * Retrieves a bank account by its unique business account number.
     * Accessible by the account owner or authorized banking staff.
     *
     * @param accountNumber         business account number
     * @param authenticatedUserId   id of authenticated principal
     * @return safe account response DTO
     */
    @Transactional(readOnly = true)
    public AccountResponse getAccountByNumber(String accountNumber, Long authenticatedUserId) {
        if (accountNumber == null || accountNumber.isBlank()) {
            throw new IllegalArgumentException("Account number is required");
        }

        BankAccount account = bankAccountRepository.findByAccountNumber(accountNumber.trim())
                .orElseThrow(() -> new BankAccountNotFoundException("Bank account not found with account number: " + accountNumber));

        User authUser = userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new UserNotFoundException("Authenticated user not found with ID: " + authenticatedUserId));

        validateAccessPermission(account, authUser);

        return accountMapper.toResponse(account);
    }

    /**
     * Retrieves all bank accounts belonging to the authenticated customer.
     *
     * @param authenticatedUserId   id of authenticated customer
     * @return list of accounts owned by customer
     */
    @Transactional(readOnly = true)
    public List<AccountResponse> getMyAccounts(Long authenticatedUserId) {
        if (!userRepository.existsById(authenticatedUserId)) {
            throw new UserNotFoundException("Authenticated user not found with ID: " + authenticatedUserId);
        }

        Customer customer = customerRepository.findByUserId(authenticatedUserId)
                .orElseThrow(() -> new CustomerNotFoundException("No customer profile found for user ID: " + authenticatedUserId));

        List<BankAccount> accounts = bankAccountRepository.findByCustomerId(customer.getId());
        return accounts.stream()
                .map(accountMapper::toResponse)
                .toList();
    }

    /**
     * Lists bank accounts with database-level pagination.
     * Restricted to authorized staff members only.
     *
     * @param authenticatedUserId   id of authenticated staff user
     * @param pageable              pagination parameters
     * @return paginated account response DTOs
     */
    @Transactional(readOnly = true)
    public Page<AccountResponse> getAllAccounts(Long authenticatedUserId, Pageable pageable) {
        User authUser = userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new UserNotFoundException("Authenticated user not found with ID: " + authenticatedUserId));

        if (!isAuthorizedStaff(authUser.getRole())) {
            throw new UnauthorizedAccountAccessException(
                    "Access denied: Only authorized banking staff can list all bank accounts"
            );
        }

        Page<BankAccount> page = bankAccountRepository.findAll(pageable);
        return page.map(accountMapper::toResponse);
    }

    /**
     * Freezes an active or eligible bank account.
     * Restricted to authorized staff members only.
     * Reject attempts to freeze CLOSED accounts.
     *
     * @param accountId             database primary key of the account
     * @param authenticatedUserId   id of authenticated staff user
     * @return updated account response DTO
     */
    @Transactional
    public AccountResponse freezeAccount(Long accountId, Long authenticatedUserId) {
        User authUser = userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new UserNotFoundException("Authenticated user not found with ID: " + authenticatedUserId));

        if (!isAuthorizedStaff(authUser.getRole())) {
            throw new UnauthorizedAccountAccessException(
                    "Access denied: Only authorized banking staff can freeze bank accounts"
            );
        }

        BankAccount account = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new BankAccountNotFoundException("Bank account not found with ID: " + accountId));

        if (account.getAccountStatus() == AccountStatus.CLOSED) {
            throw new InvalidAccountOperationException("Cannot freeze a closed bank account");
        }

        account.setAccountStatus(AccountStatus.FROZEN);
        BankAccount updatedAccount = bankAccountRepository.save(account);

        return accountMapper.toResponse(updatedAccount);
    }

    /**
     * Helper to verify if the authenticated user is either the account owner or an authorized staff member.
     */
    private void validateAccessPermission(BankAccount account, User authUser) {
        boolean isOwner = account.getCustomer() != null
                && account.getCustomer().getUser() != null
                && account.getCustomer().getUser().getId().equals(authUser.getId());

        boolean isStaff = isAuthorizedStaff(authUser.getRole());

        if (!isOwner && !isStaff) {
            throw new UnauthorizedAccountAccessException(
                    "Access denied: You are not authorized to access this account"
            );
        }
    }

    /**
     * Checks if a role belongs to the authorized banking staff set.
     */
    public boolean isAuthorizedStaff(UserRole role) {
        return role != null && AUTHORIZED_STAFF_ROLES.contains(role);
    }
}

