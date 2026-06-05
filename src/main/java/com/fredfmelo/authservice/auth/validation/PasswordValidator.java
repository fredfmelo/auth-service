package com.fredfmelo.authservice.auth.validation;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.fredfmelo.eventdrivencore.exception.BusinessException;

@Component
public class PasswordValidator {

    private static final int MIN_LENGTH = 8;

    public void validate(String password) {

        int badRequest = HttpStatus.BAD_REQUEST.value();

        if (password == null || password.length() < MIN_LENGTH) {
            throw new BusinessException("Password must contain at least 8 characters", badRequest);
        }

        if (!containsUppercase(password)) {
            throw new BusinessException("Password must contain at least one uppercase letter", badRequest);
        }

        if (!containsLowercase(password)) {
            throw new BusinessException("Password must contain at least one lowercase letter", badRequest);
        }

        if (!containsDigit(password)) {
            throw new BusinessException("Password must contain at least one number", badRequest);
        }
    }

    private boolean containsUppercase(String password) {
        return password.chars().anyMatch(Character::isUpperCase);
    }

    private boolean containsLowercase(String password) {
        return password.chars().anyMatch(Character::isLowerCase);
    }

    private boolean containsDigit(String password) {
        return password.chars().anyMatch(Character::isDigit);
    }
}