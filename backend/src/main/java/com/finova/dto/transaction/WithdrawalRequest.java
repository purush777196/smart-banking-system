package com.finova.dto.transaction;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Request DTO for account withdrawal operations.
 * Enforces strict financial input validation, nonblank idempotency tracking,
 * and excludes client-controlled state mutations (balances, timestamps, account numbers).
 */
public class WithdrawalRequest {

    @NotNull(message = "Withdrawal amount is required")
    @DecimalMin(value = "0.01", message = "Withdrawal amount must be strictly positive")
    @Digits(integer = 17, fraction = 2, message = "Withdrawal amount must adhere to monetary precision (up to 17 integer digits and 2 decimals)")
    private BigDecimal amount;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;

    @NotBlank(message = "Idempotency key is required")
    @Size(min = 1, max = 128, message = "Idempotency key must be between 1 and 128 characters")
    private String idempotencyKey;

    public WithdrawalRequest() {
    }

    public WithdrawalRequest(BigDecimal amount, String idempotencyKey) {
        this.amount = amount;
        this.idempotencyKey = idempotencyKey;
    }

    public WithdrawalRequest(BigDecimal amount, String description, String idempotencyKey) {
        this.amount = amount;
        this.description = description;
        this.idempotencyKey = idempotencyKey;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }
}

