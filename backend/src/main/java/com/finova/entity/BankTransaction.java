package com.finova.entity;

import com.finova.entity.enums.TransactionStatus;
import com.finova.entity.enums.TransactionType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * BankTransaction entity representing financial ledger records in Finova.
 * Implements immutable transaction auditing, strict monetary precision (precision 19, scale 2),
 * and foreign-key referential integrity to prevent cascading deletion of ledger history.
 */
@Entity
@Table(name = "bank_transactions", indexes = {
    @Index(name = "idx_bank_tx_reference", columnList = "transaction_reference", unique = true),
    @Index(name = "idx_bank_tx_account_id", columnList = "account_id"),
    @Index(name = "idx_bank_tx_type", columnList = "transaction_type"),
    @Index(name = "idx_bank_tx_status", columnList = "transaction_status"),
    @Index(name = "idx_bank_tx_created_at", columnList = "created_at"),
    @Index(name = "idx_bank_tx_transfer_group", columnList = "transfer_group_reference"),
    @Index(name = "idx_bank_tx_idempotency_key", columnList = "idempotency_key", unique = true)
})
public class BankTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Transaction reference is required")
    @Size(min = 8, max = 100, message = "Transaction reference must be between 8 and 100 characters")
    @Column(name = "transaction_reference", nullable = false, unique = true, length = 100)
    private String transactionReference;

    @NotNull(message = "Bank account reference is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", referencedColumnName = "id", nullable = false)
    private BankAccount account;

    @NotNull(message = "Transaction type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 30)
    private TransactionType transactionType;

    @NotNull(message = "Transaction status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_status", nullable = false, length = 30)
    private TransactionStatus transactionStatus = TransactionStatus.PENDING;

    @NotNull(message = "Transaction amount is required")
    @DecimalMin(value = "0.01", message = "Transaction amount must be strictly positive")
    @Digits(integer = 17, fraction = 2, message = "Transaction amount must adhere to monetary precision (up to 17 integer digits and 2 decimals)")
    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @NotBlank(message = "Currency code is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency code must be a valid 3-character uppercase ISO code")
    @Column(name = "currency", nullable = false, length = 10)
    private String currency = "INR";

    @NotNull(message = "Balance before transaction is required")
    @DecimalMin(value = "0.00", message = "Balance before cannot be negative")
    @Digits(integer = 17, fraction = 2, message = "Balance before must adhere to monetary precision (up to 17 integer digits and 2 decimals)")
    @Column(name = "balance_before", nullable = false, precision = 19, scale = 2)
    private BigDecimal balanceBefore;

    @NotNull(message = "Balance after transaction is required")
    @DecimalMin(value = "0.00", message = "Balance after cannot be negative")
    @Digits(integer = 17, fraction = 2, message = "Balance after must adhere to monetary precision (up to 17 integer digits and 2 decimals)")
    @Column(name = "balance_after", nullable = false, precision = 19, scale = 2)
    private BigDecimal balanceAfter;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    @Column(name = "description", length = 255)
    private String description;

    @Size(max = 100, message = "Transfer group reference cannot exceed 100 characters")
    @Column(name = "transfer_group_reference", length = 100)
    private String transferGroupReference;

    @Size(max = 128, message = "Idempotency key cannot exceed 128 characters")
    @Column(name = "idempotency_key", unique = true, length = 128)
    private String idempotencyKey;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public BankTransaction() {
        this.transactionStatus = TransactionStatus.PENDING;
        this.currency = "INR";
    }

    public BankTransaction(String transactionReference,
                           BankAccount account,
                           TransactionType transactionType,
                           BigDecimal amount,
                           BigDecimal balanceBefore,
                           BigDecimal balanceAfter) {
        this();
        setTransactionReference(transactionReference);
        this.account = account;
        this.transactionType = transactionType;
        setAmount(amount);
        setBalanceBefore(balanceBefore);
        setBalanceAfter(balanceAfter);
    }

    public BankTransaction(String transactionReference,
                           BankAccount account,
                           TransactionType transactionType,
                           BigDecimal amount,
                           String currency,
                           BigDecimal balanceBefore,
                           BigDecimal balanceAfter,
                           String description) {
        this(transactionReference, account, transactionType, amount, balanceBefore, balanceAfter);
        setCurrency(currency);
        setDescription(description);
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.transactionStatus == null) {
            this.transactionStatus = TransactionStatus.PENDING;
        }
        if (this.currency == null || this.currency.isBlank()) {
            this.currency = "INR";
        } else {
            this.currency = this.currency.trim().toUpperCase();
        }
        if (this.transactionReference != null) {
            this.transactionReference = this.transactionReference.trim();
        }
        if (this.description != null) {
            this.description = this.description.trim();
        }
        if (this.transferGroupReference != null) {
            this.transferGroupReference = this.transferGroupReference.trim();
        }
        if (this.idempotencyKey != null) {
            this.idempotencyKey = this.idempotencyKey.trim();
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference != null ? transactionReference.trim() : null;
    }

    public BankAccount getAccount() {
        return account;
    }

    public void setAccount(BankAccount account) {
        this.account = account;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public TransactionStatus getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(TransactionStatus transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transaction amount must be strictly positive");
        }
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getBalanceBefore() {
        return balanceBefore;
    }

    public void setBalanceBefore(BigDecimal balanceBefore) {
        if (balanceBefore != null && balanceBefore.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Balance before cannot be negative");
        }
        this.balanceBefore = balanceBefore;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(BigDecimal balanceAfter) {
        if (balanceAfter != null && balanceAfter.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Balance after cannot be negative");
        }
        this.balanceAfter = balanceAfter;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description != null ? description.trim() : null;
    }

    public String getTransferGroupReference() {
        return transferGroupReference;
    }

    public void setTransferGroupReference(String transferGroupReference) {
        this.transferGroupReference = transferGroupReference != null ? transferGroupReference.trim() : null;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey != null ? idempotencyKey.trim() : null;
    }
}

