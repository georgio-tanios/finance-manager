package com.myfinance.finance_manager.security;

import com.myfinance.finance_manager.model.AppUser;
import com.myfinance.finance_manager.model.Role;
import com.myfinance.finance_manager.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    @Test
    void loadUserByUsername_shouldReturnUserDetails() {
        // Arrange
        AppUser appUser = new AppUser(
                "alice@example.com",
                "encoded-password",
                Role.USER
        );

        when(appUserRepository.findByEmail("alice@example.com"))
                .thenReturn(Optional.of(appUser));

        // Act
        UserDetails result = userDetailsService
                .loadUserByUsername("  Alice@Example.COM  ");

        // Assert
        assertAll(
                () -> assertEquals(
                        "alice@example.com",
                        result.getUsername()
                ),
                () -> assertEquals(
                        "encoded-password",
                        result.getPassword()
                ),
                () -> assertTrue(
                        result.getAuthorities()
                                .stream()
                                .anyMatch(authority ->
                                        authority.getAuthority()
                                                .equals("ROLE_USER")
                                )
                )
        );

        verify(appUserRepository)
                .findByEmail("alice@example.com");
    }

    @Test
    void loadUserByUsername_shouldThrowException_whenUserDoesNotExist() {
        // Arrange
        when(appUserRepository.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        // Act
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername(
                        "unknown@example.com"
                )
        );

        // Assert
        assertEquals("User not found", exception.getMessage());

        verify(appUserRepository)
                .findByEmail("unknown@example.com");
    }
}