package com.finova.repository;

import com.finova.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long>, JpaSpecificationExecutor<Customer> {

    Optional<Customer> findByCustomerCode(String customerCode);

    Optional<Customer> findByUserId(Long userId);

    @Query("SELECT c FROM Customer c WHERE LOWER(c.user.email) = LOWER(:email)")
    Optional<Customer> findByUserEmail(@Param("email") String email);

    Optional<Customer> findByPhoneNumber(String phoneNumber);

    boolean existsByCustomerCode(String customerCode);

    boolean existsByUserId(Long userId);

    boolean existsByPhoneNumber(String phoneNumber);
}

