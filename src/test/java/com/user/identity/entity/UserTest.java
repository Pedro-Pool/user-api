package com.user.identity.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UserTest {

    @Test
    @DisplayName("Should create user with valid data")
    void test_01(){
        User user = new User("Pedro Silva", "Pedro", "Silva");

        assertThat(user.getDisplayName()).isEqualTo("Pedro Silva");
        assertThat(user.getFirstName()).isEqualTo("Pedro");
        assertThat(user.getLastName()).isEqualTo("Silva");
        assertThat(user.getStatus()).isEqualTo(UserStatus.PENDING_VERIFICATION);
    }
    @Test
    @DisplayName("Should reject invalid display name")
    void test_02(){
        String message = "Display name is required";

        assertThatThrownBy(() ->
                new User(null, "Pedro", "Silva"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(message);

        assertThatThrownBy(() ->
                new User("", "Pedro", "Silva"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(message);

        assertThatThrownBy(() ->
                new User("  ", "Pedro", "Silva"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(message);
    }
    @Test
    @DisplayName("Should valid display name maximum length")
    void test_03(){
        String validDisplayName = "A".repeat(120);
        String invalidDisplayName = "A".repeat(121);

        User user = new User(validDisplayName, "pedro", "silva");

        assertThat(user.getDisplayName()).isEqualTo(validDisplayName);

        assertThatThrownBy(() ->
                new User(invalidDisplayName, "Pedro", "Silva"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Display name must not exceed 120 characters");
    }
    @Test
    @DisplayName("Should reject invalid first name")
    void test_04(){
        String message = "First name is required";

        assertThatThrownBy(() ->
                new User("Pedro Silva", null, "Silva"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(message);

        assertThatThrownBy(() ->
                new User("Pedro Silva", "", "Silva"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(message);

        assertThatThrownBy(() ->
                new User("Pedro Silva", "   ", "Silva"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(message);
    }
    @Test
    @DisplayName("Should valid first name maximum length")
    void test_05(){
        String validFirstName = "A".repeat(80);
        String invalidFirstName = "A".repeat(81);

        User user = new User("Pedro Silva", validFirstName, "silva");

        assertThat(user.getFirstName()).isEqualTo(validFirstName);

        assertThatThrownBy(() ->
                new User("Pedro Silva", invalidFirstName, "Silva"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("First name must not exceed 80 characters");
    }
    @Test
    @DisplayName("Should reject invalid last name")
    void test_06(){
        String message = "Last name is required";

        assertThatThrownBy(() ->
                new User("Pedro Silva", "Pedro", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(message);

        assertThatThrownBy(() ->
                new User("Pedro Silva", "Pedro", ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(message);

        assertThatThrownBy(() ->
                new User("Pedro Silva", "Pedro", "  "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(message);
    }
    @Test
    @DisplayName("Should validate last name maximum length")
    void test_07(){
        String validLastName = "A".repeat(120);
        String invalidLastName = "A".repeat(121);

        User user = new User("Pedro Silva", "Pedro", validLastName);

        assertThat(user.getLastName()).isEqualTo(validLastName);

        assertThatThrownBy(() ->
                new User("Pedro Silva", "Pedro", invalidLastName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Last name must not exceed 120 characters");
    }
    @Test
    @DisplayName("Should update display name")
    void test_08(){
        User user = new User("Pedro Silva", "Pedro", "Silva");

        user.updateDisplayName("Pedro Santos");

        assertThat(user.getDisplayName()).isEqualTo("Pedro Santos");
    }
    @Test
    @DisplayName("Should reject invalid display name update")
    void test_09(){
        User user = new User("Pedro Silva", "Pedro", "Silva");
        String message = "Display name is required";

        assertThatThrownBy(() ->
                user.updateDisplayName(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(message);

        assertThatThrownBy(() ->
                user.updateDisplayName(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(message);

        assertThatThrownBy(() ->
                user.updateDisplayName("  "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(message);

        assertThat(user.getDisplayName()).isEqualTo("Pedro Silva");
    }
    @Test
    @DisplayName("Should reject display name update exceeding maximum length")
    void test_10(){
        User user = new User("Pedro Silva", "Pedro", "Silva");
        String invalidDisplayName = "A".repeat(121);

        assertThatThrownBy(() ->
                user.updateDisplayName(invalidDisplayName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Display name must not exceed 120 characters");

        assertThat(user.getDisplayName()).isEqualTo("Pedro Silva");
    }
    @Test
    @DisplayName("Should update first and last name")
    void test_11(){
        User user = new User("Pedro Silva", "Pedro", "Silva");

        user.updateFirstName("João");
        user.updateLastName("Santos");

        assertThat(user.getFirstName()).isEqualTo("João");
        assertThat(user.getLastName()).isEqualTo("Santos");
    }
    @Test
    @DisplayName("Should reject invalid first name update")
    void test_12() {
        String messageFirstName = "First name is required";

        User user = new User("Pedro Silva", "Pedro", "Silva");

        assertThatThrownBy(() ->
                user.updateFirstName(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(messageFirstName);

        assertThatThrownBy(() ->
                user.updateFirstName(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(messageFirstName);

        assertThatThrownBy(() ->
                user.updateFirstName("  "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(messageFirstName);

        assertThat(user.getFirstName()).isEqualTo("Pedro");
    }
    @Test
    @DisplayName("Should reject invalid last name update")
    void test_13() {
        String messageLastName = "Last name is required";

        User user = new User("Pedro Silva", "Pedro", "Silva");

        assertThatThrownBy(() ->
                user.updateLastName(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(messageLastName);

        assertThatThrownBy(() ->
                user.updateLastName(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(messageLastName);

        assertThatThrownBy(() ->
                user.updateLastName("  "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(messageLastName);

        assertThat(user.getFirstName()).isEqualTo("Pedro");
    }
    @Test
    @DisplayName("Should validate first and last name maximum length")
    void test_14(){
        User user = new User("Pedro Silva", "Pedro", "Silva");
        String validFirstName = "A".repeat(80);
        String validLastName = "A".repeat(120);

        user.updateFirstName(validFirstName);
        user.updateLastName(validLastName);

        assertThat(user.getFirstName()).isEqualTo(validFirstName);
        assertThat(user.getLastName()).isEqualTo(validLastName);
    }
    @Test
    @DisplayName("Should reject first and last name update exceeding maximum length")
    void test_15(){
        User user = new User("Pedro Silva", "Pedro", "Silva");
        String invalidFirstName = "A".repeat(81);
        String invalidLastName = "A".repeat(121);

        assertThatThrownBy(() ->
                user.updateFirstName(invalidFirstName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("First name must not exceed 80 characters");

        assertThatThrownBy(() ->
                user.updateLastName(invalidLastName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Last name must not exceed 120 characters");

        assertThat(user.getFirstName()).isEqualTo("Pedro");
        assertThat(user.getLastName()).isEqualTo("Silva");
    }
    @Test
    @DisplayName("Should active user")
    void test_16(){
        User user = new User("Pedro Silva", "Pedro", "Silva");

        user.activate();

        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }
    @Test
    @DisplayName("Should lock user")
    void test_17(){
        User user = new User("Pedro Silva", "Pedro", "Silva");

        user.lock();

        assertThat(user.getStatus()).isEqualTo(UserStatus.LOCKED);
    }
    @Test
    @DisplayName("Should disable user")
    void test_18(){
        User user = new User("Pedro Silva", "Pedro", "Silva");

        user.disable();

        assertThat(user.getStatus()).isEqualTo(UserStatus.DISABLED);
    }
    @Test
    @DisplayName("Should not activate disabled user")
    void test_19(){
        User user = new User("Pedro Silva", "Pedro", "Silva");

        user.disable();

        assertThatThrownBy(user::activate)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Disabled user cannot be activated");
    }
    @Test
    @DisplayName("Should not lock disable user")
    void test_20(){
        User user = new User("Pedro Silva", "Pedro", "Silva");

        user.disable();

        assertThatThrownBy(user::lock)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Disabled user cannot be locked");
    }
    @Test
    @DisplayName("Should soft delete user")
    void test_21(){
        Clock fixedClock = Clock.fixed(
                Instant.parse("2026-11-16T12:00:00Z"),
                ZoneOffset.UTC
        );
        User user = new User("Pedro Silva", "Pedro", "Silva", fixedClock);

        user.softDelete();

        assertThat(user.getDeletedAt()).isEqualTo(Instant.parse("2026-11-16T12:00:00Z"));
    }
    @Test
    @DisplayName("Should preserve original deletion timestamp")
    void test_22(){
        AtomicReference<Instant> currentTime = new AtomicReference<>(
                Instant.parse("2026-11-16T12:00:00Z")
        );

        Clock clock = Clock.fixed(
                currentTime.get(),
                ZoneOffset.UTC
        );
        User user = new User("Pedro Silva", "Pedro", "Silva", clock);

        user.softDelete();

        Instant originalDeletedAt = user.getDeletedAt();

        user.softDelete();

        assertThat(user.getDeletedAt()).isEqualTo(originalDeletedAt);
    }
    @Test
    @DisplayName("Should preserve user status when soft deleting")
    void test_23(){
        User user = new User("Pedro Silva", "Pedro", "Silva");

        user.softDelete();

        assertThat(user.getStatus()).isEqualTo(UserStatus.PENDING_VERIFICATION);
    }
    @Test
    @DisplayName("Should initialize timestamp on creation")
    void test_24(){
        Instant expectedTime = Instant.parse("2026-11-16T12:00:00Z");

        Clock clock = Clock.fixed(
                expectedTime,
                ZoneOffset.UTC
        );
        User user = new User("Pedro Silva", "Pedro", "Silva", clock);

        user.onCreate();

        assertThat(user.getCreatedAt()).isEqualTo(expectedTime);
        assertThat(user.getUpdatedAt()).isEqualTo(expectedTime);
    }
    @Test
    @DisplayName("Should initialize timestamp on creation")
    void test_25(){
        Instant creationTime = Instant.parse("2026-11-16T12:00:00Z");

        MutableClock clock = new MutableClock(creationTime);

        User user = new User("Pedro Silva", "Pedro", "Silva", clock);

        user.onCreate();

        clock.advanceSeconds(3600);

        user.onUpdate();

        assertThat(user.getCreatedAt()).isEqualTo(creationTime);
        assertThat(user.getUpdatedAt()).isEqualTo(creationTime.plusSeconds(3600));
    }


    private static class MutableClock extends Clock {

        private Instant currentInstant;

        MutableClock(Instant initialInstant) {
            this.currentInstant = initialInstant;
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return Clock.fixed(currentInstant, zone);
        }

        @Override
        public Instant instant() {
            return currentInstant;
        }

        void advanceSeconds(long seconds) {
            currentInstant = currentInstant.plusSeconds(seconds);
        }
    }
}