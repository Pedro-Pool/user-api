package com.user.identity.dto.user;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserRegistrationRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp(){
        validator = Validation
                .buildDefaultValidatorFactory()
                .getValidator();
    }

    @Test
    @DisplayName("Should accept valid registration data")
    void test_01(){
        UserRegistrationRequest request = new UserRegistrationRequest(
                "Pedro",
                "Pedro",
                "Silva",
                "pedro@example.com",
                "Strong@Password123!"
        );

        assertThat(validator.validate(request)).isEmpty();
    }
    @Test
    @DisplayName("Should reject null, empty and blank required fields")
    void test_02(){
        UserRegistrationRequest nullFields = new UserRegistrationRequest(
                null, null, null, null, null
        );

        UserRegistrationRequest emptyFields = new UserRegistrationRequest(
                "", "", "", "", ""
        );

        UserRegistrationRequest blankFields = new UserRegistrationRequest(
                "  ", "  ", "  ", "  ", "   "
        );

        assertThat(validator.validate(nullFields))
                .filteredOn(violation ->
                        violation.getConstraintDescriptor().getAnnotation() instanceof NotBlank)
                .extracting(violation -> violation.getPropertyPath().toString())
                        .containsExactlyInAnyOrder(
                                "displayName", "firstName", "lastName", "email", "password"
                        );

        assertThat(validator.validate(emptyFields))
                .filteredOn(violation ->
                        violation.getConstraintDescriptor().getAnnotation() instanceof NotBlank)
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactlyInAnyOrder(
                        "displayName", "firstName", "lastName", "email", "password"
                );

        assertThat(validator.validate(blankFields))
                .filteredOn(violation ->
                        violation.getConstraintDescriptor().getAnnotation() instanceof NotBlank)
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactlyInAnyOrder(
                        "displayName", "firstName", "lastName", "email", "password"
                );
    }
    @Test
    @DisplayName("Should reject names exceeding maximum length")
    void test_03() {
        UserRegistrationRequest request = new UserRegistrationRequest(
                "A".repeat(121),
                "A".repeat(81),
                "A".repeat(121),
                "pedro@example.com",
                "Strong@Password123!"
        );

        assertThat(validator.validate(request)).hasSize(3);
    }
    @Test
    @DisplayName("Should reject invalid email format and excessive length")
    void test_04() {
        UserRegistrationRequest invalidFormat = new UserRegistrationRequest(
                "Pedro",
                "Pedro",
                "Silva",
                "invalid-email",
                "Strong@Password123!"
        );

        String longEmail = "a".repeat(263) + "@example.com";

        UserRegistrationRequest exessiveLength = new UserRegistrationRequest(
                "Pedro",
                "Pedro",
                "Silva",
                longEmail,
                "Strong@Password123!"
        );

        assertThat(validator.validate(invalidFormat)).hasSize(1);
        assertThat(validator.validate(exessiveLength))
                .anySatisfy(violation -> {
                    assertThat(violation.getPropertyPath().toString())
                            .isEqualTo("email");
                    assertThat(violation.getMessage())
                            .isEqualTo("Email must not exceed 254 characters");
                });
    }
    @Test
    @DisplayName("Should accept names and email at maximum allowed length")
    void test_05() {
        String maxLengthEmail =
                "a".repeat(64) + "@"
                        + "b".repeat(63) + "."
                        + "c".repeat(63) + "."
                        + "d".repeat(61);

        UserRegistrationRequest request = new UserRegistrationRequest(
                "A".repeat(120),
                "A".repeat(80),
                "A".repeat(120),
                maxLengthEmail,
                "Strong@Password123!"
        );

        assertThat(validator.validate(request)).isEmpty();
    }
}
