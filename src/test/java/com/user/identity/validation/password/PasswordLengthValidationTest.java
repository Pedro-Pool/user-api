package com.user.identity.validation.password;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordLengthValidationTest {

    private PasswordLengthValidation validation;

    @BeforeEach
    void setUp(){
        validation = new PasswordLengthValidation();
    }

    @Test
    @DisplayName("Should accept valid password")
    void test_01(){
        PasswordValidationResult result =
                validation.validate("StrongPassword@123");

        assertThat(result.valid()).isTrue();
        assertThat(result.errors()).isEmpty();
    }
    @Test
    @DisplayName("Should reject null password")
    void test_02(){
        PasswordValidationResult result =
                validation.validate(null);

        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).containsExactly("Password is required");
    }
    @Test
    @DisplayName("Should reject empty password")
    void test_03(){
        PasswordValidationResult result =
                validation.validate("");

        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).containsExactly("Password is required");
    }
    @Test
    @DisplayName("Should reject password shorter than minimum")
    void test_04(){
        PasswordValidationResult result =
                validation.validate("Short123");

        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).hasSize(1);
    }
    @Test
    @DisplayName("Should reject password shorter than minimum")
    void test_05(){
        PasswordValidationResult result =
                validation.validate("A".repeat(129));

        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).hasSize(1);
    }
    @Test
    @DisplayName("Should accept password with exactly minimum length")
    void test_06(){
        PasswordValidationResult result =
                validation.validate("A".repeat(15));

        assertThat(result.valid()).isTrue();
    }
    @Test
    @DisplayName("Should accept password with exactly maximum length")
    void test_07(){
        PasswordValidationResult result =
                validation.validate("A".repeat(128));

        assertThat(result.valid()).isTrue();
    }
    @Test
    @DisplayName("Should accept password with exactly maximum length")
    void test_08(){
        PasswordValidationResult result =
                validation.validate("My secure pass phrase");

        assertThat(result.valid()).isTrue();
    }
    @Test
    @DisplayName("Should reject password with multiple violations")
    void test_09(){
        PasswordValidationResult result =
                validation.validate("short");

        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).hasSize(1);
    }
}
