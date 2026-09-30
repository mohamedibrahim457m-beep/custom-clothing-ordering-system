package com.customclothing.system.service;

import com.customclothing.system.dto.UserRegisterDTO;
import com.customclothing.system.dto.UserResponseDTO;
import com.customclothing.system.entity.User;
import com.customclothing.system.enums.Role;
import com.customclothing.system.repository.UserRepository;
import com.customclothing.system.service.impl.UserServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceEmailTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("register sends welcome email when a new customer registers")
    void testRegister_TriggersWelcomeEmailForCustomer() {
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setFullName("Emma Watson");
        dto.setEmail("emma@example.com");
        dto.setPassword("password123");
        dto.setRole(Role.CUSTOMER);

        when(userRepository.existsByEmail("emma@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(100L);
            return u;
        });

        UserResponseDTO result = userService.register(dto);

        assertNotNull(result);
        assertEquals("emma@example.com", result.getEmail());
        verify(emailService, times(1)).sendCustomerWelcomeEmail(any(UserResponseDTO.class));
    }

    @Test
    @DisplayName("register succeeds even if emailService throws an unexpected error")
    void testRegister_SucceedsEvenIfEmailFails() {
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setFullName("James Bond");
        dto.setEmail("james@example.com");
        dto.setPassword("agent007");
        dto.setRole(Role.CUSTOMER);

        when(userRepository.existsByEmail("james@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(101L);
            return u;
        });

        doThrow(new RuntimeException("Simulated Mail Server Outage"))
                .when(emailService).sendCustomerWelcomeEmail(any(UserResponseDTO.class));

        // Registration should not fail or throw exception
        UserResponseDTO result = assertDoesNotThrow(() -> userService.register(dto));

        assertNotNull(result);
        assertEquals("james@example.com", result.getEmail());
        verify(emailService, times(1)).sendCustomerWelcomeEmail(any(UserResponseDTO.class));
    }
}
