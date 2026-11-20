package com.user.tenant.entity;

import com.user.tenant.enums.OrganizationStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "organizations")
public class Organization {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "slug", nullable = false, length = 100, unique = true)
    private String slug;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrganizationStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Transient
    private Clock clock = Clock.systemUTC();

    protected Organization(){}

    public Organization(String name, String slug){
        this(name, slug, Clock.systemUTC());
    }

    public Organization(String name, String slug, Clock clock) {
        validateString(name,"name", 150);

        validateString(slug,"slug", 100);

        this.name = name.trim();
        this.slug = slug.trim();
        this.clock = Objects.requireNonNull(clock, "Clock is required");
        this.status = OrganizationStatus.ACTIVE;
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

    public void updateName(String name){
        validateString(name,"name", 150);

        this.name = name.trim();
    }

    public void updateSlug(String slug){
        validateString(slug,"slug", 100);

        this.slug = slug.trim();
    }

    public void changeStatus(OrganizationStatus status){
        this.status = Objects.requireNonNull(status, "Organization status is required");
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }

    public OrganizationStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    private void validateString(String value, String field, int maxLength){
        if (value == null || value.isBlank()){
            throw new IllegalArgumentException("Organization "+ field +" is required");
        }
        if (value.trim().length() > maxLength){
            throw new IllegalArgumentException(
                    "Organization "+ field +" must not exceed "+ maxLength +" characters"
            );
        }
    }
}
