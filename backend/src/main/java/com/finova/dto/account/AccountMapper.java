package com.finova.dto.account;

import com.finova.entity.BankAccount;
import com.finova.entity.Customer;
import com.finova.entity.enums.AccountStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Dedicated mapper between BankAccount entities and safe DTO representations.
 * Prevents mass-assignment of protected attributes, avoids entity leakage,
 * and ensures customer PII or entity graphs are not unnecessarily exposed.
 */
@Component
public class AccountMapper {

    private static final String DEFAULT_CURRENCY = "INR";

    /**
     * Maps an AccountCreateRequest, parent Customer, and generated account number into a new BankAccount entity.
     * Enforces default status ACTIVE, zero initial balance (0.00), and INR currency.
     */
    public BankAccount toEntity(AccountCreateRequest request, Customer customer, String accountNumber) {
        if (request == null) {
            return null;
        }

        BankAccount account = new BankAccount();
        account.setAccountNumber(accountNumber != null ? accountNumber.trim() : null);
        account.setCustomer(customer);
        account.setAccountType(request.getAccountType());
        account.setAccountStatus(AccountStatus.ACTIVE);
        account.setBalance(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        account.setCurrency(DEFAULT_CURRENCY);

        if (request.getBranchName() != null && !request.getBranchName().isBlank()) {
            account.setBranchName(request.getBranchName().trim());
        }
        if (request.getBranchCode() != null && !request.getBranchCode().isBlank()) {
            account.setBranchCode(request.getBranchCode().trim());
        }
        if (request.getIfscCode() != null && !request.getIfscCode().isBlank()) {
            account.setIfscCode(request.getIfscCode().trim().toUpperCase());
        }

        return account;
    }

    /**
     * Converts a BankAccount entity into a sanitized AccountResponse.
     */
    public AccountResponse toResponse(BankAccount account) {
        if (account == null) {
            return null;
        }

        AccountResponse response = new AccountResponse();
        response.setId(account.getId());
        response.setAccountNumber(account.getAccountNumber());
        response.setAccountType(account.getAccountType());
        response.setAccountStatus(account.getAccountStatus());
        response.setBalance(account.getBalance());
        response.setCurrency(account.getCurrency());
        response.setBranchName(account.getBranchName());
        response.setBranchCode(account.getBranchCode());
        response.setIfscCode(account.getIfscCode());
        response.setCreatedAt(account.getCreatedAt());
        response.setUpdatedAt(account.getUpdatedAt());

        return response;
    }
}

