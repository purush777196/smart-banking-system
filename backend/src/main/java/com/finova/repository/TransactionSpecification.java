package com.finova.repository;

import com.finova.entity.BankTransaction;
import com.finova.entity.enums.TransactionStatus;
import com.finova.entity.enums.TransactionType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specifications for dynamic, type-safe filtering of BankTransaction ledger entries.
 */
public class TransactionSpecification {

    public static Specification<BankTransaction> filterTransactions(
            Long accountId,
            TransactionType type,
            TransactionStatus status,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (accountId != null) {
                predicates.add(cb.equal(root.get("account").get("id"), accountId));
            }

            if (type != null) {
                predicates.add(cb.equal(root.get("transactionType"), type));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("transactionStatus"), status));
            }

            if (startDateTime != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startDateTime));
            }

            if (endDateTime != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), endDateTime));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

