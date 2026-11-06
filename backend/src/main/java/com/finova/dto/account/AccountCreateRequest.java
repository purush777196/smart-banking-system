package com.finova.dto.account;

import com.finova.entity.enums.AccountType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a new bank account.
 * Restricts client input strictly to permitted configuration fields.
 * Protected attributes such as accountNumber, customerId, balance, accountStatus,
 * and audit timestamps are intentionally excluded to prevent mass assignment vulnerabilities.
 */
public class AccountCreateRequest {

    @NotNull(message = "Account type is required")
    private AccountType accountType;

    @Size(max = 100, message = "Branch name cannot exceed 100 characters")
    private String branchName;

    @Size(max = 30, message = "Branch code cannot exceed 30 characters")
    private String branchCode;

    @Size(max = 20, message = "IFSC code cannot exceed 20 characters")
    private String ifscCode;

    public AccountCreateRequest() {
    }

    public AccountCreateRequest(AccountType accountType) {
        this.accountType = accountType;
    }

    public AccountCreateRequest(AccountType accountType, String branchName) {
        this.accountType = accountType;
        this.branchName = branchName;
    }

    public AccountCreateRequest(AccountType accountType, String branchName, String branchCode, String ifscCode) {
        this.accountType = accountType;
        this.branchName = branchName;
        this.branchCode = branchCode;
        this.ifscCode = ifscCode;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
    }

    public String getIfscCode() {
        return ifscCode;
    }

    public void setIfscCode(String ifscCode) {
        this.ifscCode = ifscCode;
    }
}

