package com.finova.dto.account;

import com.finova.entity.BankAccount;
import com.finova.entity.Customer;
import com.finova.entity.User;
import com.finova.entity.UserRole;
import com.finova.entity.enums.AccountStatus;
import com.finova.entity.enums.AccountType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class AccountMapperTest {

    private final AccountMapper accountMapper = new AccountMapper();

    @Test
    @DisplayName("Mapper: Maps request and customer to new BankAccount entity with defaults")
    void testToEntity() {
        User user = new User("John Doe", "john@finova.bank", "pwd", UserRole.CUSTOMER);
        user.setId(10L);

        Customer customer = new Customer();
        customer.setId(100L);
        customer.setUser(user);
        customer.setDateOfBirth(LocalDate.of(1990, 1, 1));
        customer.setCustomerCode("CUST-001");

        AccountCreateRequest request = new AccountCreateRequest(AccountType.SAVINGS, "Main Branch", "BR-01", "FINO0001234");
        String accountNumber = "FIN1000000001";

        BankAccount account = accountMapper.toEntity(request, customer, accountNumber);

        assertNotNull(account);
        assertEquals("FIN1000000001", account.getAccountNumber());
        assertEquals(customer, account.getCustomer());
        assertEquals(AccountType.SAVINGS, account.getAccountType());
        assertEquals(AccountStatus.ACTIVE, account.getAccountStatus());
        assertEquals(new BigDecimal("0.00"), account.getBalance());
        assertEquals("INR", account.getCurrency());
        assertEquals("Main Branch", account.getBranchName());
        assertEquals("BR-01", account.getBranchCode());
        assertEquals("FINO0001234", account.getIfscCode());
    }

    @Test
    @DisplayName("Mapper: Maps BankAccount entity to AccountResponse DTO without exposing relationships")
    void testToResponse() {
        Customer customer = new Customer();
        customer.setId(100L);

        BankAccount account = new BankAccount("FIN1000000002", customer, AccountType.CURRENT);
        account.setId(50L);
        account.setAccountStatus(AccountStatus.ACTIVE);
        account.setBalance(new BigDecimal("25000.50"));
        account.setCurrency("INR");
        account.setBranchName("North Branch");
        account.setBranchCode("NB-02");
        account.setIfscCode("FINO0005678");
        account.setCreatedAt(LocalDateTime.now());
        account.setUpdatedAt(LocalDateTime.now());

        AccountResponse response = accountMapper.toResponse(account);

        assertNotNull(response);
        assertEquals(50L, response.getId());
        assertEquals("FIN1000000002", response.getAccountNumber());
        assertEquals(AccountType.CURRENT, response.getAccountType());
        assertEquals(AccountStatus.ACTIVE, response.getAccountStatus());
        assertEquals(new BigDecimal("25000.50"), response.getBalance());
        assertEquals("INR", response.getCurrency());
        assertEquals("North Branch", response.getBranchName());
        assertEquals("NB-02", response.getBranchCode());
        assertEquals("FINO0005678", response.getIfscCode());
        assertNotNull(response.getCreatedAt());
        assertNotNull(response.getUpdatedAt());
    }

    @Test
    @DisplayName("Mapper: Handles null gracefully")
    void testNullHandling() {
        assertNull(accountMapper.toEntity(null, null, null));
        assertNull(accountMapper.toResponse(null));
    }
}

