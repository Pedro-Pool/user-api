package com.user.identity.validation.password;

import java.util.List;

public record PasswordValidationResult(boolean valid, List<String> errors) {
    public PasswordValidationResult{
        errors = List.copyOf(errors);

        if (valid && !errors.isEmpty()){
            throw new IllegalArgumentException(
                    "A valid password cannot contain validation errors"
            );
        }
        if (!valid && errors.isEmpty()){
            throw new IllegalArgumentException(
                    "A invalid password must contain validation errors"
            );
        }
    }
    public static PasswordValidationResult success(){
        return new PasswordValidationResult(true, List.of());
    }
    public static PasswordValidationResult failure(List<String> errors){
        if (errors == null || errors.isEmpty()){
            throw new IllegalArgumentException("Validation errors are required");
        }
        return new PasswordValidationResult(false, errors);
    }
}
