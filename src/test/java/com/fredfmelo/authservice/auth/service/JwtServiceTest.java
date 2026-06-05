package com.fredfmelo.authservice.auth.service;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fredfmelo.authservice.auth.entity.Role;
import com.fredfmelo.authservice.auth.entity.UserEntity;
import com.fredfmelo.authservice.auth.security.AuthenticatedUser;
import com.fredfmelo.authservice.auth.security.RsaKeyProvider;
import com.fredfmelo.authservice.config.ServiceConfig;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @Mock
    private ServiceConfig serviceConfig;

    @Mock
    private ServiceConfig.Jwt jwtConfig;

    @Mock
    private RsaKeyProvider rsaKeyProvider;

    @InjectMocks
    private JwtService jwtService;

    @BeforeEach
    void setUp() throws Exception {

        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);

        KeyPair keyPair = generator.generateKeyPair();

        when(rsaKeyProvider.loadPrivateKey())
                .thenReturn(keyPair.getPrivate());

        when(rsaKeyProvider.loadPublicKey())
                .thenReturn(keyPair.getPublic());

        jwtService.init();
    }

    @Test
    void shouldGenerateAndValidateToken() {
        mockJwtConfig();

        UserEntity user = buildUser();

        String token = jwtService.generateToken(user);

        assertThat(token).isNotBlank();
        assertThat(jwtService.isValid(token)).isTrue();
        assertThat(jwtService.extractUserId(token))
                .isEqualTo(user.getUserId());
        assertThat(jwtService.extractEmail(token))
                .isEqualTo(user.getEmail());
        assertThat(jwtService.extractRole(token))
                .isEqualTo(user.getRole());
    }

    @Test
    void shouldExtractAuthenticatedUser() {
        mockJwtConfig();

        UserEntity user = buildUser();

        String token = jwtService.generateToken(user);

        AuthenticatedUser authenticatedUser =
                jwtService.extractUser(token);

        assertThat(authenticatedUser.userId())
                .isEqualTo(user.getUserId());

        assertThat(authenticatedUser.email())
                .isEqualTo(user.getEmail());

        assertThat(authenticatedUser.role())
                .isEqualTo(user.getRole());
    }

    @Test
    void shouldReturnFalseForInvalidToken() {
        assertThat(jwtService.isValid("invalid-token"))
                .isFalse();
    }

    @Test
    void shouldInitializeKeysOnStartup() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);

        KeyPair keyPair = generator.generateKeyPair();

        when(rsaKeyProvider.loadPrivateKey())
                .thenReturn(keyPair.getPrivate());

        when(rsaKeyProvider.loadPublicKey())
                .thenReturn(keyPair.getPublic());

        JwtService service = new JwtService(serviceConfig, rsaKeyProvider);

        service.init();

        UserEntity user = buildUser();

        mockJwtConfig();

        String token = service.generateToken(user);

        assertThat(service.isValid(token)).isTrue();
    }

    private void mockJwtConfig() {
        when(serviceConfig.getJwt()).thenReturn(jwtConfig);
        when(jwtConfig.getExpirationSeconds()).thenReturn(3600L);
    }

    private UserEntity buildUser() {
        UserEntity user = new UserEntity();

        user.setUserId(UUID.randomUUID());
        user.setEmail("test@test.com");
        user.setRole(Role.CUSTOMER);

        return user;
    }
}