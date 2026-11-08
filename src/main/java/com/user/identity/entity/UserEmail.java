package com.user.identity.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Entity
@Table(name = "user_emails")
public class UserEmail {
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "email", nullable = false, length = 254)
    private String email;

    @Column(name = "email_normalized", nullable = false, length = 254)
    private String normalizedEmail;

    @Column(name = "is_primary", nullable = false)
    private boolean primary;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "retired_at")
    private Instant retiredAt;

    @Transient
    private Clock clock = Clock.systemUTC();

    protected UserEmail(){}

    public UserEmail(User user, String email, Clock clock) {
        this(user, email);

        if (clock == null){
            throw new IllegalArgumentException("Clock is required");
        }
        this.clock = clock;
    }

    public UserEmail(User user, String email) {
        if(user == null){
            throw new IllegalArgumentException("User is required");
        }
        if(email == null || email.isBlank()){
            throw new IllegalArgumentException("Email is required");
        }

        this.user = user;
        this.email = email.trim();
        this.normalizedEmail = normalizeEmail(email);
        this.primary = false;
    }
    private String normalizeEmail(String email){
        return email.trim().toLowerCase(Locale.ROOT);
    }
    @PrePersist
    protected void onCrete(){
        this.createdAt = Instant.now(clock);
    }
    public void verify(){
        ensureNotRetired();

        if(this.verifiedAt == null){
            this.verifiedAt = Instant.now(clock);
        }
    }
    public void makePrimary(){
        ensureNotRetired();

        if(this.verifiedAt == null){
            throw new IllegalStateException("An unverified email cannot be primary");
        }
        this.primary = true;
    }
    public void removePrimary(){
        this.primary = false;
    }
    public void retire(){
        if(this.retiredAt != null){
            return;
        }
        this.primary = false;
        this.retiredAt = Instant.now(clock);
    }

    private void ensureNotRetired(){
        if (this.retiredAt != null){
            throw new IllegalArgumentException("A retired email cannot be modified");
        }
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getEmail() {
        return email;
    }

    public String getNormalizedEmail() {
        return normalizedEmail;
    }

    public boolean isPrimary() {
        return primary;
    }

    public Instant getVerifiedAt() {
        return verifiedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getRetiredAt() {
        return retiredAt;
    }
}
