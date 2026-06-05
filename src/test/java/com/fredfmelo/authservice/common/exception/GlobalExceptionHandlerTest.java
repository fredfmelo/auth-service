package com.fredfmelo.authservice.common.exception;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import com.fredfmelo.eventdrivencore.exception.BusinessException;
import com.fredfmelo.eventdrivencore.exception.TechnicalException;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void shouldHandleBusinessException() {
        BusinessException exception = new BusinessException("Business error", HttpStatus.CONFLICT.value());

        var response = handler.handleBusiness(exception);

        assertThat(response.getStatusCode().value())
                .isEqualTo(HttpStatus.CONFLICT.value());

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Business error");
        assertThat(response.getBody().status()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(response.getBody().timestamp()).isBeforeOrEqualTo(Instant.now());
    }

    @Test
    void shouldHandleTechnicalException() {
        TechnicalException exception = new TechnicalException("Technical error", HttpStatus.INTERNAL_SERVER_ERROR.value());

        var response = handler.handleTechnical(exception);

        assertThat(response.getStatusCode().value()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Technical error");
        assertThat(response.getBody().status()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(response.getBody().timestamp()).isBeforeOrEqualTo(Instant.now());
    }

    @Test
    void shouldHandleUnexpectedException() {
        Exception exception = new RuntimeException("Unexpected");

        var response = handler.handleUnexpected(exception);

        assertThat(response.getStatusCode().value()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Internal server error");
        assertThat(response.getBody().status()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(response.getBody().timestamp()).isBeforeOrEqualTo(Instant.now());
    }
}