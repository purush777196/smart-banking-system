package com.finova.service;

import com.finova.dto.transaction.DepositRequest;
import com.finova.dto.transaction.TransactionMapper;
import com.finova.dto.transaction.TransactionResponse;
import com.finova.entity.BankAccount;
import com.finova.entity.BankTransaction;
import com.finova.entity.User;
import com.finova.entity.enums.AccountStatus;
import com.finova.entity.enums.TransactionStatus;
import com.finova.entity.enums.TransactionType;
import com.finova.exception.BankAccountNotFoundException;
import com.finova.exception.DuplicateTransactionException;
import com.finova.exception.IdempotencyConflictException;
import com.finova.exception.InactiveAccountException;
import com.finova.exception.UnauthorizedAccountAccessException;
import com.finova.exception.UserNotFoundException;
import com.finova.repository.BankAccountRepository;
import com.finova.repository.BankTransactionRepository;
import com.finova.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

/**
 * Service orchestrating atomic deposit operations, pessimistic account locking,
 * ledger integrity, and idempotency protection.
 */
@Service
public class DepositService {

    private static final BigDecimal MAX_SUPPORTED_AMOUNT = new BigDecimal("99999999999999999.99");

    private final BankAccountRepository bankAccountRepository;
    private final BankTransactionRepository bankTransactionRepository;
    private final UserRepository userRepository;
    private final TransactionReferenceGenerator transactionReferenceGenerator;
    private final TransactionMapper transactionMapper;

    public DepositService(BankAccountRepository bankAccountRepository,
                          BankTransactionRepository bankTransactionRepository,
                          UserRepository userRepository,
                          TransactionReferenceGenerator transactionReferenceGenerator,
                          TransactionMapper transactionMapper) {
        this.bankAccountRepository = bankAccountRepository;
        this.bankTransactionRepository = bankTransactionRepository;
        this.userRepository = userRepository;
        this.transactionReferenceGenerator = transactionReferenceGenerator;
        this.transactionMapper = transactionMapper;
    }

    /**
     * Executes an atomic, idempotent deposit into the specified bank account.
     * Concurrency is serialized using pessimistic write locking on the account row.
     *
     * @param accountId           account primary key
     * @param authenticatedUserId principal user primary key
     * @param request             validated deposit request parameters
     * @return safe transaction response DTO
     */
    @Transactional
    public TransactionResponse deposit(Long accountId, Long authenticatedUserId, DepositRequest request) {
        validateRequest(request);

        if (authenticatedUserId == null) {
            throw new IllegalArgumentException("Authenticated user ID is required");
        }

        User authUser = userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new UserNotFoundException("Authenticated user not found with ID: " + authenticatedUserId));

        if (accountId == null) {
            throw new IllegalArgumentException("Account ID is required");
        }

        // Acquire pessimistic write lock on the target bank account
        BankAccount account = bankAccountRepository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new BankAccountNotFoundException("Bank account not found with ID: " + accountId));

        // Verify account ownership
        validateOwnership(account, authUser);

        // Verify account status
        validateAccountActive(account);

        // Verify idempotency
        String idempotencyKey = request.getIdempotencyKey().trim();
        Optional<BankTransaction> existingOpt = bankTransactionRepository.findByIdempotencyKey(idempotencyKey);
        if (existingOpt.isPresent()) {
            BankTransaction existing = existingOpt.get();
            boolean sameAccount = existing.getAccount() != null && existing.getAccount().getId().equals(account.getId());
            boolean sameType = existing.getTransactionType() == TransactionType.DEPOSIT;
            boolean sameAmount = existing.getAmount().compareTo(request.getAmount()) == 0;

            if (sameAccount && sameType && sameAmount) {
                // Return existing completed transaction response without balance mutation
                return transactionMapper.toResponse(existing);
            }
            throw new IdempotencyConflictException(
                    "Idempotency conflict: Key '" + idempotencyKey + "' has already been used with different transaction parameters"
            );
        }

        // Calculate new balance using strict BigDecimal precision
        BigDecimal balanceBefore = account.getBalance();
        BigDecimal balanceAfter = balanceBefore.add(request.getAmount()).setScale(2, RoundingMode.HALF_UP);

        // Build immutable ledger transaction record
        String transactionReference = transactionReferenceGenerator.generateReference();
        BankTransaction transaction = new BankTransaction();
        transaction.setTransactionReference(transactionReference);
        transaction.setAccount(account);
        transaction.setTransactionType(TransactionType.DEPOSIT);
        transaction.setTransactionStatus(TransactionStatus.COMPLETED);
        transaction.setAmount(request.getAmount().setScale(2, RoundingMode.HALF_UP));
        transaction.setCurrency(account.getCurrency() != null ? account.getCurrency() : "INR");
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(balanceAfter);
        transaction.setDescription(request.getDescription());
        transaction.setIdempotencyKey(idempotencyKey);

        // Update account balance
        account.setBalance(balanceAfter);
        bankAccountRepository.save(account);

        try {
            BankTransaction savedTransaction = bankTransactionRepository.save(transaction);
            bankTransactionRepository.flush();
            return transactionMapper.toResponse(savedTransaction);
        } catch (DataIntegrityViolationException ex) {
            // Guard against concurrent race condition on database unique constraint
            Optional<BankTransaction> concurrentTx = bankTransactionRepository.findByIdempotencyKey(idempotencyKey);
            if (concurrentTx.isPresent()) {
                BankTransaction existing = concurrentTx.get();
                if (existing.getAccount() != null && existing.getAccount().getId().equals(account.getId())
                        && existing.getTransactionType() == TransactionType.DEPOSIT
                        && existing.getAmount().compareTo(request.getAmount()) == 0) {
                    return transactionMapper.toResponse(existing);
                }
                throw new IdempotencyConflictException("Concurrent idempotency conflict for key: " + idempotencyKey, ex);
            }
            throw new DuplicateTransactionException("Duplicate transaction detected for idempotency key or reference: " + idempotencyKey, ex);
        }
    }

    private void validateRequest(DepositRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Deposit request is required");
        }
        if (request.getAmount() == null) {
            throw new IllegalArgumentException("Deposit amount is required");
        }
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be strictly positive");
        }
        if (request.getAmount().scale() > 2) {
            throw new IllegalArgumentException("Deposit amount must not have more than 2 decimal places");
        }
        if (request.getAmount().compareTo(MAX_SUPPORTED_AMOUNT) > 0) {
            throw new IllegalArgumentException("Deposit amount exceeds maximum supported monetary precision");
        }
        if (request.getIdempotencyKey() == null || request.getIdempotencyKey().isBlank()) {
            throw new IllegalArgumentException("Idempotency key is required");
        }
        if (request.getIdempotencyKey().trim().length() > 128) {
            throw new IllegalArgumentException("Idempotency key cannot exceed 128 characters");
        }
        if (request.getDescription() != null && request.getDescription().length() > 255) {
            throw new IllegalArgumentException("Description cannot exceed 255 characters");
        }
    }

    private void validateOwnership(BankAccount account, User authUser) {
        boolean isOwner = account.getCustomer() != null
                && account.getCustomer().getUser() != null
                && account.getCustomer().getUser().getId().equals(authUser.getId());

        if (!isOwner) {
            throw new UnauthorizedAccountAccessException(
                    "Access denied: You are not authorized to operate on this account"
            );
        }
    }

    private void validateAccountActive(BankAccount account) {
        if (account.getAccountStatus() == AccountStatus.FROZEN) {
            throw new InactiveAccountException("Cannot perform deposit: Account is frozen");
        }
        if (account.getAccountStatus() == AccountStatus.CLOSED) {
            throw new InactiveAccountException("Cannot perform deposit: Account is closed");
        }
        if (account.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new InactiveAccountException("Cannot perform deposit: Account is not active");
        }
    }
}

