package com.fredfmelo.authservice.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.fredfmelo.authservice.api.AuthenticationApi;
import com.fredfmelo.authservice.auth.service.AuthService;
import com.fredfmelo.authservice.model.LoginRequest;
import com.fredfmelo.authservice.model.LoginResponse;
import com.fredfmelo.authservice.model.MeResponse;
import com.fredfmelo.authservice.model.RegisterRequest;
import com.fredfmelo.authservice.model.RegisterResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequiredArgsConstructor
public class AuthenticationControllerImpl implements AuthenticationApi {

    private final AuthService authService;

    @Override
    public ResponseEntity<RegisterResponse> register(RegisterRequest registerRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.register(registerRequest));
    }

    @Override
    public ResponseEntity<LoginResponse> login(LoginRequest loginRequest) {
        loginRequest.setEmail(loginRequest.getEmail().trim().toLowerCase());

        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @Override
    public ResponseEntity<MeResponse> me(){
        return ResponseEntity.ok(authService.me());
    }

}