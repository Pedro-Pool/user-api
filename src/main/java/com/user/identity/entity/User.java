package com.user.identity.entity;


import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
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

    protected User(){}

    public User(String displayName, String firstName, String lastName) {
        this.displayName = displayName;
        this.firstName = firstName;
        this.lastName = lastName;
    }
    @PrePersist
    protected void onCreate(){
        Instant now = Instant.now();

        this.createdAt = now;
        this.updatedAt = now;

        if (this.status == null){
            this.status = UserStatus.PENDING_VERIFICATION;
        }
    }
    @PreUpdate
    protected void onUpdate(){
        this.updatedAt = Instant.now();
    }

    public void updateDisplayName(String displayName){
        this.displayName = displayName;
    }
    public void updatePersonalName(String firstName, String lastName){
        this.firstName = firstName;
        this.lastName = lastName;
    }
    public void activate(){
        this.status = UserStatus.ACTIVE;
    }
    public void lock(){
        this.status = UserStatus.LOCKED;
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
}
