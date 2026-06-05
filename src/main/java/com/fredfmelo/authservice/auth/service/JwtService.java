package com.fredfmelo.authservice.auth.service;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.fredfmelo.authservice.auth.entity.Role;
import com.fredfmelo.authservice.auth.entity.UserEntity;
import com.fredfmelo.authservice.auth.security.AuthenticatedUser;
import com.fredfmelo.authservice.auth.security.RsaKeyProvider;
import com.fredfmelo.authservice.config.ServiceConfig;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final ServiceConfig serviceConfig;
    private final RsaKeyProvider rsaKeyProvider;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    @PostConstruct
    void init() throws Exception {
        privateKey = rsaKeyProvider.loadPrivateKey();
        publicKey = rsaKeyProvider.loadPublicKey();
    }

    public String generateToken(UserEntity user) {
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(user.getUserId().toString())
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(
                        now.plusSeconds(serviceConfig.getJwt().getExpirationSeconds())))
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
        } catch (Exception ex) {
            return false;
        }
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(extractClaims(token).getSubject());
    }

    public String extractEmail(String token) {
        return extractClaims(token).get("email", String.class);
    }

    public Role extractRole(String token) {
        return Role.valueOf(extractClaims(token).get("role", String.class));
    }

    public AuthenticatedUser extractUser(String token) {
        Claims claims = extractClaims(token);

        return new AuthenticatedUser(
                UUID.fromString(claims.getSubject()),
                claims.get("email", String.class),
                Role.valueOf(claims.get("role", String.class)));
    }
}