package com.finova.controller;

import com.finova.repository.UserRepository;
import com.finova.service.CustomerService;

/**
 * Controller alias for CustomerController under com.finova.controller package.
 */
public class CustomerController extends com.finova.customer.controller.CustomerController {

    public CustomerController(CustomerService customerService, UserRepository userRepository) {
        super(customerService, userRepository);
    }
}

