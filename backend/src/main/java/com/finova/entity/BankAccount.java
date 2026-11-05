package com.finova.entity;

import com.finova.entity.enums.AccountStatus;
import com.finova.entity.enums.AccountType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * BankAccount entity representing customer bank accounts within Finova.
 * Supports multiple accounts per customer, enforces monetary precision (precision 19, scale 2),
 * and prevents negative initial balances and unauthorized cascading deletions.
 */
@Entity
@Table(name = "bank_accounts", indexes = {
    @Index(name = "idx_bank_account_number", columnList = "account_number", unique = true),
    @Index(name = "idx_bank_account_customer_id", columnList = "customer_id"),
    @Index(name = "idx_bank_account_status", columnList = "account_status"),
    @Index(name = "idx_bank_account_type", columnList = "account_type")
})
public class BankAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Account number is required")
    @Size(min = 8, max = 50, message = "Account number must be between 8 and 50 characters")
    @Column(name = "account_number", nullable = false, unique = true, length = 50)
    private String accountNumber;

    @NotNull(message = "Customer reference is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", referencedColumnName = "id", nullable = false)
    private Customer customer;

    @NotNull(message = "Account type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 30)
    private AccountType accountType;

    @NotNull(message = "Account status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false, length = 30)
    private AccountStatus accountStatus = AccountStatus.ACTIVE;

    @NotNull(message = "Account balance is required")
    @DecimalMin(value = "0.00", message = "Account balance cannot be negative")
    @Digits(integer = 17, fraction = 2, message = "Account balance must adhere to monetary precision (up to 17 integer digits and 2 decimals)")
    @Column(name = "balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    @NotBlank(message = "Currency code is required")
    @Size(min = 3, max = 10, message = "Currency code must be between 3 and 10 characters")
    @Column(name = "currency", nullable = false, length = 10)
    private String currency = "INR";

    @Size(max = 100, message = "Branch name cannot exceed 100 characters")
    @Column(name = "branch_name", length = 100)
    private String branchName;

    @Size(max = 30, message = "Branch code cannot exceed 30 characters")
    @Column(name = "branch_code", length = 30)
    private String branchCode;

    @Size(max = 20, message = "IFSC code cannot exceed 20 characters")
    @Column(name = "ifsc_code", length = 20)
    private String ifscCode;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public BankAccount() {
        this.accountStatus = AccountStatus.ACTIVE;
        this.balance = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        this.currency = "INR";
    }

    public BankAccount(String accountNumber, Customer customer, AccountType accountType) {
        this();
        this.accountNumber = accountNumber != null ? accountNumber.trim() : null;
        this.customer = customer;
        this.accountType = accountType;
    }

    public BankAccount(String accountNumber, Customer customer, AccountType accountType,
                       BigDecimal balance, String currency) {
        this(accountNumber, customer, accountType);
        if (balance != null) {
            if (balance.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Initial balance cannot be negative");
            }
            this.balance = balance;
        }
        if (currency != null && !currency.isBlank()) {
            this.currency = currency.trim().toUpperCase();
        }
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.accountStatus == null) {
            this.accountStatus = AccountStatus.ACTIVE;
        }
        if (this.balance == null) {
            this.balance = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        } else {
            this.balance = this.balance.setScale(2, RoundingMode.HALF_UP);
        }
        if (this.currency == null || this.currency.isBlank()) {
            this.currency = "INR";
        }
        if (this.balance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Initial balance cannot be negative");
        }
        if (this.accountNumber != null) {
            this.accountNumber = this.accountNumber.trim();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        if (this.balance != null) {
            this.balance = this.balance.setScale(2, RoundingMode.HALF_UP);
            if (this.balance.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Account balance cannot be negative");
            }
        }
        if (this.accountNumber != null) {
            this.accountNumber = this.accountNumber.trim();
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber != null ? accountNumber.trim() : null;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public AccountStatus getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(AccountStatus accountStatus) {
        this.accountStatus = accountStatus;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        if (balance != null) {
            if (balance.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Account balance cannot be negative");
            }
            this.balance = balance;
        } else {
            this.balance = null;
        }
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency != null ? currency.trim().toUpperCase() : null;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName != null ? branchName.trim() : null;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode != null ? branchCode.trim() : null;
    }

    public String getIfscCode() {
        return ifscCode;
    }

    public void setIfscCode(String ifscCode) {
        this.ifscCode = ifscCode != null ? ifscCode.trim().toUpperCase() : null;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
