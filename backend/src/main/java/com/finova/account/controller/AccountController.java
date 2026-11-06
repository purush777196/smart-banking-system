package com.finova.account.controller;

import com.finova.dto.account.AccountCreateRequest;
import com.finova.dto.account.AccountResponse;
import com.finova.entity.User;
import com.finova.entity.UserRole;
import com.finova.exception.UserNotFoundException;
import com.finova.repository.UserRepository;
import com.finova.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * REST controller for Bank Account management, creation, retrieval, paginated listing, and freezing.
 * Derives user identity strictly from the verified security context to prevent ID spoofing.
 */
@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private static final Set<String> ALLOWED_SORT_PROPERTIES = Set.of(
            "id", "accountNumber", "accountType", "accountStatus", "balance",
            "currency", "branchName", "branchCode", "ifscCode", "createdAt", "updatedAt"
    );

    private final AccountService accountService;
    private final UserRepository userRepository;

    public AccountController(AccountService accountService, UserRepository userRepository) {
        this.accountService = accountService;
        this.userRepository = userRepository;
    }

    /**
     * Creates a new bank account for the currently authenticated customer.
     * Restricted to CUSTOMER role.
     *
     * @param request        account creation options (type, branch)
     * @param authentication security principal
     * @return 201 Created with safe AccountResponse
     */
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<AccountResponse> createAccount(
            @Valid @RequestBody AccountCreateRequest request,
            Authentication authentication) {
        User authUser = getAuthenticatedUser(authentication);
        if (authUser.getRole() != UserRole.CUSTOMER) {
            throw new AccessDeniedException("Access denied: Only CUSTOMER role can create bank accounts");
        }

        AccountResponse response = accountService.createAccount(request, authUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves all bank accounts belonging to the authenticated customer.
     *
     * @param authentication security principal
     * @return 200 OK with list of accounts
     */
    @GetMapping("/me")
    public ResponseEntity<java.util.List<AccountResponse>> getMyAccounts(Authentication authentication) {
        Long authenticatedUserId = getAuthenticatedUserId(authentication);
        java.util.List<AccountResponse> response = accountService.getMyAccounts(authenticatedUserId);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a bank account by its primary key ID.
     * Accessible by the account owner or authorized staff.
     *
     * @param id             database primary key
     * @param authentication security principal
     * @return 200 OK with AccountResponse
     */
    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getAccountById(
            @PathVariable Long id,
            Authentication authentication) {
        Long authenticatedUserId = getAuthenticatedUserId(authentication);
        AccountResponse response = accountService.getAccountById(id, authenticatedUserId);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a bank account by its unique business account number.
     * Accessible by the account owner or authorized staff.
     *
     * @param accountNumber  account number string
     * @param authentication security principal
     * @return 200 OK with AccountResponse
     */
    @GetMapping("/number/{accountNumber}")
    public ResponseEntity<AccountResponse> getAccountByNumber(
            @PathVariable String accountNumber,
            Authentication authentication) {
        Long authenticatedUserId = getAuthenticatedUserId(authentication);
        AccountResponse response = accountService.getAccountByNumber(accountNumber, authenticatedUserId);
        return ResponseEntity.ok(response);
    }

    /**
     * Staff-authorized endpoint for paginated and sorted bank account listings.
     * Customers are rejected with 403 Forbidden.
     *
     * @param page           page index (default 0)
     * @param size           page size (default 10, max 100)
     * @param sortBy         sorting property (default createdAt)
     * @param sortDir        sorting direction (asc/desc, default desc)
     * @param authentication security principal
     * @return 200 OK with paginated accounts
     */
    @GetMapping
    public ResponseEntity<Page<AccountResponse>> getAllAccounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            Authentication authentication) {

        if (page < 0) {
            throw new IllegalArgumentException("Page index must not be less than zero");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("Page size must be between 1 and 100");
        }
        if (!ALLOWED_SORT_PROPERTIES.contains(sortBy)) {
            throw new IllegalArgumentException("Invalid sort field: '" + sortBy + "'. Allowed fields: " + ALLOWED_SORT_PROPERTIES);
        }
        if (!sortDir.equalsIgnoreCase("asc") && !sortDir.equalsIgnoreCase("desc")) {
            throw new IllegalArgumentException("Invalid sort direction: '" + sortDir + "'. Allowed values: asc, desc");
        }

        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Long authenticatedUserId = getAuthenticatedUserId(authentication);
        Page<AccountResponse> result = accountService.getAllAccounts(authenticatedUserId, pageable);

        return ResponseEntity.ok(result);
    }

    /**
     * Staff-authorized endpoint to freeze an active bank account.
     * Customers are rejected with 403 Forbidden.
     *
     * @param id             account ID to freeze
     * @param authentication security principal
     * @return 200 OK with updated AccountResponse
     */
    @PatchMapping("/{id}/freeze")
    public ResponseEntity<AccountResponse> freezeAccount(
            @PathVariable Long id,
            Authentication authentication) {
        Long authenticatedUserId = getAuthenticatedUserId(authentication);
        AccountResponse response = accountService.freezeAccount(id, authenticatedUserId);
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
}

