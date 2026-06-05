package com.fredfmelo.authservice.auth.validation;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;

import com.fredfmelo.eventdrivencore.exception.BusinessException;

class PasswordValidatorTest {

    private final PasswordValidator validator = new PasswordValidator();

    @Test
    void shouldAcceptValidPassword() {
        assertThatCode(() -> validator.validate("Password123"))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldRejectNullPassword() {
        assertThatThrownBy(() -> validator.validate(null))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Password must contain at least 8 characters");
    }

    @Test
    void shouldRejectPasswordShorterThanEightCharacters() {
        assertThatThrownBy(() -> validator.validate("Pass1"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Password must contain at least 8 characters");
    }

    @Test
    void shouldRejectPasswordWithoutUppercaseLetter() {
        assertThatThrownBy(() -> validator.validate("password123"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Password must contain at least one uppercase letter");
    }

    @Test
    void shouldRejectPasswordWithoutLowercaseLetter() {
        assertThatThrownBy(() -> validator.validate("PASSWORD123"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Password must contain at least one lowercase letter");
    }

    @Test
    void shouldRejectPasswordWithoutNumber() {
        assertThatThrownBy(() -> validator.validate("Password"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Password must contain at least one number");
    }

    @Test
    void shouldAcceptPasswordContainingSpecialCharacters() {
        assertThatCode(() -> validator.validate("Password123!"))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldAcceptPasswordExactlyEightCharactersLong() {
        assertThatCode(() -> validator.validate("Pass1234"))
                .doesNotThrowAnyException();
    }
}