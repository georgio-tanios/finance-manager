package com.myfinance.finance_manager.service;

import com.myfinance.finance_manager.dto.UserResponseDTO;
import com.myfinance.finance_manager.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AppUserRepository appUserRepository;

    public List<UserResponseDTO> getAllUsers() {
        return appUserRepository.findAll()
                .stream()
                .map(user -> new UserResponseDTO(
                        user.getId(),
                        user.getEmail(),
                        user.getRole()
                ))
                .toList();
    }
}