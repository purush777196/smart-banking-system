package com.finova.repository;

import org.springframework.stereotype.Repository;

/**
 * TransactionRepository alias interface extending BankTransactionRepository.
 * Provides optimized access methods for financial ledger auditing and specification queries.
 */
@Repository
public interface TransactionRepository extends BankTransactionRepository {
}

