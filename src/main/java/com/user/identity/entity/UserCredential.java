package com.user.identity.entity;


import jakarta.persistence.*;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_credentials")
public class UserCredential {
    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "password_hash",nullable = false, length = 512)
    private String passwordHash;

    @Column(name = "password_changed_at",nullable = false)
    private Instant passwordChangedAt;

    @Column(name = "password_expires_at")
    private Instant passwordExpiresAt;

    @Column(name = "must_change_password", nullable = false)
    private boolean mustChangePassword = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Transient
    private Clock clock = Clock.systemUTC();

    protected UserCredential(){}

    public UserCredential(User user, String passwordHash, Clock clock) {
        if (user == null){
            throw new IllegalArgumentException("User is required");
        }
        if (passwordHash == null || passwordHash.isBlank() || passwordHash.length() > 512){
            throw new IllegalArgumentException("Invalid password hash");
        }
        if (clock == null){
            throw new IllegalArgumentException("Clock is required");
        }

        this.user = user;
        this.passwordHash = passwordHash;
        this.clock = clock;

        this.passwordChangedAt = Instant.now(clock);
    }
    @PrePersist
    protected void onCreate(){
        Instant now = Instant.now(clock);

        this.createdAt = now;
        this.updatedAt = now;
    }
    @PreUpdate
    protected void onUpdate(){
        this.updatedAt = Instant.now(clock);
    }

    public void changePassword(String newPasswordHash){
        if (newPasswordHash == null
        || newPasswordHash.isBlank()
        || newPasswordHash.length() > 512){
            throw new IllegalArgumentException("Invalid password hash");
        }
        this.passwordHash = newPasswordHash;
        this.passwordChangedAt = Instant.now(clock);
        this.mustChangePassword = false;
    }
    public void requirePasswordChange(){
        this.mustChangePassword = true;
    }
    public void setPasswordExpiration(Instant expiration){
        if (expiration != null
        && !expiration.isAfter(Instant.now(clock))){
            throw new IllegalArgumentException("Password expiration must be in the future");
        }
        this.passwordExpiresAt = expiration;
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Instant getPasswordChangedAt() {
        return passwordChangedAt;
    }

    public Instant getPasswordExpiresAt() {
        return passwordExpiresAt;
    }

    public boolean isMustChangePassword() {
        return mustChangePassword;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Clock getClock() {
        return clock;
    }
}
