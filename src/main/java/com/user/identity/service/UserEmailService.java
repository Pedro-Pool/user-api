package com.user.identity.service;

import com.user.identity.entity.User;
import com.user.identity.entity.UserEmail;
import com.user.identity.repository.UserEmailRepository;
import com.user.identity.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@Service
public class UserEmailService {

    private final UserRepository userRepository;
    private final UserEmailRepository userEmailRepository;
    private final EmailVerificationService emailVerificationService;

    public UserEmailService(UserRepository userRepository,
                            UserEmailRepository userEmailRepository,
                            EmailVerificationService emailVerificationService) {
        this.userRepository = Objects.requireNonNull(userRepository);
        this.userEmailRepository = Objects.requireNonNull(userEmailRepository);
        this.emailVerificationService = Objects.requireNonNull(emailVerificationService);
    }
    @Transactional
    public EmailVerificationChallenge registerEmail(UUID userId, String rawEmail){
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String normalizedEmail = normalizeEmail(rawEmail);

        if(userEmailRepository.existsByNormalizedEmailAndRetiredAtIsNull(normalizedEmail)){
            throw new IllegalStateException("Email is already in use");
        }
        UserEmail userEmail = new UserEmail(user, rawEmail);
        userEmail = userEmailRepository.saveAndFlush(userEmail);

        return emailVerificationService.createToken(userEmail);
    }
    @Transactional
    public void retireEmail(UUID userId, UUID emailId) {
        UserEmail email = findOwnedActiveEmail(userId, emailId);

        email.retire();

        userEmailRepository.saveAndFlush(email);
    }
    @Transactional
    public EmailVerificationChallenge resendVerification(UUID userId, UUID emailId){
        UserEmail email = findOwnedActiveEmail(userId, emailId);

        if(email.getVerifiedAt() != null){
            throw new IllegalArgumentException("Email is already verified");
        }

        return emailVerificationService.createToken(email);
    }
    @Transactional
    public void setPrimaryEmail(UUID userId, UUID emailId){
        UserEmail newPrimary = findOwnedActiveEmail(userId, emailId);

        if(newPrimary.getVerifiedAt() == null){
            throw new IllegalArgumentException("Email must be verified before becoming primary");
        }
        userEmailRepository
                .findByUserIdAndPrimaryTrueAndRetiredAtIsNull(userId)
                .filter(current -> !current.getId().equals(emailId))
                .ifPresent(current -> {current.removePrimary();
                userEmailRepository.saveAndFlush(current);
                });

        newPrimary.makePrimary();
        userEmailRepository.saveAndFlush(newPrimary);
    }

    private UserEmail findOwnedActiveEmail(UUID userId, UUID emailId){
        return userEmailRepository.findByIdAndUser_IdAndRetiredAtIsNull(emailId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Active email not found for user"));
    }
    private String normalizeEmail(String email){
        if(email == null || email.isBlank()){
            throw new IllegalArgumentException("Email is required");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
