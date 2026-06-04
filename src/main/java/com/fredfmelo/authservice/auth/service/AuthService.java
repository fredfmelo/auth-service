package com.fredfmelo.authservice.auth.service;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fredfmelo.authservice.auth.entity.Role;
import com.fredfmelo.authservice.auth.entity.UserEntity;
import com.fredfmelo.authservice.auth.repository.UserRepository;
import com.fredfmelo.authservice.model.LoginRequest;
import com.fredfmelo.authservice.model.LoginResponse;
import com.fredfmelo.authservice.model.RegisterRequest;
import com.fredfmelo.authservice.model.RegisterResponse;
import com.fredfmelo.eventdrivencore.exception.BusinessException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public RegisterResponse register(RegisterRequest request) {
        userRepository.findByEmail(request.getEmail())
                .ifPresent(user -> {
                    throw new BusinessException("User already exists with email: " + request.getEmail(),
                            HttpStatus.CONFLICT.value());
                });

        UserEntity user = buildUser(request);

        userRepository.save(user);

        return new RegisterResponse()
                .userId(user.getUserId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .createdAt(OffsetDateTime.ofInstant(user.getCreatedAt(), ZoneOffset.UTC));
    }

    public LoginResponse login(LoginRequest request) {
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("User not found", HttpStatus.UNAUTHORIZED.value()));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException("Invalid credentials", HttpStatus.UNAUTHORIZED.value());}

        return new LoginResponse()
                .accessToken(jwtService.generateToken(user))
                .tokenType("Bearer")
                .expiresIn(3600);
    }

    private UserEntity buildUser(RegisterRequest request) {

        UUID userId = UUID.randomUUID();

        UserEntity user = new UserEntity();

        user.setPk(UserEntity.USER_PREFIX + userId);
        user.setSk(UserEntity.PROFILE);

        user.setEmailPk(UserEntity.EMAIL_PREFIX + request.getEmail());
        user.setEmailSk(UserEntity.PROFILE);

        user.setUserId(userId);
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.CUSTOMER);
        user.setCreatedAt(Instant.now());

        return user;
    }
}