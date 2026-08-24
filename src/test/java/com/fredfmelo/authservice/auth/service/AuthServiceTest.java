package com.fredfmelo.authservice.auth.service;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.fredfmelo.authservice.auth.entity.Role;
import com.fredfmelo.authservice.auth.entity.UserEntity;
import com.fredfmelo.authservice.auth.repository.UserRepository;
import com.fredfmelo.authservice.auth.repository.UserTransactionRepository;
import com.fredfmelo.authservice.auth.security.AuthenticatedUser;
import com.fredfmelo.authservice.auth.validation.PasswordValidator;
import com.fredfmelo.eventdrivencore.outbox.entity.OutboxEntity;
import com.fredfmelo.eventdrivencore.outbox.service.OutboxService;
import com.fredfmelo.authservice.model.LoginRequest;
import com.fredfmelo.authservice.model.LoginResponse;
import com.fredfmelo.authservice.model.MeResponse;
import com.fredfmelo.authservice.model.RegisterRequest;
import com.fredfmelo.authservice.model.RegisterResponse;
import com.fredfmelo.eventdrivencore.exception.BusinessException;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserTransactionRepository userTransactionRepository;

    @Mock
    private OutboxService outboxService;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PasswordValidator passwordValidator;

    @InjectMocks
    private AuthService authService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldRegisterUser() {
        RegisterRequest request = new RegisterRequest()
                .email("TEST@TEST.COM")
                .password("Password123");

        when(userRepository.findByEmail("test@test.com"))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode("Password123"))
                .thenReturn("hashed-password");

        when(outboxService.buildEntity(org.mockito.ArgumentMatchers.any()))
                .thenReturn(new OutboxEntity());

        RegisterResponse response = authService.register(request);

        assertThat(response.getEmail()).isEqualTo("test@test.com");
        assertThat(response.getRole()).isEqualTo(RegisterResponse.RoleEnum.CUSTOMER);

        verify(passwordValidator).validate("Password123");

        ArgumentCaptor<UserEntity> captor =
                ArgumentCaptor.forClass(UserEntity.class);

        verify(userTransactionRepository).save(captor.capture(), org.mockito.ArgumentMatchers.any(OutboxEntity.class));

        UserEntity savedUser = captor.getValue();

        assertThat(savedUser.getPk())
                .startsWith(UserEntity.USER_PREFIX);

        assertThat(savedUser.getSk())
                .isEqualTo(UserEntity.PROFILE);

        assertThat(savedUser.getEmailPk())
                .isEqualTo(UserEntity.EMAIL_PREFIX + "test@test.com");

        assertThat(savedUser.getEmailSk())
                .isEqualTo(UserEntity.PROFILE);

        assertThat(savedUser.getUserId())
                .isNotNull();

        assertThat(savedUser.getEmail())
                .isEqualTo("test@test.com");

        assertThat(savedUser.getPasswordHash())
                .isEqualTo("hashed-password");

        assertThat(savedUser.getRole())
                .isEqualTo(Role.CUSTOMER);

        assertThat(savedUser.getCreatedAt())
                .isNotNull();
    }

    @Test
    void shouldThrowWhenUserAlreadyExists() {
        RegisterRequest request = new RegisterRequest()
                .email("test@test.com")
                .password("Password123");

        when(userRepository.findByEmail("test@test.com"))
                .thenReturn(Optional.of(new UserEntity()));

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BusinessException.class);

        verify(userRepository).findByEmail("test@test.com");
    }

    @Test
    void shouldLoginSuccessfully() {
        LoginRequest request = new LoginRequest()
                .email("test@test.com")
                .password("Password123");

        UserEntity user = new UserEntity();
        user.setPasswordHash("hash");

        when(userRepository.findByEmail("test@test.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("Password123", "hash"))
                .thenReturn(true);

        when(jwtService.generateToken(user))
                .thenReturn("jwt-token");

        LoginResponse response = authService.login(request);

        assertThat(response.getAccessToken()).isEqualTo("jwt-token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getExpiresIn()).isEqualTo(3600);
    }

    @Test
    void shouldNormalizeEmailOnLogin() {
        LoginRequest request = new LoginRequest()
                .email("TEST@TEST.COM")
                .password("Password123");

        UserEntity user = new UserEntity();
        user.setPasswordHash("hash");

        when(userRepository.findByEmail("test@test.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("Password123", "hash"))
                .thenReturn(true);

        when(jwtService.generateToken(user))
                .thenReturn("jwt-token");

        authService.login(request);

        assertThat(request.getEmail())
                .isEqualTo("test@test.com");
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        LoginRequest request = new LoginRequest()
                .email("test@test.com")
                .password("Password123");

        when(userRepository.findByEmail("test@test.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void shouldThrowWhenPasswordIsInvalid() {
        LoginRequest request = new LoginRequest()
                .email("test@test.com")
                .password("Password123");

        UserEntity user = new UserEntity();
        user.setPasswordHash("hash");

        when(userRepository.findByEmail("test@test.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("Password123", "hash"))
                .thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void shouldReturnCurrentAuthenticatedUser() {
        AuthenticatedUser user = new AuthenticatedUser(
                UUID.randomUUID(),
                "test@test.com",
                Role.CUSTOMER);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null));

        MeResponse response = authService.me();

        assertThat(response.getEmail()).isEqualTo("test@test.com");
        assertThat(response.getRole()).isEqualTo("CUSTOMER");
        assertThat(response.getUserId()).isEqualTo(user.userId());
    }
}