package com.user.identity.validation.password;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PasswordValidationResultTest {

    @Test
    @DisplayName("Should create successful validation result")
    void test_01(){
        PasswordValidationResult result = PasswordValidationResult.success();

        assertThat(result.valid()).isTrue();
        assertThat(result.errors()).isEmpty();
    }
    @Test
    @DisplayName("Should create failed validation result")
    void test_02(){
        PasswordValidationResult result =
                PasswordValidationResult.failure(List.of("Password is to short"));

        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).containsExactly("Password is to short");
    }
    @Test
    @DisplayName("Should reject successful result with errors")
    void test_03(){
        assertThatThrownBy(() ->
                new PasswordValidationResult(
                        true, List.of("Invalid password")
                )).isInstanceOf(IllegalArgumentException.class);
    }
    @Test
    @DisplayName("Should reject failed result without errors")
    void test_04(){
        assertThatThrownBy(() ->
                new PasswordValidationResult(
                        false, List.of()
                )).isInstanceOf(IllegalArgumentException.class);
    }
    @Test
    @DisplayName("Should reject failed result without errors")
    void test_05(){
        assertThatThrownBy(() ->
                new PasswordValidationResult(
                        false, null
                )).isInstanceOf(NullPointerException.class);
    }
    @Test
    @DisplayName("Should reject empty failure")
    void test_06(){
        assertThatThrownBy(() ->
                PasswordValidationResult.failure(List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }
    @Test
    @DisplayName("Should prevent external modification of errors")
    void test_07(){
        List<String> errors =
                new ArrayList<>(List.of("Password is too short"));

        PasswordValidationResult result =
                PasswordValidationResult.failure(errors);

        errors.add("another error");

        assertThat(result.errors()).hasSize(1);
    }
}
