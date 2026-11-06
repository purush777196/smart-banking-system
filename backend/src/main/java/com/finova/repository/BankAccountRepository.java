package com.finova.repository;

import com.finova.entity.BankAccount;
import com.finova.entity.enums.AccountStatus;
import com.finova.entity.enums.AccountType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for BankAccount entity.
 * Provides optimized access methods for financial account lookup by account number,
 * customer linkage, status filtering, and account type classification.
 */
@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM BankAccount b WHERE b.id = :id")
    Optional<BankAccount> findByIdForUpdate(@Param("id") Long id);

    Optional<BankAccount> findByAccountNumber(String accountNumber);

    boolean existsByAccountNumber(String accountNumber);

    List<BankAccount> findByCustomerId(Long customerId);

    List<BankAccount> findByCustomerIdAndAccountStatus(Long customerId, AccountStatus accountStatus);

    List<BankAccount> findByCustomerIdAndAccountType(Long customerId, AccountType accountType);
}

