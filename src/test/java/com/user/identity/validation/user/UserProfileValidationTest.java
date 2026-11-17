package com.user.identity.validation.user;

import com.user.identity.dto.user.UserProfileUpdateRequest;
import com.user.identity.validation.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserProfileValidationTest {

    private UserProfileValidation validation;

    @BeforeEach
    void setUp(){
        validation = new UserProfileValidation();
    }

    @Test
    @DisplayName("Should return invalid result when request is null")
    void test_01(){
        ValidationResult result = validation.validate(null);

        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).isNotEmpty();
    }
    @Test
    @DisplayName("Should return invalid result when all fields are null")
    void test_02(){
        UserProfileUpdateRequest request = new UserProfileUpdateRequest(null, null, null);

        ValidationResult result = validation.validate(request);

        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).isNotEmpty();
    }
    @Test
    @DisplayName("Should return invalid result when display name is empty")
    void test_03(){
        UserProfileUpdateRequest request = new UserProfileUpdateRequest("", null, null);

        ValidationResult result = validation.validate(request);

        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).isNotEmpty();
    }
    @Test
    @DisplayName("Should accept display name with exactly 120 characters")
    void test_04(){
        String displayName = "A".repeat(120);

        UserProfileUpdateRequest request = new UserProfileUpdateRequest(displayName, null, null);

        ValidationResult result = validation.validate(request);

        assertThat(result.valid()).isTrue();
        assertThat(result.errors()).isEmpty();
    }
    @Test
    @DisplayName("Should reject display name with more than 120 characters")
    void test_05(){
        String displayName = "A".repeat(121);

        UserProfileUpdateRequest request = new UserProfileUpdateRequest(displayName, null, null);

        ValidationResult result = validation.validate(request);

        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).isNotEmpty();
    }
    @Test
    @DisplayName("Should return invalid result when first name is empty")
    void test_06(){
        UserProfileUpdateRequest request = new UserProfileUpdateRequest(null, "", null);

        ValidationResult result = validation.validate(request);

        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).isNotEmpty();
    }
    @Test
    @DisplayName("Should accept first name with exactly 80 characters")
    void test_07(){
        String firstName = "A".repeat(80);

        UserProfileUpdateRequest request = new UserProfileUpdateRequest(null, firstName, null);

        ValidationResult result = validation.validate(request);

        assertThat(result.valid()).isTrue();
        assertThat(result.errors()).isEmpty();
    }
    @Test
    @DisplayName("Should reject first name with more than 80 characters")
    void test_08(){
        String firstName = "A".repeat(81);

        UserProfileUpdateRequest request = new UserProfileUpdateRequest(null, firstName, null);

        ValidationResult result = validation.validate(request);

        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).isNotEmpty();
    }
    @Test
    @DisplayName("Should return invalid result when last name is empty")
    void test_09(){
        UserProfileUpdateRequest request = new UserProfileUpdateRequest(null, null, "");

        ValidationResult result = validation.validate(request);

        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).isNotEmpty();
    }
    @Test
    @DisplayName("Should accept last name with exactly 120 characters")
    void test_10(){
        String lastName = "A".repeat(120);

        UserProfileUpdateRequest request = new UserProfileUpdateRequest(null, null, lastName);

        ValidationResult result = validation.validate(request);

        assertThat(result.valid()).isTrue();
        assertThat(result.errors()).isEmpty();
    }
    @Test
    @DisplayName("Should reject last name with more than 120 characters")
    void test_11(){
        String lastName = "A".repeat(121);

        UserProfileUpdateRequest request = new UserProfileUpdateRequest(null, null, lastName);

        ValidationResult result = validation.validate(request);

        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).isNotEmpty();
    }
    @Test
    @DisplayName("Should reject display name containing only whitespace")
    void test_12(){
        UserProfileUpdateRequest request = new UserProfileUpdateRequest("  ", null, null);

        ValidationResult result = validation.validate(request);

        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).isNotEmpty();
    }
    @Test
    @DisplayName("Should reject first name containing only whitespace")
    void test_13(){
        UserProfileUpdateRequest request = new UserProfileUpdateRequest(null, "  ", null);

        ValidationResult result = validation.validate(request);

        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).isNotEmpty();
    }
    @Test
    @DisplayName("Should reject last name containing only whitespace")
    void test_14(){
        UserProfileUpdateRequest request = new UserProfileUpdateRequest(null, null, "  ");

        ValidationResult result = validation.validate(request);

        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).isNotEmpty();
    }
}
