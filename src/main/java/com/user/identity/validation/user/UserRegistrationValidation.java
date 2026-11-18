package com.user.identity.validation.user;

import com.user.identity.dto.user.UserRegistrationRequest;
import com.user.identity.validation.ValidationResult;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class UserRegistrationValidation {

    private final Validator validator;

    public UserRegistrationValidation(Validator validator) {
        this.validator = Objects.requireNonNull(validator);
    }

    public ValidationResult validate(UserRegistrationRequest request){
        if (request == null){
            return ValidationResult.failure(
                    List.of("User registration request is required")
            );
        }

        List<String> errors = validator.validate(request)
                .stream()
                .map(ConstraintViolation::getMessage)
                .sorted()
                .toList();

        if (!errors.isEmpty()){
            return ValidationResult.failure(errors);
        }

        return ValidationResult.success();
    }
}
