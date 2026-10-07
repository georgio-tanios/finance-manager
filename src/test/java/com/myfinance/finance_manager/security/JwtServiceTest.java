package com.myfinance.finance_manager.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String TEST_SECRET =
            "dGVzdC1qd3Qtc2VjcmV0LXRlc3QtMzItYnl0ZXMtbG9uZw==";

    private JwtService jwtService;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(
                TEST_SECRET,
                3_600_000
        );

        userDetails = User.builder()
                .username("alice@example.com")
                .password("unused")
                .roles("USER")
                .build();
    }

    @Test
    void generateToken_shouldCreateValidToken() {
        // Act
        String token = jwtService.generateToken(userDetails);

        // Assert
        assertAll(
                () -> assertNotNull(token),
                () -> assertFalse(token.isBlank()),
                () -> assertEquals(
                        "alice@example.com",
                        jwtService.extractUsername(token)
                ),
                () -> assertTrue(
                        jwtService.isTokenValid(
                                token,
                                userDetails
                        )
                )
        );
    }
}