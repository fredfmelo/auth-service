package com.fredfmelo.authservice.common.exception;

import java.time.Instant;

public record ErrorResponse(
        Instant timestamp,
        Integer status,
        String message
) {
}