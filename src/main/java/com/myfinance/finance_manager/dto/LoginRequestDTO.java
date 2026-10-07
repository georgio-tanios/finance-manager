package com.myfinance.finance_manager.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(

        @NotBlank(message = "Email must be provided")
        @Email(message = "Email must be valid")
        String email,

        @NotBlank(message = "Password must be provided")
        String password
) {
}