package com.finova.dto.transaction;

import com.finova.entity.BankTransaction;
import org.springframework.stereotype.Component;

/**
 * Mapper converting BankTransaction JPA entities to safe TransactionResponse DTOs.
 */
@Component
public class TransactionMapper {

    public TransactionResponse toResponse(BankTransaction transaction) {
        if (transaction == null) {
            return null;
        }

        TransactionResponse response = new TransactionResponse();
        response.setId(transaction.getId());
        response.setTransactionReference(transaction.getTransactionReference());
        if (transaction.getAccount() != null) {
            response.setAccountNumber(transaction.getAccount().getAccountNumber());
        }
        response.setTransactionType(transaction.getTransactionType());
        response.setTransactionStatus(transaction.getTransactionStatus());
        response.setAmount(transaction.getAmount());
        response.setCurrency(transaction.getCurrency());
        response.setBalanceBefore(transaction.getBalanceBefore());
        response.setBalanceAfter(transaction.getBalanceAfter());
        response.setDescription(transaction.getDescription());
        response.setCreatedAt(transaction.getCreatedAt());

        return response;
    }
}

