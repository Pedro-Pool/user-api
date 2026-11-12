package com.user.identity.validation.password;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class RegistrationPasswordValidationTest {

    private RegistrationPasswordValidation validation;
    private PasswordLengthValidation passwordLengthValidation;


    @BeforeEach
    void setUp(){
        passwordLengthValidation = mock(PasswordLengthValidation.class);
        validation = new RegistrationPasswordValidation(passwordLengthValidation);
    }

    @Test
    @DisplayName("Should delegate valid password")
    void test_01(){
        String password = "StrongPassword@123";

        PasswordValidationResult expectResult =
                PasswordValidationResult.success();

        when(passwordLengthValidation.validate(password)).thenReturn(expectResult);

        PasswordValidationResult result =
                validation.validate(password);

        assertThat(result).isEqualTo(expectResult);

        verify(passwordLengthValidation, times(1)).validate(password);
        verifyNoMoreInteractions(passwordLengthValidation);

    }

}
