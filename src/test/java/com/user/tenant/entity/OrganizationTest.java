
package com.user.tenant.entity;

import com.user.tenant.enums.OrganizationStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrganizationTest {

    private static final Instant FIXED_INSTANT =
            Instant.parse("2026-01-15T10:00:00Z");

    private static final Clock FIXED_CLOCK =
            Clock.fixed(FIXED_INSTANT, ZoneOffset.UTC);

    @Test
    @DisplayName("Should create organization with valid data")
    void test_01() {
        Organization organization =
                new Organization("Acme Corporation", "acme", FIXED_CLOCK);

        assertThat(organization.getName()).isEqualTo("Acme Corporation");
        assertThat(organization.getSlug()).isEqualTo("acme");
        assertThat(organization.getStatus()).isEqualTo(OrganizationStatus.ACTIVE);
        assertThat(organization.getId()).isNull();
        assertThat(organization.getCreatedAt()).isNull();
        assertThat(organization.getUpdatedAt()).isNull();
    }
    @Test
    @DisplayName("Should trim organization name and slug")
    void test_02() {
        Organization organization =
                new Organization(" Acme Corporation ", " acme ", FIXED_CLOCK);

        assertThat(organization.getName()).isEqualTo("Acme Corporation");
        assertThat(organization.getSlug()).isEqualTo("acme");
    }
    @Test
    @DisplayName("Should reject null organization name")
    void test_03() {
        assertThatThrownBy(() ->
                new Organization(null, "acme", FIXED_CLOCK))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Organization name is required");
    }
    @Test
    @DisplayName("Should reject blank organization name")
    void test_04() {
        assertThatThrownBy(() ->
                new Organization(" ", "acme", FIXED_CLOCK))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Organization name is required");
    }
    @Test
    @DisplayName("Should reject organization name exceeding 150 characters")
    void test_05() {
        String name = "A".repeat(151);

        assertThatThrownBy(() ->
                new Organization(name, "acme", FIXED_CLOCK))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Organization name must not exceed 150 characters");
    }
    @Test
    @DisplayName("Should reject null organization slug")
    void test_06() {
        assertThatThrownBy(() ->
                new Organization("Acme Corporation", null, FIXED_CLOCK))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Organization slug is required");
    }
    @Test
    @DisplayName("Should reject blank organization slug")
    void test_07() {
        assertThatThrownBy(() ->
                new Organization("Acme Corporation", " ", FIXED_CLOCK))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Organization slug is required");
    }
    @Test
    @DisplayName("Should reject organization slug exceeding 100 characters")
    void test_08() {
        String slug = "a".repeat(101);

        assertThatThrownBy(() ->
                new Organization("Acme Corporation", slug, FIXED_CLOCK))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Organization slug must not exceed 100 characters");
    }
    @Test
    @DisplayName("Should reject null clock")
    void test_09() {
        assertThatThrownBy(() ->
                new Organization("Acme Corporation", "acme", null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Clock is required");
    }
    @Test
    @DisplayName("Should update organization name")
    void test_10() {
        Organization organization =
                new Organization("Acme Corporation", "acme", FIXED_CLOCK);

        organization.updateName("New Acme");

        assertThat(organization.getName()).isEqualTo("New Acme");
    }
    @Test
    @DisplayName("Should update organization slug")
    void test_11() {
        Organization organization =
                new Organization("Acme Corporation", "acme", FIXED_CLOCK);

        organization.updateSlug("new-acme");

        assertThat(organization.getSlug()).isEqualTo("new-acme");
    }
    @Test
    @DisplayName("Should reject invalid name during update")
    void test_12() {
        Organization organization =
                new Organization("Acme Corporation", "acme", FIXED_CLOCK);

        assertThatThrownBy(() -> organization.updateName(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Organization name is required");

        assertThat(organization.getName()).isEqualTo("Acme Corporation");
    }
    @Test
    @DisplayName("Should reject invalid slug during update")
    void test_13() {
        Organization organization =
                new Organization("Acme Corporation", "acme", FIXED_CLOCK);

        assertThatThrownBy(() -> organization.updateSlug(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Organization slug is required");

        assertThat(organization.getSlug()).isEqualTo("acme");
    }
    @Test
    @DisplayName("Should change organization status")
    void test_14() {
        Organization organization =
                new Organization("Acme Corporation", "acme", FIXED_CLOCK);

        organization.changeStatus(OrganizationStatus.SUSPENDED);

        assertThat(organization.getStatus())
                .isEqualTo(OrganizationStatus.SUSPENDED);
    }
    @Test
    @DisplayName("Should reject null organization status")
    void test_15() {
        Organization organization =
                new Organization("Acme Corporation", "acme", FIXED_CLOCK);

        assertThatThrownBy(() -> organization.changeStatus(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Organization status is required");
    }
    @Test
    @DisplayName("Should initialize timestamps on create")
    void test_16() {
        Organization organization =
                new Organization("Acme Corporation", "acme", FIXED_CLOCK);

        organization.onCreate();

        assertThat(organization.getCreatedAt()).isEqualTo(FIXED_INSTANT);
        assertThat(organization.getUpdatedAt()).isEqualTo(FIXED_INSTANT);
    }
    @Test
    @DisplayName("Should update updatedAt on update")
    void test_17() {
        MutableClock mutableClock = new MutableClock(
                Instant.parse("2026-01-15T10:00:00Z")
        );

        Organization organization =
                new Organization("Acme Corporation", "acme", mutableClock);

        organization.onCreate();

        Instant createdAt = organization.getCreatedAt();
        Instant previousUpdatedAt = organization.getUpdatedAt();

        mutableClock.setInstant(
                Instant.parse("2026-01-15T11:00:00Z")
        );

        organization.updateName("New Acme");
        organization.onUpdate();

        assertThat(organization.getCreatedAt()).isEqualTo(createdAt);

        assertThat(organization.getUpdatedAt())
                .isEqualTo(Instant.parse("2026-01-15T11:00:00Z"))
                .isAfter(previousUpdatedAt);
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
        public java.time.ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }
    }
}
