package com.user.identity.validation.password;

import org.springframework.stereotype.Component;

@Component
public class PasswordChangeValidation implements PasswordValidationStrategy{

    private final PasswordLengthValidation passwordLengthValidation;

    public PasswordChangeValidation(PasswordLengthValidation passwordLengthValidation) {
        this.passwordLengthValidation = passwordLengthValidation;
    }

    @Override
    public PasswordValidationResult validate(String password){
        return passwordLengthValidation.validate(password);
    }
}
