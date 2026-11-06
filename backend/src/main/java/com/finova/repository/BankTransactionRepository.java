package com.finova.repository;

import com.finova.entity.BankTransaction;
import com.finova.entity.enums.TransactionStatus;
import com.finova.entity.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for BankTransaction entity.
 * Provides optimized access methods for financial ledger auditing,
 * account-level statement lookups, status filtering, and double-entry transfer group tracing.
 */
@Repository
public interface BankTransactionRepository extends JpaRepository<BankTransaction, Long>, JpaSpecificationExecutor<BankTransaction> {

    Optional<BankTransaction> findByTransactionReference(String transactionReference);

    boolean existsByTransactionReference(String transactionReference);

    Optional<BankTransaction> findByIdempotencyKey(String idempotencyKey);

    boolean existsByIdempotencyKey(String idempotencyKey);

    List<BankTransaction> findByAccountId(Long accountId);

    Page<BankTransaction> findByAccountId(Long accountId, Pageable pageable);

    List<BankTransaction> findByAccountIdAndTransactionStatus(Long accountId, TransactionStatus transactionStatus);

    Page<BankTransaction> findByAccountIdAndTransactionStatus(Long accountId, TransactionStatus transactionStatus, Pageable pageable);

    List<BankTransaction> findByAccountIdAndTransactionType(Long accountId, TransactionType transactionType);

    Page<BankTransaction> findByAccountIdAndTransactionType(Long accountId, TransactionType transactionType, Pageable pageable);

    List<BankTransaction> findByTransferGroupReference(String transferGroupReference);
}

