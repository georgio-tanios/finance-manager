package com.myfinance.finance_manager.service;

import com.myfinance.finance_manager.dto.LoginRequestDTO;
import com.myfinance.finance_manager.dto.LoginResponseDTO;
import com.myfinance.finance_manager.dto.RegisterRequestDTO;
import com.myfinance.finance_manager.dto.UserResponseDTO;
import com.myfinance.finance_manager.exception.EmailAlreadyExistsException;
import com.myfinance.finance_manager.model.AppUser;
import com.myfinance.finance_manager.model.Role;
import com.myfinance.finance_manager.repository.AppUserRepository;
import com.myfinance.finance_manager.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
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

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

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

    @Test
    void login_shouldAuthenticateUserAndReturnToken() {
        // Arrange
        LoginRequestDTO request = new LoginRequestDTO(
                "  Alice@Example.COM  ",
                "password123"
        );

        UserDetails userDetails = User.builder()
                .username("alice@example.com")
                .password("encoded-password")
                .roles("USER")
                .build();

        Authentication authenticatedUser =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        when(authenticationManager.authenticate(
                any(Authentication.class)
        )).thenReturn(authenticatedUser);

        when(jwtService.generateToken(userDetails))
                .thenReturn("generated-jwt-token");

        // Act
        LoginResponseDTO result = authService.login(request);

        // Assert
        assertAll(
                () -> assertEquals(
                        "generated-jwt-token",
                        result.accessToken()
                ),
                () -> assertEquals(
                        "Bearer",
                        result.tokenType()
                )
        );

        verify(authenticationManager).authenticate(
                argThat(authentication ->
                        authentication.getName()
                                .equals("alice@example.com")
                                && authentication.getCredentials()
                                .equals("password123")
                )
        );

        verify(jwtService).generateToken(userDetails);
    }

    @Test
    void login_shouldNotGenerateToken_whenCredentialsAreInvalid() {
        // Arrange
        LoginRequestDTO request = new LoginRequestDTO(
                "alice@example.com",
                "wrong-password"
        );

        when(authenticationManager.authenticate(
                any(Authentication.class)
        )).thenThrow(
                new BadCredentialsException("Bad credentials")
        );

        // Act
        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class,
                () -> authService.login(request)
        );

        // Assert
        assertEquals(
                "Bad credentials",
                exception.getMessage()
        );

        verify(authenticationManager).authenticate(
                any(Authentication.class)
        );

        verifyNoInteractions(jwtService);
    }
}
