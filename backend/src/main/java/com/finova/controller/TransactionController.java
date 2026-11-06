package com.finova.controller;

import com.finova.repository.UserRepository;
import com.finova.service.DepositService;
import com.finova.service.TransactionService;
import com.finova.service.WithdrawalService;

/**
 * Controller alias for TransactionController under com.finova.controller package.
 */
public class TransactionController extends com.finova.transaction.controller.TransactionController {

    public TransactionController(DepositService depositService,
                                 WithdrawalService withdrawalService,
                                 TransactionService transactionService,
                                 UserRepository userRepository) {
        super(depositService, withdrawalService, transactionService, userRepository);
    }
}

