package com.fredfmelo.authservice.auth.security;

import java.util.UUID;

import com.fredfmelo.authservice.auth.entity.Role;

public record AuthenticatedUser(
        UUID userId,
        String email,
        Role role) {
}