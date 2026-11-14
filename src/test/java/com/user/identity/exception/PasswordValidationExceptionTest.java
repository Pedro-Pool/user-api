package com.user.identity.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PasswordValidationExceptionTest {

    @Test
    @DisplayName("Should create exception with validation errors")
    void test_01(){
        List<String> errors = List.of(
                "Password must contain as least 15 characters",
                "Password must contain as special character"
        );

        PasswordValidationException exception = new PasswordValidationException(errors);

        assertThat(exception.getMessage()).isEqualTo("Password validation failed");
        assertThat(exception.getErrors()).containsExactlyElementsOf(errors);
    }
    @Test
    @DisplayName("Should return an immutable list of errors")
    void test_02(){
        PasswordValidationException exception = new PasswordValidationException(List.of(
                "Password is too short"
        ));

        assertThatThrownBy(() ->
                exception.getErrors().add("Another error"))
                .isInstanceOf(UnsupportedOperationException.class);
    }
    @Test
    @DisplayName("Should reject null validation errors")
    void test_03(){
        assertThatThrownBy(() ->
                new PasswordValidationException(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Validation errors are required");
    }
    @Test
    @DisplayName("Should reject empty validation errors")
    void test_04(){
        assertThatThrownBy(() ->
                new PasswordValidationException(List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Validation errors are required");
    }
}
