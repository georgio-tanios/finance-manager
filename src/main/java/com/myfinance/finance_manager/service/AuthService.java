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
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

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

    public LoginResponseDTO login(LoginRequestDTO request) {
        String normalizedEmail = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                normalizedEmail,
                                request.password()
                        )
                );

        UserDetails userDetails =
                (UserDetails) authentication.getPrincipal();

        String token = jwtService.generateToken(userDetails);

        return new LoginResponseDTO(
                token,
                "Bearer"
        );
    }
}
