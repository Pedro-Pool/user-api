package com.user.identity.exception;

import java.util.List;

public class PasswordValidationException extends RuntimeException{

    private final List<String> errors;

    public PasswordValidationException(List<String> errors) {
        super("Password validation failed");

        if (errors == null || errors.isEmpty()){
            throw new IllegalArgumentException("Validation errors are required");
        }

        this.errors = List.copyOf(errors);
    }

    public List<String> getErrors() {
        return errors;
    }
}
