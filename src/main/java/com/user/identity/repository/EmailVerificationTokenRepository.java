package com.user.identity.repository;

import com.user.identity.entity.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmailVerificationTokenRepository
        extends JpaRepository<EmailVerificationToken, UUID> {

    Optional<EmailVerificationToken>
    findByTokenHashAndUsedAtIsNullAndRevokedAtIsNull(byte[] tokenHash);

    List<EmailVerificationToken>
    findAllByUserEmail_IdAndUsedAtIsNullAndRevokedAtIsNull(UUID userEmailId);
}