package com.myfinance.finance_manager.dto;

import com.myfinance.finance_manager.model.Role;

public record UserResponseDTO(
        Long id,
        String email,
        Role role
) {
}
