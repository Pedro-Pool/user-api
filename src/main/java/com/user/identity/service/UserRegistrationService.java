package com.user.identity.service;

import com.user.identity.dto.user.UserRegistrationRequest;
import com.user.identity.dto.user.UserRegistrationResponse;
import com.user.identity.dto.user.UserResponse;
import com.user.identity.entity.User;
import com.user.identity.repository.UserRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Validator;

import java.util.Objects;
import java.util.Set;


@Service
@Transactional
public class UserRegistrationService {

    private final Validator validator;
    private final UserRepository userRepository;
    private final UserCredentialService userCredentialService;
    private final UserEmailService userEmailService;

    public UserRegistrationService(
            Validator validator,
            UserRepository userRepository,
            UserCredentialService userCredentialService,
            UserEmailService userEmailService) {
        this.validator = Objects.requireNonNull(validator);
        this.userRepository = Objects.requireNonNull(userRepository);
        this.userCredentialService = Objects.requireNonNull(userCredentialService);
        this.userEmailService = Objects.requireNonNull(userEmailService);
    }

    public UserRegistrationResponse register(UserRegistrationRequest request){
        if (request == null){
            throw new IllegalArgumentException("Registration request is required");
        }

        validateRequest(request);

        User user = new User(
                request.displayName(),
                request.firstName(),
                request.lastName()
        );

        user = userRepository.save(user);

        userCredentialService.createCredential(user, request.password());

        EmailVerificationChallenge challenge = userEmailService.registerEmail(user.getId(), request.email());

        UserResponse response = new UserResponse(
                user.getId(),
                user.getDisplayName(),
                user.getFirstName(),
                user.getLastName(),
                user.getStatus(),
                user.getCreatedAt()
        );

        return new UserRegistrationResponse(
                response,
                challenge
        );
    }


    private void validateRequest(UserRegistrationRequest request){
        Set<ConstraintViolation<UserRegistrationRequest>> violations =
                validator.validate(request);

        if (!violations.isEmpty()){
            throw new ConstraintViolationException(violations);
        }
    }
}
