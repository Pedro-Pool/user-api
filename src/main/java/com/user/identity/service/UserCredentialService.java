package com.user.identity.service;

import com.user.identity.entity.User;
import com.user.identity.entity.UserCredential;
import com.user.identity.exception.PasswordValidationException;
import com.user.identity.repository.UserCredentialRepository;
import com.user.identity.validation.password.PasswordValidationResult;
import com.user.identity.validation.password.RegistrationPasswordValidation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.Objects;

@Service
@Transactional
public class UserCredentialService {

    private final RegistrationPasswordValidation passwordValidation;
    private final PasswordService passwordService;
    private final UserCredentialRepository userCredentialRepository;
    private final Clock clock;

    public UserCredentialService
            (RegistrationPasswordValidation passwordValidation,
             PasswordService passwordService,
             UserCredentialRepository userCredentialRepository,
             Clock clock) {
        this.passwordValidation = Objects.requireNonNull(passwordValidation);
        this.passwordService = Objects.requireNonNull(passwordService);
        this.userCredentialRepository = Objects.requireNonNull(userCredentialRepository);
        this.clock = Objects.requireNonNull(clock);
    }

    public UserCredential createCredential(User user, String rawPassword){
        Objects.requireNonNull(user, "User is required");

        PasswordValidationResult result = passwordValidation.validate(rawPassword);

        if (!result.valid()){
            throw new PasswordValidationException(result.errors());
        }

        String passwordHash = passwordService.encode(rawPassword);

        UserCredential credential = new UserCredential(
                user,
                passwordHash,
                clock
        );

        return userCredentialRepository.save(credential);
    }
}
