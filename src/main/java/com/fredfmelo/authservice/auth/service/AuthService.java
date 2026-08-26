package com.fredfmelo.authservice.auth.service;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fredfmelo.authservice.auth.entity.Role;
import com.fredfmelo.authservice.auth.entity.UserEntity;
import com.fredfmelo.authservice.auth.event.UserCreatedEvent;
import com.fredfmelo.authservice.auth.repository.UserRepository;
import com.fredfmelo.authservice.auth.repository.UserTransactionRepository;
import com.fredfmelo.eventdrivencore.outbox.entity.OutboxEntity;
import com.fredfmelo.eventdrivencore.outbox.service.OutboxService;
import com.fredfmelo.authservice.auth.security.AuthenticatedUser;
import com.fredfmelo.authservice.auth.validation.PasswordValidator;
import com.fredfmelo.authservice.model.LoginRequest;
import com.fredfmelo.authservice.model.LoginResponse;
import com.fredfmelo.authservice.model.MeResponse;
import com.fredfmelo.authservice.model.RegisterRequest;
import com.fredfmelo.authservice.model.RegisterResponse;
import com.fredfmelo.authservice.model.RegisterResponse.RoleEnum;
import com.fredfmelo.eventdrivencore.exception.BusinessException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserTransactionRepository userTransactionRepository;
    private final OutboxService outboxService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final PasswordValidator passwordValidator;

    public RegisterResponse register(RegisterRequest request) {
        return registerUser(request, Role.CUSTOMER);
    }

    public RegisterResponse registerSeller(RegisterRequest request) {
        return registerUser(request, Role.SELLER);
    }

    private RegisterResponse registerUser(RegisterRequest request, Role role) {
        request.setEmail(request.getEmail().trim().toLowerCase());

        userRepository.findByEmail(request.getEmail())
                .ifPresent(user -> {
                    throw new BusinessException("User already exists with email: " + request.getEmail(),
                            HttpStatus.CONFLICT.value());
                });

        passwordValidator.validate(request.getPassword());

        UserEntity user = buildUser(request, role);

        UserCreatedEvent event = buildUserCreatedEvent(user);
        OutboxEntity outbox = outboxService.buildEntity(event);

        userTransactionRepository.save(user, outbox);

        return new RegisterResponse()
                .userId(user.getUserId())
                .email(user.getEmail())
                .role(RoleEnum.fromValue(role.name()))
                .createdAt(OffsetDateTime.ofInstant(user.getCreatedAt(), ZoneOffset.UTC));
    }

    public LoginResponse login(LoginRequest request) {
        request.setEmail(request.getEmail().trim().toLowerCase());

        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("User not found", HttpStatus.UNAUTHORIZED.value()));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException("Invalid credentials", HttpStatus.UNAUTHORIZED.value());
        }

        return new LoginResponse()
                .accessToken(jwtService.generateToken(user))
                .tokenType("Bearer")
                .expiresIn(3600);
    }

    public MeResponse me() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
    
        return new MeResponse()
                .userId(user.userId())
                .email(user.email())
                .role(user.role().name());
    }

    private UserEntity buildUser(RegisterRequest request, Role role) {

        UUID userId = UUID.randomUUID();

        UserEntity user = new UserEntity();

        user.setPk(UserEntity.USER_PREFIX + userId);
        user.setSk(UserEntity.PROFILE);

        user.setEmailPk(UserEntity.EMAIL_PREFIX + request.getEmail());
        user.setEmailSk(UserEntity.PROFILE);

        user.setUserId(userId);
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setCreatedAt(Instant.now());

        return user;
    }

    private UserCreatedEvent buildUserCreatedEvent(UserEntity user) {
        return new UserCreatedEvent(
                UUID.randomUUID(),
                UUID.randomUUID().toString(),
                "USER_CREATED",
                Instant.now(),
                user.getUserId());
    }

}