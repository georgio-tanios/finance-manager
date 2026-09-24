package com.myfinance.finance_manager.controller;

import com.myfinance.finance_manager.config.SecurityConfig;
import com.myfinance.finance_manager.dto.RegisterRequestDTO;
import com.myfinance.finance_manager.dto.UserResponseDTO;
import com.myfinance.finance_manager.exception.EmailAlreadyExistsException;
import com.myfinance.finance_manager.model.Role;
import com.myfinance.finance_manager.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Test
    void register_shouldReturnCreatedUser() throws Exception {
        // Arrange
        UserResponseDTO response = new UserResponseDTO(
                1L,
                "alice@example.com",
                Role.USER
        );

        when(authService.register(argThat(request ->
                request.email().equals("Alice@Example.COM")
                        && request.password().equals("password123")
        ))).thenReturn(response);

        // Act + Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "Alice@Example.COM",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email")
                        .value("alice@example.com"))
                .andExpect(jsonPath("$.role").value("USER"));

        verify(authService).register(argThat(request ->
                request.email().equals("Alice@Example.COM")
                        && request.password().equals("password123")
        ));
    }

    @Test
    void register_shouldReturnBadRequest_whenEmailIsInvalid()
            throws Exception {

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "email": "invalid-email",
                              "password": "password123"
                            }
                            """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    @Test
    void register_shouldReturnBadRequest_whenPasswordIsTooShort()
            throws Exception {

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "email": "alice@example.com",
                              "password": "123"
                            }
                            """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    @Test
    void register_shouldReturnConflict_whenEmailAlreadyExists()
            throws Exception {

        when(authService.register(any(RegisterRequestDTO.class)))
                .thenThrow(new EmailAlreadyExistsException(
                        "Email is already registered"
                ));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "email": "alice@example.com",
                              "password": "password123"
                            }
                            """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("Email is already registered"));
    }
}