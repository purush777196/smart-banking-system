package com.finova.transaction.controller;

import com.finova.dto.transaction.DepositRequest;
import com.finova.dto.transaction.TransactionHistoryResponse;
import com.finova.dto.transaction.TransactionResponse;
import com.finova.dto.transaction.WithdrawalRequest;
import com.finova.entity.User;
import com.finova.entity.enums.TransactionStatus;
import com.finova.entity.enums.TransactionType;
import com.finova.exception.UserNotFoundException;
import com.finova.repository.UserRepository;
import com.finova.service.DepositService;
import com.finova.service.TransactionService;
import com.finova.service.WithdrawalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

/**
 * REST controller for banking transactions: customer deposits, withdrawals,
 * account transaction history, and unique reference lookups.
 */
@RestController
public class TransactionController {

    private final DepositService depositService;
    private final WithdrawalService withdrawalService;
    private final TransactionService transactionService;
    private final UserRepository userRepository;

    public TransactionController(DepositService depositService,
                                 WithdrawalService withdrawalService,
                                 TransactionService transactionService,
                                 UserRepository userRepository) {
        this.depositService = depositService;
        this.withdrawalService = withdrawalService;
        this.transactionService = transactionService;
        this.userRepository = userRepository;
    }

    /**
     * Executes a customer deposit into the specified bank account.
     * Restricted to authenticated account owners.
     *
     * @param accountId      target bank account primary key
     * @param request        deposit parameters
     * @param authentication security principal
     * @return 201 Created with safe TransactionResponse
     */
    @PostMapping("/api/accounts/{accountId}/deposits")
    public ResponseEntity<TransactionResponse> deposit(
            @PathVariable Long accountId,
            @Valid @RequestBody DepositRequest request,
            Authentication authentication) {
        Long authenticatedUserId = getAuthenticatedUserId(authentication);
        TransactionResponse response = depositService.deposit(accountId, authenticatedUserId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Executes a customer withdrawal from the specified bank account.
     * Restricted to authenticated account owners.
     *
     * @param accountId      target bank account primary key
     * @param request        withdrawal parameters
     * @param authentication security principal
     * @return 200 OK with safe TransactionResponse
     */
    @PostMapping("/api/accounts/{accountId}/withdrawals")
    public ResponseEntity<TransactionResponse> withdraw(
            @PathVariable Long accountId,
            @Valid @RequestBody WithdrawalRequest request,
            Authentication authentication) {
        Long authenticatedUserId = getAuthenticatedUserId(authentication);
        TransactionResponse response = withdrawalService.withdraw(accountId, authenticatedUserId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves paginated transaction history for an account.
     * Accessible by the account owner or authorized staff.
     *
     * @param accountId      target bank account primary key
     * @param page           page index (default 0)
     * @param size           page size (default 20, max 100)
     * @param sortBy         sort property (default createdAt)
     * @param direction      sort direction ("asc" or "desc", default "desc")
     * @param type           optional transaction type filter
     * @param status         optional transaction status filter
     * @param startDate      optional start date (YYYY-MM-DD or ISO-8601)
     * @param endDate        optional end date (YYYY-MM-DD or ISO-8601)
     * @param authentication security principal
     * @return 200 OK with TransactionHistoryResponse
     */
    @GetMapping("/api/accounts/{accountId}/transactions")
    public ResponseEntity<TransactionHistoryResponse> getTransactionHistory(
            @PathVariable Long accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) TransactionStatus status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            Authentication authentication) {

        Long authenticatedUserId = getAuthenticatedUserId(authentication);
        LocalDateTime startDateTime = parseStartDate(startDate);
        LocalDateTime endDateTime = parseEndDate(endDate);

        TransactionHistoryResponse response = transactionService.getTransactionHistory(
                accountId, authenticatedUserId, page, size, sortBy, direction, type, status, startDateTime, endDateTime
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a single transaction by its unique reference string.
     * Accessible by the account owner or authorized staff.
     *
     * @param transactionReference unique transaction reference string
     * @param authentication       security principal
     * @return 200 OK with safe TransactionResponse
     */
    @GetMapping("/api/transactions/{transactionReference}")
    public ResponseEntity<TransactionResponse> getTransactionByReference(
            @PathVariable String transactionReference,
            Authentication authentication) {
        Long authenticatedUserId = getAuthenticatedUserId(authentication);
        TransactionResponse response = transactionService.getTransactionByReference(transactionReference, authenticatedUserId);
        return ResponseEntity.ok(response);
    }

    private User getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Authentication is required to access this resource");
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Authenticated user not found with email: " + email));
    }

    private Long getAuthenticatedUserId(Authentication authentication) {
        return getAuthenticatedUser(authentication).getId();
    }

    private LocalDateTime parseStartDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        try {
            if (dateStr.contains("T")) {
                return LocalDateTime.parse(dateStr.trim());
            }
            return LocalDate.parse(dateStr.trim()).atStartOfDay();
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid date format for startDate. Expected YYYY-MM-DD or ISO-8601: " + dateStr);
        }
    }

    private LocalDateTime parseEndDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        try {
            if (dateStr.contains("T")) {
                return LocalDateTime.parse(dateStr.trim());
            }
            return LocalDate.parse(dateStr.trim()).atTime(23, 59, 59, 999999999);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid date format for endDate. Expected YYYY-MM-DD or ISO-8601: " + dateStr);
        }
    }
}

