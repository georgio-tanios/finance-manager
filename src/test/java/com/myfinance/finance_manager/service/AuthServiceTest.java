package com.myfinance.finance_manager.service;

import com.myfinance.finance_manager.dto.RegisterRequestDTO;
import com.myfinance.finance_manager.dto.UserResponseDTO;
import com.myfinance.finance_manager.exception.EmailAlreadyExistsException;
import com.myfinance.finance_manager.model.AppUser;
import com.myfinance.finance_manager.model.Role;
import com.myfinance.finance_manager.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_shouldNormalizeEmailEncodePasswordAndSaveUser() {
        // Arrange
        RegisterRequestDTO request = new RegisterRequestDTO(
                "  Alice@Example.COM  ",
                "password123"
        );

        when(appUserRepository.existsByEmail("alice@example.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("password123"))
                .thenReturn("encoded-password");

        when(appUserRepository.save(any(AppUser.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UserResponseDTO result = authService.register(request);

        // Assert
        ArgumentCaptor<AppUser> userCaptor =
                ArgumentCaptor.forClass(AppUser.class);

        verify(appUserRepository).save(userCaptor.capture());

        AppUser savedUser = userCaptor.getValue();

        assertAll(
                () -> assertEquals(
                        "alice@example.com",
                        savedUser.getEmail()
                ),
                () -> assertEquals(
                        "encoded-password",
                        savedUser.getPasswordHash()
                ),
                () -> assertEquals(Role.USER, savedUser.getRole()),
                () -> assertEquals(
                        "alice@example.com",
                        result.email()
                ),
                () -> assertEquals(Role.USER, result.role())
        );

        verify(passwordEncoder).encode("password123");
    }

    @Test
    void register_shouldThrowException_whenEmailIsAlreadyRegistered() {
        // Arrange
        RegisterRequestDTO request = new RegisterRequestDTO(
                "  Alice@Example.COM  ",
                "password123"
        );

        when(appUserRepository.existsByEmail("alice@example.com"))
                .thenReturn(true);

        // Act
        EmailAlreadyExistsException exception = assertThrows(
                EmailAlreadyExistsException.class,
                () -> authService.register(request)
        );

        // Assert
        assertEquals(
                "Email is already registered",
                exception.getMessage()
        );

        verify(appUserRepository)
                .existsByEmail("alice@example.com");

        verifyNoInteractions(passwordEncoder);

        verify(appUserRepository, never())
                .save(any(AppUser.class));
    }
}
