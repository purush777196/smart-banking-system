package com.finova.entity.enums;

/**
 * Supported banking transaction classifications in the Finova banking system.
 * Represents financial operations across deposit, withdrawal, and double-entry transfer legs.
 */
public enum TransactionType {
    DEPOSIT,
    WITHDRAWAL,
    TRANSFER_DEBIT,
    TRANSFER_CREDIT
}

