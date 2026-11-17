package com.user.identity.validation;

import java.util.List;

public record ValidationResult(boolean valid, List<String> errors) {

    public ValidationResult {
        errors = List.copyOf(errors);

        if (valid && !errors.isEmpty()) {
            throw new IllegalArgumentException(
                    "A valid result cannot contain validation errors"
            );
        }

        if (!valid && errors.isEmpty()) {
            throw new IllegalArgumentException(
                    "An invalid result must contain validation errors"
            );
        }
    }

    public static ValidationResult success() {
        return new ValidationResult(true, List.of());
    }

    public static ValidationResult failure(List<String> errors) {
        if (errors == null || errors.isEmpty()) {
            throw new IllegalArgumentException(
                    "Validation errors are required"
            );
        }

        return new ValidationResult(false, errors);
    }
}