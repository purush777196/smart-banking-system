package com.finova.service;

import com.finova.repository.CustomerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomerCodeGeneratorTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerCodeGenerator customerCodeGenerator;

    @Test
    @DisplayName("CodeGenerator: Should generate valid format FIN-CUS-XXXXXXXX")
    void testGenerateUniqueCodeFormat() {
        when(customerRepository.existsByCustomerCode(anyString())).thenReturn(false);

        String code = customerCodeGenerator.generateUniqueCode();

        assertNotNull(code);
        assertTrue(code.startsWith("FIN-CUS-"), "Code must start with FIN-CUS- prefix");
        assertEquals(16, code.length(), "Code format should be FIN-CUS- followed by 8 characters");
        verify(customerRepository, atLeastOnce()).existsByCustomerCode(code);
    }

    @Test
    @DisplayName("CodeGenerator: Should retry and generate alternative when collision occurs")
    void testCollisionHandling() {
        // First code collides, second code is unique
        when(customerRepository.existsByCustomerCode(anyString()))
                .thenReturn(true)
                .thenReturn(false);

        String code = customerCodeGenerator.generateUniqueCode();

        assertNotNull(code);
        assertTrue(code.startsWith("FIN-CUS-"));
        verify(customerRepository, times(2)).existsByCustomerCode(anyString());
    }
}

