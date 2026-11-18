package com.user.identity.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserProfileValidationExceptionTest {

    @Test
    @DisplayName("Should create exception with validation errors")
    void test_01(){
        List<String> errors = List.of("Display name cannot be blank");

        UserProfileValidationException exception = new UserProfileValidationException(errors);

        assertThat(exception.getMessage()).isEqualTo("User profile validation failed");
        assertThat(exception.getErrors()).containsExactly("Display name cannot be blank");
    }
    @Test
    @DisplayName("Should preserver multiples validations errors")
    void test_02(){
        List<String> errors = List.of(
                "Display name cannot be blank",
                "First name cannot be blank",
                "Last name must not exceed 120 characters"
        );

        UserProfileValidationException exception = new UserProfileValidationException(errors);

        assertThat(exception.getErrors()).hasSize(3).containsExactlyElementsOf(errors);
    }
    @Test
    @DisplayName("Should return immutable validations errors list")
    void test_03(){
        List<String> errors = List.of(
                "Display name cannot be blank",
                "First name cannot be blank"
        );

        UserProfileValidationException exception = new UserProfileValidationException(errors);

        assertThatThrownBy(() -> exception.getErrors().add("Another error"))
                .isInstanceOf(UnsupportedOperationException.class);
    }
    @Test
    @DisplayName("Should reject null validations errors")
    void test_04(){
        assertThatThrownBy(() -> new UserProfileValidationException(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Validation errors are required");
    }
    @Test
    @DisplayName("Should reject empty validations errors")
    void test_05(){
        assertThatThrownBy(() -> new UserProfileValidationException(List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Validation errors are required");
    }
}
