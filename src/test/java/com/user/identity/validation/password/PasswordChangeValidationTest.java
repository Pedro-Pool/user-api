package com.user.identity.validation.password;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PasswordChangeValidationTest {

    private PasswordChangeValidation validation;
    private PasswordLengthValidation passwordLengthValidation;

    @BeforeEach
    void setUp() {
        passwordLengthValidation = mock(PasswordLengthValidation.class);
        validation = new PasswordChangeValidation(passwordLengthValidation);
    }

    @Test
    @DisplayName("Should delegate valid password")
    void test_01() {
        String password = "StrongPassword@123";

        PasswordValidationResult expectedResult =
                PasswordValidationResult.success();

        when(passwordLengthValidation.validate(password))
                .thenReturn(expectedResult);

        PasswordValidationResult result = validation.validate(password);

        assertThat(result).isEqualTo(expectedResult);

        verify(passwordLengthValidation, times(1)).validate(password);
        verifyNoMoreInteractions(passwordLengthValidation);
    }
}