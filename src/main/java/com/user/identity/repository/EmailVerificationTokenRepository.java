package com.user.identity.repository;

import com.user.identity.entity.EmailVerificationToken;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmailVerificationTokenRepository
        extends JpaRepository<EmailVerificationToken, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<EmailVerificationToken>
    findByTokenHashAndUsedAtIsNullAndRevokedAtIsNull(byte[] tokenHash);

    List<EmailVerificationToken>
    findAllByUserEmail_IdAndUsedAtIsNullAndRevokedAtIsNull(UUID userEmailId);

    List<EmailVerificationToken>
    findAllByUserEmail_Id(UUID userEmailId);
}