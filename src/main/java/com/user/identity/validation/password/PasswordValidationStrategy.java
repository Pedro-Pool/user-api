package com.user.identity.validation.password;

public interface PasswordValidationStrategy {
    PasswordValidationResult validate(String password);
}
