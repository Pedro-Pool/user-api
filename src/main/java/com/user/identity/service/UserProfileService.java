package com.user.identity.service;


import com.user.identity.dto.user.UserProfileUpdateRequest;
import com.user.identity.dto.user.UserResponse;
import com.user.identity.entity.User;
import com.user.identity.exception.UserProfileValidationException;
import com.user.identity.repository.UserRepository;
import com.user.identity.validation.ValidationResult;
import com.user.identity.validation.user.UserProfileValidation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
@Transactional
public class UserProfileService {

    private final UserProfileValidation profileValidation;
    private final UserRepository userRepository;

    public UserProfileService(UserProfileValidation profileValidation, UserRepository userRepository) {

        this.profileValidation = Objects.requireNonNull(profileValidation);
        this.userRepository = Objects.requireNonNull(userRepository);
    }

    @Transactional(readOnly = true)
    public UserResponse getProfile(UUID userId){
        Objects.requireNonNull(userId, "User ID is required");

        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return new UserResponse(
                user.getId(),
                user.getDisplayName(),
                user.getFirstName(),
                user.getLastName(),
                user.getStatus(),
                user.getCreatedAt()
        );
    }

    public UserResponse updateProfile(UUID userId, UserProfileUpdateRequest request){
        Objects.requireNonNull(userId, "User ID is required");

        ValidationResult result = profileValidation.validate(request);

        if (!result.valid()){
            throw new UserProfileValidationException(result.errors());
        }

        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (request.displayName() != null){
            user.updateDisplayName(request.displayName());
        }
        if (request.firstName() != null){
            user.updateFirstName(request.firstName());
        }
        if (request.lastName() != null){
            user.updateLastName(request.lastName());
        }

        return new UserResponse(
                user.getId(),
                user.getDisplayName(),
                user.getFirstName(),
                user.getLastName(),
                user.getStatus(),
                user.getCreatedAt()
        );
    }
}
