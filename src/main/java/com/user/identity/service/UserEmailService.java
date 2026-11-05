package com.user.identity.service;

import com.user.identity.entity.EmailVerificationToken;
import com.user.identity.entity.User;
import com.user.identity.entity.UserEmail;
import com.user.identity.repository.EmailVerificationTokenRepository;
import com.user.identity.repository.UserEmailRepository;
import com.user.identity.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Locale;
import java.util.UUID;

@Service
public class UserEmailService {

    private static final Duration TOKEN_TTL = Duration.ofMinutes(15);

    private final UserRepository userRepository;
    private final UserEmailRepository userEmailRepository;
    private final EmailVerificationTokenRepository tokenRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public UserEmailService(UserRepository userRepository,
                            UserEmailRepository userEmailRepository,
                            EmailVerificationTokenRepository tokenRepository) {
        this.userRepository = userRepository;
        this.userEmailRepository = userEmailRepository;
        this.tokenRepository = tokenRepository;
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

        return issueVerificationToken(userEmail);
    }

    @Transactional
    public void verifyEmail(String rawToken){
        if(rawToken == null || rawToken.isBlank()){
            throw new IllegalArgumentException("Token is required");
        }
        byte[] tokenHash = hashToken(rawToken);

        EmailVerificationToken token = tokenRepository
                .findByTokenHashAndUsedAtIsNullAndRevokedAtIsNull(tokenHash)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or already used token"));

        if (token.isExpired()){
            throw new IllegalArgumentException("Token has expired");
        }
        UserEmail email = token.getUserEmail();

        token.consume();
        email.verify();

        tokenRepository.save(token);
        userEmailRepository.save(email);
    }
    @Transactional
    public EmailVerificationChallenge resendVerification(UUID userId, UUID emailId){
        UserEmail email = findOwnedActiveEmail(userId, emailId);

        if(email.getVerifiedAt() != null){
            throw new IllegalArgumentException("Email is already verified");
        }

        return issueVerificationToken(email);
    }
    @Transactional
    public void setPrimaryEmail(UUID userId, UUID emailId){
        UserEmail newPrimary = findOwnedActiveEmail(userId, emailId);

        if(newPrimary.getVerifiedAt() == null){
            throw new IllegalArgumentException("Email must be before becoming primary");
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
    @Transactional
    public void retireEmail(UUID userId, UUID emailId){
        UserEmail email = findOwnedActiveEmail(userId, emailId);

        email.retire();

        userEmailRepository.saveAndFlush(email);
    }
    private EmailVerificationChallenge issueVerificationToken(UserEmail email){
        tokenRepository.findAllByUserEmail_IdAndUsedAtIsNullAndRevokedAtIsNull(
                email.getId()
        )
                .forEach(EmailVerificationToken::revoke);

        tokenRepository.flush();

        String rawToken = generateRawToken();
        Instant expiresAt = Instant.now().plus(TOKEN_TTL);

        EmailVerificationToken token = new EmailVerificationToken(
                email, hashToken(rawToken), expiresAt
        );
        tokenRepository.saveAndFlush(token);

        return new EmailVerificationChallenge(
                email.getId(), rawToken, expiresAt
        );
    }

    private UserEmail findOwnedActiveEmail(UUID userId, UUID emailId){
        return userEmailRepository.findByIdAndUser_IdAndRetiredAtIsNull(emailId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Active email not found for user"));
    }
    private String generateRawToken(){
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);

        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
    private byte[] hashToken(String rawToken){
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            return digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException exception){
            throw new IllegalStateException("SHA-256 algorithm is unavailable",exception );
        }
    }

    private String normalizeEmail(String email){
        if(email == null || email.isBlank()){
            throw new IllegalStateException("Email is required");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
