package com.user.identity.validation.password;

import org.springframework.stereotype.Component;

@Component
public class RegistrationPasswordValidation implements PasswordValidationStrategy{

    private final PasswordLengthValidation passwordLengthValidation;

    public RegistrationPasswordValidation(PasswordLengthValidation passwordLengthValidation){
        this.passwordLengthValidation = passwordLengthValidation;
    }

    @Override
    public PasswordValidationResult validate(String password){
        return passwordLengthValidation.validate(password);
    }
}
