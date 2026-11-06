package com.finova.service;

import com.finova.dto.transaction.TransactionHistoryResponse;
import com.finova.dto.transaction.TransactionMapper;
import com.finova.dto.transaction.TransactionResponse;
import com.finova.entity.BankAccount;
import com.finova.entity.BankTransaction;
import com.finova.entity.User;
import com.finova.entity.enums.TransactionStatus;
import com.finova.entity.enums.TransactionType;
import com.finova.exception.BankAccountNotFoundException;
import com.finova.exception.TransactionNotFoundException;
import com.finova.exception.UnauthorizedAccountAccessException;
import com.finova.exception.UserNotFoundException;
import com.finova.repository.BankAccountRepository;
import com.finova.repository.BankTransactionRepository;
import com.finova.repository.TransactionSpecification;
import com.finova.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Service for banking transaction history retrieval, reference lookup,
 * pagination, and multi-criteria filtering with customer and staff authorization.
 */
@Service
public class TransactionService {

    public static final Set<String> ALLOWED_SORT_PROPERTIES = Set.of(
            "id", "transactionReference", "amount", "currency",
            "balanceBefore", "balanceAfter", "transactionType", "transactionStatus", "createdAt"
    );

    private final BankTransactionRepository bankTransactionRepository;
    private final BankAccountRepository bankAccountRepository;
    private final UserRepository userRepository;
    private final AccountService accountService;
    private final TransactionMapper transactionMapper;

    public TransactionService(BankTransactionRepository bankTransactionRepository,
                              BankAccountRepository bankAccountRepository,
                              UserRepository userRepository,
                              AccountService accountService,
                              TransactionMapper transactionMapper) {
        this.bankTransactionRepository = bankTransactionRepository;
        this.bankAccountRepository = bankAccountRepository;
        this.userRepository = userRepository;
        this.accountService = accountService;
        this.transactionMapper = transactionMapper;
    }

    /**
     * Retrieves paginated transaction history for an account with dynamic filtering.
     * Enforces customer ownership and role-based staff authorization.
     *
     * @param accountId           account ID
     * @param authenticatedUserId caller user ID
     * @param page                page index (zero-based)
     * @param size                page size (1 to 100)
     * @param sortBy              allowed sort property
     * @param direction           sort direction ("asc" or "desc")
     * @param type                optional transaction type filter
     * @param status              optional transaction status filter
     * @param startDateTime       optional start date-time
     * @param endDateTime         optional end date-time
     * @return paginated transaction history response DTO
     */
    @Transactional(readOnly = true)
    public TransactionHistoryResponse getTransactionHistory(
            Long accountId,
            Long authenticatedUserId,
            int page,
            int size,
            String sortBy,
            String direction,
            TransactionType type,
            TransactionStatus status,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime
    ) {
        if (accountId == null) {
            throw new IllegalArgumentException("Account ID is required");
        }
        if (authenticatedUserId == null) {
            throw new IllegalArgumentException("Authenticated user ID is required");
        }

        User authUser = userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new UserNotFoundException("Authenticated user not found with ID: " + authenticatedUserId));

        BankAccount account = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new BankAccountNotFoundException("Bank account not found with ID: " + accountId));

        validateAccountAccess(account, authUser);
        validatePaginationAndSorting(page, size, sortBy, direction);
        validateDateRange(startDateTime, endDateTime);

        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        Specification<BankTransaction> spec = TransactionSpecification.filterTransactions(
                accountId, type, status, startDateTime, endDateTime
        );

        Page<BankTransaction> pageResult = bankTransactionRepository.findAll(spec, pageable);
        Page<TransactionResponse> mappedPage = pageResult.map(transactionMapper::toResponse);

        return TransactionHistoryResponse.from(mappedPage);
    }

    /**
     * Retrieves a single transaction by its unique reference string.
     * Accessible by the account owner or authorized staff.
     *
     * @param transactionReference unique transaction reference string
     * @param authenticatedUserId  caller user ID
     * @return safe transaction response DTO
     */
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionByReference(String transactionReference, Long authenticatedUserId) {
        if (transactionReference == null || transactionReference.isBlank()) {
            throw new IllegalArgumentException("Transaction reference is required");
        }
        if (authenticatedUserId == null) {
            throw new IllegalArgumentException("Authenticated user ID is required");
        }

        User authUser = userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new UserNotFoundException("Authenticated user not found with ID: " + authenticatedUserId));

        BankTransaction transaction = bankTransactionRepository.findByTransactionReference(transactionReference.trim())
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found with reference: " + transactionReference));

        boolean isOwner = transaction.getAccount() != null
                && transaction.getAccount().getCustomer() != null
                && transaction.getAccount().getCustomer().getUser() != null
                && transaction.getAccount().getCustomer().getUser().getId().equals(authUser.getId());

        boolean isStaff = accountService.isAuthorizedStaff(authUser.getRole());

        if (!isOwner && !isStaff) {
            throw new UnauthorizedAccountAccessException(
                    "Access denied: You are not authorized to view this transaction"
            );
        }

        return transactionMapper.toResponse(transaction);
    }

    private void validateAccountAccess(BankAccount account, User authUser) {
        boolean isOwner = account.getCustomer() != null
                && account.getCustomer().getUser() != null
                && account.getCustomer().getUser().getId().equals(authUser.getId());

        boolean isStaff = accountService.isAuthorizedStaff(authUser.getRole());

        if (!isOwner && !isStaff) {
            throw new UnauthorizedAccountAccessException(
                    "Access denied: You are not authorized to access transactions for this account"
            );
        }
    }

    private void validatePaginationAndSorting(int page, int size, String sortBy, String direction) {
        if (page < 0) {
            throw new IllegalArgumentException("Page index must not be less than zero");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("Page size must be between 1 and 100");
        }
        if (sortBy == null || !ALLOWED_SORT_PROPERTIES.contains(sortBy)) {
            throw new IllegalArgumentException("Invalid sort field: '" + sortBy + "'. Allowed fields: " + ALLOWED_SORT_PROPERTIES);
        }
        if (direction == null || (!direction.equalsIgnoreCase("asc") && !direction.equalsIgnoreCase("desc"))) {
            throw new IllegalArgumentException("Invalid sort direction: '" + direction + "'. Allowed values: asc, desc");
        }
    }

    private void validateDateRange(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        if (startDateTime != null && endDateTime != null && startDateTime.isAfter(endDateTime)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
    }
}

