package com.finova.customer.controller;

import com.finova.dto.customer.CustomerCreateRequest;
import com.finova.dto.customer.CustomerProfileResponse;
import com.finova.dto.customer.CustomerResponse;
import com.finova.dto.customer.CustomerUpdateRequest;
import com.finova.entity.KycStatus;
import com.finova.entity.User;
import com.finova.exception.UserNotFoundException;
import com.finova.repository.UserRepository;
import com.finova.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Set;

/**
 * REST controller for Customer profile operations, listing, pagination, and filtering.
 * Strictly derives the user identity from the security context to prevent ID spoofing.
 */
@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private static final Set<String> ALLOWED_SORT_PROPERTIES = Set.of(
            "id", "customerCode", "dateOfBirth", "phoneNumber", "alternatePhoneNumber",
            "gender", "address", "city", "state", "postalCode", "country",
            "occupation", "kycStatus", "createdAt", "updatedAt"
    );

    private final CustomerService customerService;
    private final UserRepository userRepository;

    public CustomerController(CustomerService customerService, UserRepository userRepository) {
        this.customerService = customerService;
        this.userRepository = userRepository;
    }

    /**
     * Creates a new customer profile for the currently authenticated customer.
     */
    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(
            @Valid @RequestBody CustomerCreateRequest request,
            Authentication authentication) {
        Long authenticatedUserId = getAuthenticatedUserId(authentication);
        CustomerResponse response = customerService.createCustomer(request, authenticatedUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves the profile of the currently authenticated customer.
     */
    @GetMapping("/me")
    public ResponseEntity<CustomerProfileResponse> getMyProfile(Authentication authentication) {
        Long authenticatedUserId = getAuthenticatedUserId(authentication);
        CustomerProfileResponse response = customerService.getMyProfile(authenticatedUserId);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a customer profile by its primary key ID.
     * Enforces ownership or authorized staff access.
     */
    @GetMapping("/{customerId}")
    public ResponseEntity<CustomerResponse> getCustomerById(
            @PathVariable Long customerId,
            Authentication authentication) {
        Long authenticatedUserId = getAuthenticatedUserId(authentication);
        CustomerResponse response = customerService.getCustomerById(customerId, authenticatedUserId);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a customer profile by its unique business code.
     * Enforces ownership or authorized staff access.
     */
    @GetMapping("/code/{customerCode}")
    public ResponseEntity<CustomerResponse> getCustomerByCode(
            @PathVariable String customerCode,
            Authentication authentication) {
        Long authenticatedUserId = getAuthenticatedUserId(authentication);
        CustomerResponse response = customerService.getCustomerByCode(customerCode, authenticatedUserId);
        return ResponseEntity.ok(response);
    }

    /**
     * Updates permitted profile fields for an existing customer.
     * Enforces ownership or authorized staff access. Protected fields are untouched.
     */
    @PutMapping("/{customerId}")
    public ResponseEntity<CustomerResponse> updateCustomer(
            @PathVariable Long customerId,
            @Valid @RequestBody CustomerUpdateRequest request,
            Authentication authentication) {
        Long authenticatedUserId = getAuthenticatedUserId(authentication);
        CustomerResponse response = customerService.updateCustomer(customerId, request, authenticatedUserId);
        return ResponseEntity.ok(response);
    }

    /**
     * Staff-authorized endpoint for paginated and filtered customer listings.
     * Ordinary customers are rejected with 403 Forbidden.
     */
    @GetMapping
    public ResponseEntity<Page<CustomerResponse>> getAllCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String kycStatus,
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
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

        KycStatus parsedKycStatus = null;
        if (kycStatus != null && !kycStatus.trim().isEmpty()) {
            try {
                parsedKycStatus = KycStatus.valueOf(kycStatus.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid KYC status filter: '" + kycStatus + "'. Allowed values: " + Arrays.toString(KycStatus.values()));
            }
        }

        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Long authenticatedUserId = getAuthenticatedUserId(authentication);
        Page<CustomerResponse> result = customerService.getAllCustomers(search, parsedKycStatus, city, pageable, authenticatedUserId);

        return ResponseEntity.ok(result);
    }

    private Long getAuthenticatedUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Authentication is required to access this resource");
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .map(User::getId)
                .orElseThrow(() -> new UserNotFoundException("Authenticated user not found with email: " + email));
    }
}

