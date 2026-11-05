package com.finova.security;

import com.finova.entity.Customer;
import com.finova.entity.User;
import com.finova.entity.UserRole;
import com.finova.repository.CustomerRepository;
import com.finova.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
public class CustomerSecurityIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerRepository customerRepository;

    private MockMvc mockMvc;
    private String customerToken;
    private String staffToken;
    private Customer otherCustomer;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        User customerUser = userRepository.findByEmail("customer@finova.bank")
                .orElseThrow(() -> new IllegalStateException("customer@finova.bank not seeded"));
        customerToken = jwtService.generateToken(customerUser);

        User managerUser = userRepository.findByEmail("manager@finova.bank")
                .orElseThrow(() -> new IllegalStateException("manager@finova.bank not seeded"));
        staffToken = jwtService.generateToken(managerUser);

        // Ensure a second distinct customer exists to test cross-customer profile denial
        User otherUser = userRepository.findByEmail("other.customer@finova.bank").orElseGet(() -> {
            User u = new User("Bob Smith", "other.customer@finova.bank", "hash", UserRole.CUSTOMER);
            return userRepository.save(u);
        });

        if (!customerRepository.existsByUserId(otherUser.getId())) {
            Customer otherCust = new Customer(
                    "CUST-OTHER-1002",
                    otherUser,
                    LocalDate.of(1992, 3, 20),
                    "+1999888777",
                    "456 Elm St",
                    "Gotham",
                    "NY",
                    "10002",
                    "USA"
            );
            otherCustomer = customerRepository.save(otherCust);
        } else {
            otherCustomer = customerRepository.findByUserId(otherUser.getId()).get();
        }
    }

    @Test
    @DisplayName("Security: Missing Bearer token on secured endpoint returns 401 Unauthorized")
    void testMissingBearerTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/customers/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Security: Invalid or expired Bearer token returns 401 Unauthorized")
    void testInvalidBearerTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/customers/me")
                        .header("Authorization", "Bearer invalid.corrupted.token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Security: Authenticated customer accessing another customer's profile returns 403 Forbidden")
    void testCustomerAccessingOtherCustomerProfileReturns403() throws Exception {
        mockMvc.perform(get("/api/customers/" + otherCustomer.getId())
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Security: Authenticated customer accessing staff-only customer listing returns 403 Forbidden")
    void testCustomerAccessingListingReturns403() throws Exception {
        mockMvc.perform(get("/api/customers")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Security: Authorized staff can access customer listing (200 OK)")
    void testStaffCanAccessCustomerListingReturns200() throws Exception {
        mockMvc.perform(get("/api/customers?page=0&size=10")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").isNumber());
    }
}
