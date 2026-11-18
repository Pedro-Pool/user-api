package com.user.identity.validation.user;

import com.user.identity.dto.user.UserRegistrationRequest;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserRegistrationValidationTest {

    private ValidatorFactory factory;
    private UserRegistrationValidation validation;

    @BeforeEach
    void setUp(){
        factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        validation = new UserRegistrationValidation(validator);
    }
    @AfterEach
    void teatDown(){
        factory.close();
    }

    private UserRegistrationRequest validRequest(){
        return new UserRegistrationRequest(
                "John Doe",
                "John",
                "Doe",
                "john.doe@example.com",
                "SecurePassword123"
        );
    }

    @Test
    @DisplayName("Should accept a valid registration request")
    void test_01(){
        UserRegistrationRequest request = validRequest();

        var result = validation.validate(request);

        assertThat(result.valid()).isTrue();
        assertThat(result.errors()).isEmpty();
    }
    @Test
    @DisplayName("Should reject a null registration request")
    void test_02(){
        var result = validation.validate(null);

        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).contains("User registration request is required");
    }
    @Test
    @DisplayName("Should reject a blank display name")
    void test_03(){
        UserRegistrationRequest request = new UserRegistrationRequest(
                " ",
                "John",
                "Doe",
                "john.doe@example.com",
                "SecurePassword123"
        );

        var result = validation.validate(request);

        assertThat(result.valid()).isFalse();
    }
    @Test
    @DisplayName("Should reject a blank first name")
    void test_04(){
        UserRegistrationRequest request = new UserRegistrationRequest(
                "John Doe",
                " ",
                "Doe",
                "john.doe@example.com",
                "SecurePassword123"
        );

        var result = validation.validate(request);

        assertThat(result.valid()).isFalse();
    }
    @Test
    @DisplayName("Should reject a blank last name")
    void test_05(){
        UserRegistrationRequest request = new UserRegistrationRequest(
                "John Doe",
                "John",
                " ",
                "john.doe@example.com",
                "SecurePassword123"
        );

        var result = validation.validate(request);

        assertThat(result.valid()).isFalse();
    }
    @Test
    @DisplayName("Should reject a blank email")
    void test_06(){
        UserRegistrationRequest request = new UserRegistrationRequest(
                "John Doe",
                "John",
                "Doe",
                " ",
                "SecurePassword123"
        );

        var result = validation.validate(request);

        assertThat(result.valid()).isFalse();
    }
    @Test
    @DisplayName("Should reject an invalid email")
    void test_07(){
        UserRegistrationRequest request = new UserRegistrationRequest(
                "John Doe",
                "John",
                "Doe",
                "invalid email",
                "SecurePassword123"
        );

        var result = validation.validate(request);

        assertThat(result.valid()).isFalse();
    }
    @Test
    @DisplayName("Should reject an email longer than 254 characters")
    void test_08(){
        String longEmail = "a".repeat(255);

        UserRegistrationRequest request = new UserRegistrationRequest(
                "John Doe",
                "John",
                "Doe",
                longEmail,
                "SecurePassword123"
        );

        var result = validation.validate(request);

        assertThat(result.valid()).isFalse();
    }
    @Test
    @DisplayName("Should reject a blank password")
    void test_09(){
        UserRegistrationRequest request = new UserRegistrationRequest(
                "John Doe",
                "John",
                "Doe",
                "john.doe@example.com",
                " "
        );

        var result = validation.validate(request);

        assertThat(result.valid()).isFalse();
    }
    @Test
    @DisplayName("Should reject multiple invalid fields")
    void test_10(){
        UserRegistrationRequest request = new UserRegistrationRequest(
                " ",
                " ",
                " ",
                "invalid-email",
                " "
        );

        var result = validation.validate(request);

        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).hasSizeGreaterThan(1);
    }
}
