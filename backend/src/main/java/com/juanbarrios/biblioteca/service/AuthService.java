package com.juanbarrios.biblioteca.service;

import com.juanbarrios.biblioteca.domain.AppUser;
import com.juanbarrios.biblioteca.domain.Role;
import com.juanbarrios.biblioteca.exception.DuplicateEmailException;
import com.juanbarrios.biblioteca.repository.AppUserRepository;
import com.juanbarrios.biblioteca.security.AppUserPrincipal;
import com.juanbarrios.biblioteca.security.JwtService;
import com.juanbarrios.biblioteca.web.dto.AuthResponse;
import com.juanbarrios.biblioteca.web.dto.LoginRequest;
import com.juanbarrios.biblioteca.web.dto.RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (appUserRepository.existsByEmailIgnoreCase(request.email())) {
            throw new DuplicateEmailException(request.email());
        }

        AppUser user = AppUser.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.BIBLIOTECARIO)
                .blocked(false)
                .build();

        appUserRepository.save(user);
        return toResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        AppUser user = ((AppUserPrincipal) authentication.getPrincipal()).getUser();
        return toResponse(user);
    }

    private AuthResponse toResponse(AppUser user) {
        String token = jwtService.generateToken(user);
        return new AuthResponse(token, user.getName(), user.getEmail(), user.getRole().name());
    }
}
