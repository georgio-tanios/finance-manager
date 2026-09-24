package com.myfinance.finance_manager.service;

import com.myfinance.finance_manager.dto.RegisterRequestDTO;
import com.myfinance.finance_manager.dto.UserResponseDTO;
import com.myfinance.finance_manager.exception.EmailAlreadyExistsException;
import com.myfinance.finance_manager.model.AppUser;
import com.myfinance.finance_manager.model.Role;
import com.myfinance.finance_manager.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponseDTO register(RegisterRequestDTO request){
        String normalizedEmail = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (appUserRepository.existsByEmail(normalizedEmail)){
            throw new EmailAlreadyExistsException(
                    "Email is already registered"
            );
        }

        String passwordHash = passwordEncoder.encode(request.password());

        AppUser user = new AppUser(
                normalizedEmail,
                passwordHash,
                Role.USER
        );

        AppUser savedUser = appUserRepository.save(user);

        return new UserResponseDTO(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getRole()
        );
    }
}
