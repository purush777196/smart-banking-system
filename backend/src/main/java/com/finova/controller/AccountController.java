package com.finova.controller;

import com.finova.repository.UserRepository;
import com.finova.service.AccountService;

/**
 * Controller alias for AccountController under com.finova.controller package.
 */
public class AccountController extends com.finova.account.controller.AccountController {

    public AccountController(AccountService accountService, UserRepository userRepository) {
        super(accountService, userRepository);
    }
}

