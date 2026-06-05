package com.fredfmelo.authservice.auth.security;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import org.springframework.stereotype.Component;

import com.fredfmelo.authservice.config.SecretsManagerService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RsaKeyProvider {

    private static final String PRIVATE_KEY_SECRET = "auth-service-private-key";
    private static final String PUBLIC_KEY_SECRET = "auth-service-public-key";

    private final SecretsManagerService secretsManagerService;

    public PrivateKey loadPrivateKey() throws Exception {
        String key = secretsManagerService.getSecret(PRIVATE_KEY_SECRET);

        key = key.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] decoded = Base64.getDecoder().decode(key);

        return KeyFactory.getInstance("RSA")
                .generatePrivate(new PKCS8EncodedKeySpec(decoded));
    }

    public PublicKey loadPublicKey() throws Exception {
        String key = secretsManagerService.getSecret(PUBLIC_KEY_SECRET);

        key = key.replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        byte[] decoded = Base64.getDecoder().decode(key);

        return KeyFactory.getInstance("RSA")
                .generatePublic(new X509EncodedKeySpec(decoded));
    }
}