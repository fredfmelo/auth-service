package com.fredfmelo.authservice.auth.security;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fredfmelo.authservice.config.SecretsManagerService;

@ExtendWith(MockitoExtension.class)
class RsaKeyProviderTest {

    @Mock
    private SecretsManagerService secretsManagerService;

    @InjectMocks
    private RsaKeyProvider rsaKeyProvider;

    private String privatePem;
    private String publicPem;

    @BeforeEach
    void setUp() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);

        KeyPair keyPair = generator.generateKeyPair();

        privatePem =
                "-----BEGIN PRIVATE KEY-----\n"
                        + Base64.getEncoder()
                                .encodeToString(keyPair.getPrivate().getEncoded())
                        + "\n-----END PRIVATE KEY-----";

        publicPem =
                "-----BEGIN PUBLIC KEY-----\n"
                        + Base64.getEncoder()
                                .encodeToString(keyPair.getPublic().getEncoded())
                        + "\n-----END PUBLIC KEY-----";
    }

    @Test
    void shouldLoadPrivateKey() throws Exception {
        when(secretsManagerService.getSecret("auth-service-private-key"))
                .thenReturn(privatePem);

        var privateKey = rsaKeyProvider.loadPrivateKey();

        assertThat(privateKey).isNotNull();
        assertThat(privateKey.getAlgorithm()).isEqualTo("RSA");
    }

    @Test
    void shouldLoadPublicKey() throws Exception {
        when(secretsManagerService.getSecret("auth-service-public-key"))
                .thenReturn(publicPem);

        var publicKey = rsaKeyProvider.loadPublicKey();

        assertThat(publicKey).isNotNull();
        assertThat(publicKey.getAlgorithm()).isEqualTo("RSA");
    }
}