package com.fredfmelo.authservice.auth.controller;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.fredfmelo.authservice.auth.service.AuthService;
import com.fredfmelo.authservice.model.RegisterRequest;
import com.fredfmelo.authservice.model.RegisterResponse;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerImplTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthenticationControllerImpl controller;

    @Test
    void shouldRegisterUser() {
        RegisterRequest request = new RegisterRequest()
                .email("TEST@TEST.COM")
                .password("Password123");
    
        RegisterResponse response = new RegisterResponse()
                .email("test@test.com");
    
        when(authService.register(request)).thenReturn(response);
    
        ResponseEntity<RegisterResponse> result = controller.register(request);
    
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isEqualTo(response);
    
        verify(authService).register(request);
    }
}