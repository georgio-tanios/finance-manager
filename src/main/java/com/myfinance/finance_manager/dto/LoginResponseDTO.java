package com.myfinance.finance_manager.dto;

public record LoginResponseDTO(
        String accessToken,
        String tokenType
) {
}