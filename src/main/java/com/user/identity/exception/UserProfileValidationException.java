package com.user.identity.exception;

import java.util.List;

public class UserProfileValidationException extends RuntimeException {

    private final List<String> errors;

    public UserProfileValidationException(List<String> errors) {
        super("User profile validation failed");

        if (errors == null || errors.isEmpty()){
            throw new IllegalArgumentException("Validation errors are required");
        }
        this.errors = List.copyOf(errors);
    }
    public List<String> getErrors(){
        return errors;
    }
}
