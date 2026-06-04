package com.fredfmelo.authservice.auth.service;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

import org.springframework.stereotype.Service;

import com.fredfmelo.authservice.auth.entity.UserEntity;
import com.fredfmelo.authservice.config.SecretsManagerService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtService {

    private static final long EXPIRATION_SECONDS = 3600;

    private static final String PRIVATE_KEY_SECRET = "auth-service-private-key";
    private static final String PUBLIC_KEY_SECRET = "auth-service-public-key";

    private final SecretsManagerService secretsManagerService;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    @PostConstruct
    void init() throws Exception {
        privateKey = loadPrivateKey();
        publicKey = loadPublicKey();
    }

    public String generateToken(UserEntity user) {
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(user.getUserId().toString())
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(EXPIRATION_SECONDS)))
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isValid(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (SignatureException ex) {
            return false;
        } catch (Exception ex) {
            return false;
        }
    }

    private PrivateKey loadPrivateKey() throws Exception {
        String key = secretsManagerService.getSecret(PRIVATE_KEY_SECRET);

        key = key.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] decoded = Base64.getDecoder().decode(key);

        return KeyFactory.getInstance("RSA")
                .generatePrivate(new PKCS8EncodedKeySpec(decoded));
    }

    private PublicKey loadPublicKey() throws Exception {
        String key = secretsManagerService.getSecret(PUBLIC_KEY_SECRET);

        key = key.replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        byte[] decoded = Base64.getDecoder().decode(key);

        return KeyFactory.getInstance("RSA")
                .generatePublic(new X509EncodedKeySpec(decoded));
    }
}