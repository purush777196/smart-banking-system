package com.finova.service;

import com.finova.dto.customer.CustomerCreateRequest;
import com.finova.dto.customer.CustomerMapper;
import com.finova.dto.customer.CustomerProfileResponse;
import com.finova.dto.customer.CustomerResponse;
import com.finova.dto.customer.CustomerUpdateRequest;
import com.finova.entity.Customer;
import com.finova.entity.KycStatus;
import com.finova.entity.User;
import com.finova.entity.UserRole;
import com.finova.exception.CustomerAlreadyExistsException;
import com.finova.exception.CustomerNotFoundException;
import com.finova.exception.UnauthorizedCustomerAccessException;
import com.finova.exception.UserNotFoundException;
import com.finova.repository.CustomerRepository;
import com.finova.repository.UserRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Service implementing business logic, referential integrity, and role-based authorization
 * for Customer profile management.
 */
@Service
public class CustomerService {

    private static final Set<UserRole> AUTHORIZED_STAFF_ROLES = EnumSet.of(
            UserRole.SUPER_ADMIN,
            UserRole.BRANCH_MANAGER,
            UserRole.EMPLOYEE,
            UserRole.COMPLIANCE_OFFICER
    );

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final CustomerMapper customerMapper;
    private final CustomerCodeGenerator customerCodeGenerator;

    public CustomerService(CustomerRepository customerRepository,
                           UserRepository userRepository,
                           CustomerMapper customerMapper,
                           CustomerCodeGenerator customerCodeGenerator) {
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.customerMapper = customerMapper;
        this.customerCodeGenerator = customerCodeGenerator;
    }

    /**
     * Creates a new customer profile for an authenticated user with role CUSTOMER.
     * Enforces uniqueness, generates a secure customer code, and initializes KYC status as PENDING.
     */
    @Transactional
    public CustomerResponse createCustomer(CustomerCreateRequest request, Long authenticatedUserId) {
        User user = userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + authenticatedUserId));

        if (user.getRole() != UserRole.CUSTOMER) {
            throw new IllegalArgumentException("Only users with role CUSTOMER can create a customer profile");
        }

        if (customerRepository.existsByUserId(authenticatedUserId)) {
            throw new CustomerAlreadyExistsException("A customer profile already exists for user ID: " + authenticatedUserId);
        }

        String customerCode = customerCodeGenerator.generateUniqueCode();
        Customer customer = customerMapper.toEntity(request, user, customerCode);
        Customer savedCustomer = customerRepository.save(customer);

        return customerMapper.toResponse(savedCustomer);
    }

    /**
     * Retrieves a customer profile by its primary key.
     * Accessible by the profile owner or authorized banking staff.
     */
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(Long customerId, Long authenticatedUserId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerId));

        User authUser = userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new UserNotFoundException("Authenticated user not found with ID: " + authenticatedUserId));

        validateAccessPermission(customer, authUser);

        return customerMapper.toResponse(customer);
    }

    /**
     * Retrieves a customer profile by its unique business code.
     * Accessible by the profile owner or authorized banking staff.
     */
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerByCode(String customerCode, Long authenticatedUserId) {
        Customer customer = customerRepository.findByCustomerCode(customerCode)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with code: " + customerCode));

        User authUser = userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new UserNotFoundException("Authenticated user not found with ID: " + authenticatedUserId));

        validateAccessPermission(customer, authUser);

        return customerMapper.toResponse(customer);
    }

    /**
     * Retrieves the profile of the currently authenticated customer.
     */
    @Transactional(readOnly = true)
    public CustomerProfileResponse getMyProfile(Long authenticatedUserId) {
        if (!userRepository.existsById(authenticatedUserId)) {
            throw new UserNotFoundException("Authenticated user not found with ID: " + authenticatedUserId);
        }

        Customer customer = customerRepository.findByUserId(authenticatedUserId)
                .orElseThrow(() -> new CustomerNotFoundException("No customer profile found for user ID: " + authenticatedUserId));

        return customerMapper.toProfileResponse(customer);
    }

    /**
     * Updates permitted profile fields for an existing customer.
     * Strictly preserves customerCode, user linkage, KYC status, and date of birth.
     */
    @Transactional
    public CustomerResponse updateCustomer(Long customerId, CustomerUpdateRequest request, Long authenticatedUserId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerId));

        User authUser = userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new UserNotFoundException("Authenticated user not found with ID: " + authenticatedUserId));

        validateAccessPermission(customer, authUser);

        customerMapper.updateEntityFromRequest(customer, request);
        Customer updatedCustomer = customerRepository.save(customer);

        return customerMapper.toResponse(updatedCustomer);
    }

    /**
     * Lists customer profiles with dynamic filtering and pagination.
     * Restricted to authorized staff members only.
     */
    @Transactional(readOnly = true)
    public Page<CustomerResponse> getAllCustomers(String search, KycStatus kycStatus, String city, Pageable pageable, Long authenticatedUserId) {
        User authUser = userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new UserNotFoundException("Authenticated user not found with ID: " + authenticatedUserId));

        if (!isAuthorizedStaff(authUser.getRole())) {
            throw new UnauthorizedCustomerAccessException(
                    "Access denied: Only authorized banking staff can list customer profiles"
            );
        }

        Specification<Customer> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (city != null && !city.trim().isEmpty()) {
                predicates.add(cb.equal(cb.lower(root.get("city")), city.trim().toLowerCase()));
            }

            if (kycStatus != null) {
                predicates.add(cb.equal(root.get("kycStatus"), kycStatus));
            }

            if (search != null && !search.trim().isEmpty()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                Join<Customer, User> userJoin = root.join("user", JoinType.LEFT);

                Predicate searchPredicate = cb.or(
                        cb.like(cb.lower(root.get("customerCode")), pattern),
                        cb.like(cb.lower(root.get("phoneNumber")), pattern),
                        cb.like(cb.lower(userJoin.get("fullName")), pattern),
                        cb.like(cb.lower(userJoin.get("email")), pattern)
                );
                predicates.add(searchPredicate);
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Customer> page = customerRepository.findAll(spec, pageable);
        return page.map(customerMapper::toResponse);
    }

    /**
     * Helper to verify if the authenticated user is either the profile owner or an authorized staff member.
     */
    private void validateAccessPermission(Customer customer, User authUser) {
        boolean isOwner = customer.getUser() != null && customer.getUser().getId().equals(authUser.getId());
        boolean isStaff = isAuthorizedStaff(authUser.getRole());

        if (!isOwner && !isStaff) {
            throw new UnauthorizedCustomerAccessException(
                    "Access denied: You are not authorized to access or modify this customer profile"
            );
        }
    }

    private boolean isAuthorizedStaff(UserRole role) {
        return role != null && AUTHORIZED_STAFF_ROLES.contains(role);
    }
}

