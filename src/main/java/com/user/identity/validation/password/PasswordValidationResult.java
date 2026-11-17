package com.user.identity.validation.password;

import com.user.identity.validation.ValidationResult;

import java.util.List;

public record PasswordValidationResult(ValidationResult result) {
    public PasswordValidationResult{
        if (result == null){
            throw new IllegalArgumentException(
                    "Validation result is required"
            );
        }
    }
    public boolean valid(){
        return result.valid();
    }
    public List<String> errors(){
        return result.errors();
    }

    public static PasswordValidationResult success(){
        return new PasswordValidationResult(ValidationResult.success());
    }
    public static PasswordValidationResult failure(List<String> errors){
        return new PasswordValidationResult(ValidationResult.failure(errors));
    }
}
