
package com.user.identity.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "email_verification_tokens")
public class EmailVerificationToken {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "email_id", nullable = false)
    private UserEmail userEmail;

    @Column(name = "token_hash", nullable = false, unique = true)
    private byte[] tokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "used_at")
    private Instant usedAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Transient
    private Clock clock = Clock.systemUTC();

    protected EmailVerificationToken() {
    }
    public EmailVerificationToken(
            UserEmail userEmail,
            byte[] tokenHash,
            Instant expiresAt
    ){
        this(userEmail, tokenHash, expiresAt, Clock.systemUTC());
    }


    public EmailVerificationToken(
            UserEmail userEmail,
            byte[] tokenHash,
            Instant expiresAt,
            Clock clock
    ) {
        if (userEmail == null) {
            throw new IllegalArgumentException("User email is required");
        }

        if (tokenHash == null || tokenHash.length != 32) {
            throw new IllegalArgumentException(
                    "Token hash must contain exactly 32 bytes"
            );
        }
        if (clock == null){
            throw new IllegalArgumentException("Clock is required");
        }

        if (expiresAt == null || !expiresAt.isAfter(Instant.now(clock))) {
            throw new IllegalArgumentException(
                    "Expiration must be in the future"
            );
        }

        this.userEmail = userEmail;
        this.tokenHash = tokenHash.clone();
        this.expiresAt = expiresAt;
        this.clock = clock;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now(clock);
    }

    public void consume() {
        if (usedAt != null || revokedAt != null || isExpired()) {
            throw new IllegalStateException("Token is no longer active");
        }

        this.usedAt = Instant.now(clock);
    }

    public void revoke() {
        if (usedAt == null && revokedAt == null) {
            this.revokedAt = Instant.now(clock);
        }
    }

    public boolean isExpired() {
        return !Instant.now(clock).isBefore(expiresAt);
    }

    public boolean isActive() {
        return usedAt == null
                && revokedAt == null
                && !isExpired();
    }

    public UUID getId() {
        return id;
    }

    public UserEmail getUserEmail() {
        return userEmail;
    }

    public byte[] getTokenHash() {
        return tokenHash == null ? null : tokenHash.clone();
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getUsedAt() {
        return usedAt;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
