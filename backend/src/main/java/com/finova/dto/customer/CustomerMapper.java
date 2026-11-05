package com.finova.dto.customer;

import com.finova.entity.Customer;
import com.finova.entity.KycStatus;
import com.finova.entity.User;
import org.springframework.stereotype.Component;

/**
 * Dedicated mapper between Customer entities and safe DTO representations.
 * Prevents mass-assignment of protected attributes, avoids entity leakage,
 * and ensures passwords or credential hashes are never exposed.
 */
@Component
public class CustomerMapper {

    private static final String DEFAULT_MEMBERSHIP_TIER = "Finova Signature Tier";

    /**
     * Maps a CustomerCreateRequest and an authenticated User into a new Customer entity.
     * The customerCode is generated server-side and kycStatus defaults strictly to PENDING.
     */
    public Customer toEntity(CustomerCreateRequest request, User user, String customerCode) {
        if (request == null) {
            return null;
        }

        Customer customer = new Customer();
        customer.setUser(user);
        customer.setCustomerCode(customerCode != null ? customerCode.trim() : null);
        customer.setDateOfBirth(request.getDateOfBirth());
        customer.setPhoneNumber(request.getPhoneNumber() != null ? request.getPhoneNumber().trim() : null);
        customer.setAlternatePhoneNumber(request.getAlternatePhoneNumber() != null ? request.getAlternatePhoneNumber().trim() : null);
        customer.setGender(request.getGender());
        customer.setAddress(request.getAddress() != null ? request.getAddress().trim() : null);
        customer.setCity(request.getCity() != null ? request.getCity().trim() : null);
        customer.setState(request.getState() != null ? request.getState().trim() : null);
        customer.setPostalCode(request.getPostalCode() != null ? request.getPostalCode().trim() : null);
        customer.setCountry(request.getCountry() != null ? request.getCountry().trim() : null);
        customer.setOccupation(request.getOccupation() != null ? request.getOccupation().trim() : null);
        customer.setKycStatus(KycStatus.PENDING);

        return customer;
    }

    /**
     * Safely updates an existing Customer entity with fields permitted in CustomerUpdateRequest.
     * Protected fields (id, customerCode, user, kycStatus, createdAt, dateOfBirth) cannot be altered.
     */
    public void updateEntityFromRequest(Customer customer, CustomerUpdateRequest request) {
        if (customer == null || request == null) {
            return;
        }

        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) {
            customer.setPhoneNumber(request.getPhoneNumber().trim());
        }
        if (request.getAlternatePhoneNumber() != null) {
            customer.setAlternatePhoneNumber(request.getAlternatePhoneNumber().trim());
        }
        if (request.getGender() != null) {
            customer.setGender(request.getGender());
        }
        if (request.getAddress() != null && !request.getAddress().isBlank()) {
            customer.setAddress(request.getAddress().trim());
        }
        if (request.getCity() != null && !request.getCity().isBlank()) {
            customer.setCity(request.getCity().trim());
        }
        if (request.getState() != null && !request.getState().isBlank()) {
            customer.setState(request.getState().trim());
        }
        if (request.getPostalCode() != null && !request.getPostalCode().isBlank()) {
            customer.setPostalCode(request.getPostalCode().trim());
        }
        if (request.getCountry() != null && !request.getCountry().isBlank()) {
            customer.setCountry(request.getCountry().trim());
        }
        if (request.getOccupation() != null) {
            customer.setOccupation(request.getOccupation().trim());
        }
    }

    /**
     * Converts a Customer entity into a sanitized CustomerResponse.
     */
    public CustomerResponse toResponse(Customer customer) {
        if (customer == null) {
            return null;
        }

        CustomerResponse response = new CustomerResponse();
        response.setId(customer.getId());
        response.setCustomerCode(customer.getCustomerCode());

        if (customer.getUser() != null) {
            response.setFullName(customer.getUser().getFullName());
            response.setEmail(customer.getUser().getEmail());
        }

        response.setPhoneNumber(customer.getPhoneNumber());
        response.setAlternatePhoneNumber(customer.getAlternatePhoneNumber());
        response.setDateOfBirth(customer.getDateOfBirth());
        response.setGender(customer.getGender());
        response.setAddress(customer.getAddress());
        response.setCity(customer.getCity());
        response.setState(customer.getState());
        response.setPostalCode(customer.getPostalCode());
        response.setCountry(customer.getCountry());
        response.setOccupation(customer.getOccupation());
        response.setKycStatus(customer.getKycStatus());
        response.setCreatedAt(customer.getCreatedAt());
        response.setUpdatedAt(customer.getUpdatedAt());

        return response;
    }

    /**
     * Converts a Customer entity into a customer profile response for the authenticated user.
     */
    public CustomerProfileResponse toProfileResponse(Customer customer) {
        if (customer == null) {
            return null;
        }

        CustomerProfileResponse response = new CustomerProfileResponse();
        response.setCustomerId(customer.getId());

        if (customer.getUser() != null) {
            response.setUserId(customer.getUser().getId());
            response.setFullName(customer.getUser().getFullName());
            response.setEmail(customer.getUser().getEmail());
        }

        response.setCustomerCode(customer.getCustomerCode());
        response.setPhoneNumber(customer.getPhoneNumber());
        response.setAlternatePhoneNumber(customer.getAlternatePhoneNumber());
        response.setDateOfBirth(customer.getDateOfBirth());
        response.setGender(customer.getGender());
        response.setAddress(customer.getAddress());
        response.setCity(customer.getCity());
        response.setState(customer.getState());
        response.setPostalCode(customer.getPostalCode());
        response.setCountry(customer.getCountry());
        response.setOccupation(customer.getOccupation());
        response.setKycStatus(customer.getKycStatus());
        response.setMembershipTier(DEFAULT_MEMBERSHIP_TIER);
        response.setCreatedAt(customer.getCreatedAt());
        response.setUpdatedAt(customer.getUpdatedAt());

        return response;
    }
}

