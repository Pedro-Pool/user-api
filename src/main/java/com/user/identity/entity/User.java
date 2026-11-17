package com.user.identity.entity;


import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(name = "display_name", nullable = false, length = 120)
    private String displayName;

    @Column(name = "first_name", nullable = false, length = 80)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 120)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private UserStatus status = UserStatus.PENDING_VERIFICATION;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Version
    @Column(nullable = false)
    private long version;

    @Transient
    private Clock clock = Clock.systemUTC();

    protected User(){}

    public User(String displayName, String firstName, String lastName){
        this(displayName, firstName, lastName,Clock.systemUTC());
    }

    public User(String displayName, String firstName, String lastName, Clock clock) {
        this.clock = Objects.requireNonNull(clock, "Clock is required");

        validateName(displayName, 120, "Display name");
        validateName(firstName, 80, "First name");
        validateName(lastName, 120, "Last name");

        this.displayName = displayName;
        this.firstName = firstName;
        this.lastName = lastName;
    }
    @PrePersist
    protected void onCreate(){
        Instant now = Instant.now(clock);

        this.createdAt = now;
        this.updatedAt = now;

        if (this.status == null){
            this.status = UserStatus.PENDING_VERIFICATION;
        }
    }
    @PreUpdate
    protected void onUpdate(){
        this.updatedAt = Instant.now(clock);
    }

    public void updateDisplayName(String displayName){
        validateName(displayName, 120, "Display name");

        this.displayName = displayName;
    }
    public void updateFirstName(String firstName){
        validateName(firstName, 80, "First name");

        this.firstName = firstName;
    }
    public void updateLastName(String lastName){
        validateName(lastName, 120, "Last name");

        this.lastName = lastName;
    }
    public void activate(){
        if (this.status == UserStatus.DISABLED){
            throw new IllegalArgumentException("Disabled user cannot be activated");
        }

        this.status = UserStatus.ACTIVE;
    }
    public void lock(){
        if (this.status == UserStatus.DISABLED){
            throw new IllegalArgumentException("Disabled user cannot be locked");
        }

        this.status = UserStatus.LOCKED;
    }

    public void softDelete(){
        if (this.deletedAt != null){
            return;
        }

        this.deletedAt = Instant.now(clock);
    }
    public void disable(){
        this.status = UserStatus.DISABLED;
    }

    public UUID getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public UserStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public long getVersion() {
        return version;
    }

    private void validateName(String value, int maxLength, String fieldName){
        if (value == null || value.isBlank()){
            throw new IllegalArgumentException(fieldName + " is required");
        }
        if (value.length() > maxLength){
            throw new IllegalArgumentException(fieldName + " must not exceed " + maxLength + " characters");
        }
    }
}
