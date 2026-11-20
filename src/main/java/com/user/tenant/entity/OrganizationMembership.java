package com.user.tenant.entity;

import com.user.identity.entity.User;
import com.user.tenant.enums.MembershipStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "organization_memberships")
public class OrganizationMembership {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MembershipStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_by")
    private User invitedBy;

    @Column(name = "joined_at")
    private Instant joinedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "removed_at")
    private Instant removedAt;

    @Transient
    private Clock clock = Clock.systemUTC();

    protected OrganizationMembership(){}

    public OrganizationMembership(Organization organization, User user, User invitedBy) {
        this(organization, user, invitedBy, Clock.systemUTC());
    }

    public OrganizationMembership(Organization organization, User user, User invitedBy, Clock clock) {

        this.organization = Objects.requireNonNull(
                organization, "Organization is required"
        );
        this.user = Objects.requireNonNull(
                user, "User is required"
        );
        this.invitedBy = invitedBy;
        this.clock = Objects.requireNonNull(
                clock, "Clock is required"
        );
        this.status = MembershipStatus.PENDING;
    }

    public void acceptInvitation(){
        ensureNotRemoved();

        if (status != MembershipStatus.PENDING){
            throw new IllegalStateException("Only pending membership can accept invitations");
        }

        this.status = MembershipStatus.ACTIVE;
        this.joinedAt = Instant.now(clock);
    }

    public void suspend(){
        ensureNotRemoved();

        if (status != MembershipStatus.ACTIVE){
            throw new IllegalStateException("Only active membership can be suspended");
        }

        this.status = MembershipStatus.SUSPENDED;
    }

    public void reactivate(){
        ensureNotRemoved();

        if (status != MembershipStatus.SUSPENDED){
            throw new IllegalStateException("Only suspended membership can be reactivated");
        }

        this.status = MembershipStatus.ACTIVE;
    }

    public void remove(){
        ensureNotRemoved();

        this.status = MembershipStatus.REMOVED;
        this.removedAt = Instant.now(clock);
    }

    private void ensureNotRemoved(){
        if (status == MembershipStatus.REMOVED){
            throw new IllegalStateException("A removed membership cannot be modified");
        }
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

    public UUID getId() {
        return id;
    }

    public Organization getOrganization() {
        return organization;
    }

    public User getUser() {
        return user;
    }

    public MembershipStatus getStatus() {
        return status;
    }

    public User getInvitedBy() {
        return invitedBy;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getRemovedAt() {
        return removedAt;
    }
}
