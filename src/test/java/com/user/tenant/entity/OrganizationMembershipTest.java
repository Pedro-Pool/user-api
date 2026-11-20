package com.user.tenant.entity;

import com.user.identity.entity.User;
import com.user.tenant.enums.MembershipStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.*;

class OrganizationMembershipTest {

    private Organization createOrganization() {
        return new Organization("Acme Corporation", "acme");
    }

    private User createUser() {
        return new User("John Doe", "John", "Doe");
    }

    @Test
    @DisplayName("Should create membership with pending status")
    void test_01() {
        Organization organization = createOrganization();
        User user = createUser();
        User invitedBy = createUser();

        OrganizationMembership membership =
                new OrganizationMembership(organization, user, invitedBy);

        assertThat(membership.getId()).isNull();
        assertThat(membership.getOrganization()).isSameAs(organization);
        assertThat(membership.getUser()).isSameAs(user);
        assertThat(membership.getInvitedBy()).isSameAs(invitedBy);
        assertThat(membership.getStatus()).isEqualTo(MembershipStatus.PENDING);
        assertThat(membership.getJoinedAt()).isNull();
        assertThat(membership.getCreatedAt()).isNull();
        assertThat(membership.getUpdatedAt()).isNull();
        assertThat(membership.getRemovedAt()).isNull();
    }
    @Test
    @DisplayName("Should create membership without invitedBy")
    void test_02() {
        OrganizationMembership membership =
                new OrganizationMembership(createOrganization(), createUser(), null);

        assertThat(membership.getInvitedBy()).isNull();
        assertThat(membership.getStatus()).isEqualTo(MembershipStatus.PENDING);
    }
    @Test
    @DisplayName("Should reject null organization")
    void test_03() {
        assertThatNullPointerException()
                .isThrownBy(() ->
                        new OrganizationMembership(null, createUser(), null)
                )
                .withMessage("Organization is required");
    }
    @Test
    @DisplayName("Should reject null user")
    void test_04() {
        assertThatNullPointerException()
                .isThrownBy(() ->
                        new OrganizationMembership(createOrganization(), null, null)
                )
                .withMessage("User is required");
    }
    @Test
    @DisplayName("Should reject null clock")
    void test_05() {
        assertThatNullPointerException()
                .isThrownBy(() ->
                        new OrganizationMembership(
                                createOrganization(),
                                createUser(),
                                null,
                                null
                        )
                )
                .withMessage("Clock is required");
    }
    @Test
    @DisplayName("Should accept a pending invitation")
    void test_06() {
        OrganizationMembership membership =
                new OrganizationMembership(createOrganization(), createUser(), null);

        membership.acceptInvitation();

        assertThat(membership.getStatus()).isEqualTo(MembershipStatus.ACTIVE);
        assertThat(membership.getJoinedAt()).isNotNull();
        assertThat(membership.getRemovedAt()).isNull();
    }
    @Test
    @DisplayName("Should suspend an active membership")
    void test_07() {
        OrganizationMembership membership =
                new OrganizationMembership(createOrganization(), createUser(), null);

        membership.acceptInvitation();
        membership.suspend();

        assertThat(membership.getStatus()).isEqualTo(MembershipStatus.SUSPENDED);
    }
    @Test
    @DisplayName("Should reactivate a suspended membership")
    void test_08() {
        OrganizationMembership membership =
                new OrganizationMembership(createOrganization(), createUser(), null);

        membership.acceptInvitation();
        membership.suspend();
        membership.reactivate();

        assertThat(membership.getStatus()).isEqualTo(MembershipStatus.ACTIVE);
    }
    @Test
    @DisplayName("Should remove a pending membership")
    void test_09() {
        OrganizationMembership membership =
                new OrganizationMembership(createOrganization(), createUser(), null);

        membership.remove();

        assertThat(membership.getStatus()).isEqualTo(MembershipStatus.REMOVED);
        assertThat(membership.getRemovedAt()).isNotNull();
    }
    @Test
    @DisplayName("Should remove an active membership")
    void test_10() {
        OrganizationMembership membership =
                new OrganizationMembership(createOrganization(), createUser(), null);

        membership.acceptInvitation();
        membership.remove();

        assertThat(membership.getStatus()).isEqualTo(MembershipStatus.REMOVED);
        assertThat(membership.getRemovedAt()).isNotNull();
    }
    @Test
    @DisplayName("Should remove a suspended membership")
    void test_11() {
        OrganizationMembership membership =
                new OrganizationMembership(createOrganization(), createUser(), null);

        membership.acceptInvitation();
        membership.suspend();
        membership.remove();

        assertThat(membership.getStatus()).isEqualTo(MembershipStatus.REMOVED);
        assertThat(membership.getRemovedAt()).isNotNull();
    }
    @Test
    @DisplayName("Should reject accepting an invitation when already active")
    void test_12() {
        OrganizationMembership membership =
                new OrganizationMembership(createOrganization(), createUser(), null);

        membership.acceptInvitation();

        assertThatThrownBy(membership::acceptInvitation)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Only pending membership can accept invitations");
    }
    @Test
    @DisplayName("Should reject suspending a pending membership")
    void test_13() {
        OrganizationMembership membership =
                new OrganizationMembership(createOrganization(), createUser(), null);

        assertThatThrownBy(membership::suspend)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Only active membership can be suspended");
    }
    @Test
    @DisplayName("Should reject reactivating an active membership")
    void test_14() {
        OrganizationMembership membership =
                new OrganizationMembership(createOrganization(), createUser(), null);

        membership.acceptInvitation();

        assertThatThrownBy(membership::reactivate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Only suspended membership can be reactivated");
    }
    @Test
    @DisplayName("Should reject reactivating a removed membership")
    void test_15() {
        OrganizationMembership membership =
                new OrganizationMembership(createOrganization(), createUser(), null);

        membership.remove();

        assertThatThrownBy(membership::reactivate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("A removed membership cannot be modified");
    }
    @Test
    @DisplayName("Should reject accepting an invitation after removal")
    void test_16() {
        OrganizationMembership membership =
                new OrganizationMembership(createOrganization(), createUser(), null);

        membership.remove();

        assertThatThrownBy(membership::acceptInvitation)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("A removed membership cannot be modified");
    }
    @Test
    @DisplayName("Should reject suspending a removed membership")
    void test_17() {
        OrganizationMembership membership =
                new OrganizationMembership(createOrganization(), createUser(), null);

        membership.remove();

        assertThatThrownBy(membership::suspend)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("A removed membership cannot be modified");
    }
    @Test
    @DisplayName("Should reject removing a membership more than once")
    void test_18() {
        OrganizationMembership membership =
                new OrganizationMembership(createOrganization(), createUser(), null);

        membership.remove();

        assertThatThrownBy(membership::remove)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("A removed membership cannot be modified");
    }
    @Test
    @DisplayName("Should set joinedAt when invitation is accepted")
    void test_19() {
        Instant expected = Instant.parse("2026-01-15T10:00:00Z");
        MutableClock clock = new MutableClock(expected);

        OrganizationMembership membership =
                new OrganizationMembership(
                        createOrganization(), createUser(), null, clock
                );

        membership.acceptInvitation();

        assertThat(membership.getJoinedAt()).isEqualTo(expected);
    }
    @Test
    @DisplayName("Should set removedAt when membership is removed")
    void test_20() {
        Instant expected = Instant.parse("2026-01-15T11:00:00Z");
        MutableClock clock = new MutableClock(expected);

        OrganizationMembership membership =
                new OrganizationMembership(
                        createOrganization(), createUser(), null, clock
                );

        membership.remove();

        assertThat(membership.getRemovedAt()).isEqualTo(expected);
    }
    @Test
    @DisplayName("Should set createdAt and updatedAt on creation")
    void test_21() {
        Instant expected = Instant.parse("2026-01-15T10:00:00Z");
        MutableClock clock = new MutableClock(expected);

        OrganizationMembership membership =
                new OrganizationMembership(
                        createOrganization(), createUser(), null, clock
                );

        membership.onCreate();

        assertThat(membership.getCreatedAt()).isEqualTo(expected);
        assertThat(membership.getUpdatedAt()).isEqualTo(expected);
    }
    @Test
    @DisplayName("Should update updatedAt without changing createdAt")
    void test_22() {
        MutableClock clock = new MutableClock(
                Instant.parse("2026-01-15T10:00:00Z")
        );

        OrganizationMembership membership =
                new OrganizationMembership(
                        createOrganization(), createUser(), null, clock
                );

        membership.onCreate();

        Instant createdAt = membership.getCreatedAt();

        clock.setInstant(Instant.parse("2026-01-15T11:00:00Z"));

        membership.acceptInvitation();
        membership.onUpdate();

        assertThat(membership.getCreatedAt()).isEqualTo(createdAt);
        assertThat(membership.getUpdatedAt())
                .isEqualTo(Instant.parse("2026-01-15T11:00:00Z"))
                .isAfter(createdAt);
    }


    private static class MutableClock extends Clock {

        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        public void setInstant(Instant instant) {
            this.instant = instant;
        }

        @Override
        public Instant instant() {
            return instant;
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }
    }
}
