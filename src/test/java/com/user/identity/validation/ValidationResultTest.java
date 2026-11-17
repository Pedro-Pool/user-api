package com.user.identity.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValidationResultTest {

    @Test
    @DisplayName("Should create successful validation result")
    void test_01(){
        ValidationResult result = ValidationResult.success();

        assertThat(result.valid()).isTrue();
        assertThat(result.errors()).isEmpty();
    }
    @Test
    @DisplayName("Should create failed validation result with errors")
    void test_02(){
        ValidationResult result = ValidationResult.failure(
                List.of("Display name is required", "First name is required")
        );

        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).containsExactly("Display name is required", "First name is required");
    }
    @Test
    @DisplayName("Should prevent modification of validation errors")
    void test_03(){
        ValidationResult result = ValidationResult.failure(
                List.of("Display name is required")
        );

        assertThatThrownBy(() ->
                result.errors().add("Another error"))
                .isInstanceOf(UnsupportedOperationException.class);
    }
    @Test
    @DisplayName("Should reject inconsistent validation result states")
    void test_04(){
        assertThatThrownBy(() ->
                new ValidationResult(true, List.of("Unexpected error")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A valid result cannot contain validation errors");

        assertThatThrownBy(() ->
                new ValidationResult(false, List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("An invalid result must contain validation errors");
    }
    @Test
    @DisplayName("Should reject inconsistent validation result states")
    void test_05(){
        assertThatThrownBy(() ->
                ValidationResult.failure(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Validation errors are required");

        assertThatThrownBy(() ->
                ValidationResult.failure(List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Validation errors are required");
    }
}
