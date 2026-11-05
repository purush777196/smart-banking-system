package com.finova.repository;

import com.finova.entity.User;
import com.finova.entity.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("UserRepository: Should persist and find user by email")
    void testFindByEmail() {
        User user = new User("Marcus Sterling", "manager.test@finova.bank", "hashed_pwd", UserRole.BRANCH_MANAGER);
        userRepository.save(user);

        Optional<User> found = userRepository.findByEmail("manager.test@finova.bank");
        assertTrue(found.isPresent(), "User should be found by email");
        assertEquals("Marcus Sterling", found.get().getFullName());
        assertEquals(UserRole.BRANCH_MANAGER, found.get().getRole());
    }

    @Test
    @DisplayName("UserRepository: existsByEmail should return true when user exists")
    void testExistsByEmail() {
        User user = new User("Alice Vance", "alice@finova.bank", "hashed_pwd", UserRole.CUSTOMER);
        userRepository.save(user);

        assertTrue(userRepository.existsByEmail("alice@finova.bank"));
        assertFalse(userRepository.existsByEmail("nonexistent@finova.bank"));
    }
}
