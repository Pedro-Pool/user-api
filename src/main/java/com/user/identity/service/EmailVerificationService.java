package com.user.identity.service;

import com.user.identity.entity.EmailVerificationToken;
import com.user.identity.entity.UserEmail;
import com.user.identity.repository.EmailVerificationTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Base64;

@Service
public class EmailVerificationService {

    private final SecureRandom secureRandom = new SecureRandom();

    private final EmailVerificationTokenRepository tokenRepository;

    private final Clock clock;

    public EmailVerificationService(EmailVerificationTokenRepository tokenRepository, Clock clock) {
        this.tokenRepository = tokenRepository;
        this.clock = clock;
    }

    @Transactional
    public String createToken(UserEmail userEmail){
        if (userEmail == null){
            throw new IllegalArgumentException("User email is required");
        }
        byte[] rawToken = generateRawToken();

        byte[] tokenHash = hashToken(rawToken);

        Instant expiresAt = Instant.now(clock).plusSeconds(900);

        EmailVerificationToken token = new EmailVerificationToken(
                userEmail,
                tokenHash,
                expiresAt,
                clock
        );
        tokenRepository.save(token);

        return encodeToken(rawToken);
    }
    @Transactional
    public void verifyToken(String encodedToken){
        byte[] rawToken = decodeToken(encodedToken);
        byte[] tokenHash = hashToken(rawToken);

        EmailVerificationToken token = tokenRepository
                .findByTokenHashAndUsedAtIsNullAndRevokedAtIsNull(tokenHash)
                .orElseThrow(() ->
                        new IllegalArgumentException("Invalid or expired token"));

        if (!token.isActive()){
            throw new IllegalArgumentException("Invalid or expired token");
        }

        token.consume();
        token.getUserEmail().verify();
    }

    private byte[] generateRawToken() {
        byte[] token = new byte[32];
        secureRandom.nextBytes(token);
        return token;
    }
    private byte[] hashToken(byte[] rawToken){
        if (rawToken == null || rawToken.length != 32){
            throw new IllegalArgumentException("Invalid token");
        }
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(rawToken);
        }catch (NoSuchAlgorithmException exception){
            throw new IllegalStateException("SHA-256 algorithm is not available", exception);
        }
    }
    private String encodeToken(byte[] rawToken){
        if (rawToken == null || rawToken.length != 32){
            throw new IllegalArgumentException("Invalid token");
        }
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(rawToken);
    }
    private byte[] decodeToken(String encodedToken) {
        if (encodedToken == null || encodedToken.isBlank()) {
            throw new IllegalArgumentException("Invalid token");
        }
        if (!encodedToken.matches("[A-Za-z0-9_-]{43}")) {
            throw new IllegalArgumentException("Invalid token");
        }

        byte[] decodedToken;

        try {
            decodedToken = Base64.getUrlDecoder().decode(encodedToken);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid token", exception);
        }
        if (decodedToken.length != 32) {
            throw new IllegalArgumentException("Invalid token");
        }

        return decodedToken;
    }
}
