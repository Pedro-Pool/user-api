package com.user.identity.validation.password;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PasswordLengthValidation{

    private static final int MIN_LENGTH = 15;
    private static final int MAX_LENGTH = 128;

    public PasswordValidationResult validate(String password){
        List<String> errors = new ArrayList<>();

        if (password == null || password.isEmpty()){
            errors.add("Password is required");

            return PasswordValidationResult.failure(errors);
        }

        if (password.length() < MIN_LENGTH){
            errors.add("Password must contain at most "+ MIN_LENGTH +" characters");
        }
        if (password.length() > MAX_LENGTH){
            errors.add("Password must contain at most "+ MAX_LENGTH +" characters");
        }

        if (!errors.isEmpty()){
            return PasswordValidationResult.failure(errors);
        }

        return PasswordValidationResult.success();
    }
}
