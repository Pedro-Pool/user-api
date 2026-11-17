package com.user.identity.validation.user;

import com.user.identity.dto.user.UserProfileUpdateRequest;
import com.user.identity.validation.ValidationResult;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserProfileValidation {

    public ValidationResult validate(UserProfileUpdateRequest request) {
        if (request == null){
            return ValidationResult.failure(
                    List.of("User profile update request is required")
            );
        }
        if (request.displayName() == null
        && request.firstName() == null
        && request.lastName() == null){
            return ValidationResult.failure(
                    List.of("At least one profile field must be provided")
            );
        }
        if (request.displayName() != null && request.displayName().isBlank()){
            return ValidationResult.failure(
                    List.of("Display name cannot be blank")
            );
        }
        if (request.displayName() != null && request.displayName().length() > 120){
            return ValidationResult.failure(
                    List.of("Display name must not exceed 120 characters")
            );
        }
        if (request.firstName() != null && request.firstName().isBlank()){
            return ValidationResult.failure(
                    List.of("First name cannot be blank")
            );
        }
        if (request.firstName() != null && request.firstName().length() > 80){
            return ValidationResult.failure(
                    List.of("First name must not exceed 80 characters")
            );
        }
        if (request.lastName() != null && request.lastName().isBlank()){
            return ValidationResult.failure(
                    List.of("Last name cannot be blank")
            );
        }
        if (request.lastName() != null && request.lastName().length() > 120){
            return ValidationResult.failure(
                    List.of("Last name must not exceed 120 characters")
            );
        }

        return ValidationResult.success();
    }
}
