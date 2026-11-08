package com.user.identity.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserCredentialTest {
    private User user;
    private MutableClock clock;

    private final Instant initialInstant = Instant.parse("2026-11-07T12:00:00Z");

    @BeforeEach
    void setUp(){
        user = new User("test", "pedro", "silva");
        clock = new MutableClock(initialInstant);
    }

    @Test
    @DisplayName("Should create credential with user and password hash")
    void test_01(){
        String passwordHash = "encoded-password-hash";

        UserCredential credential = new UserCredential(
                user,
                passwordHash,
                clock
        );

        assertThat(credential.getUser()).isEqualTo(user);
        assertThat(credential.getPasswordHash()).isEqualTo(passwordHash);
        assertThat(credential.getPasswordChangedAt()).isEqualTo(initialInstant);
        assertThat(credential.isMustChangePassword()).isFalse();
    }
    @Test
    @DisplayName("Should reject null user")
    void test_02(){
        assertThatThrownBy(() ->
                new UserCredential(null, "encode-hash", clock))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User is required");
    }
    @Test
    @DisplayName("Should reject invalid password hash")
    void test_03(){
        assertThatThrownBy(() ->
                new UserCredential(user, null, clock))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid password hash");

        assertThatThrownBy(() ->
                new UserCredential(user, "   ", clock))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid password hash");

        assertThatThrownBy(() ->
                new UserCredential(user, "a".repeat(513), clock))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid password hash");
    }
    @Test
    @DisplayName("Should update password hash and change timestamp")
    void test_04(){
        UserCredential credential = new UserCredential(
                user,
                "old-password-hash",
                clock
        );
        Instant changeAt = initialInstant.plusSeconds(60);
        clock.advanceSeconds(60);

        credential.changePassword("new-password-hash");

        assertThat(credential.getPasswordHash()).isEqualTo("new-password-hash");
        assertThat(credential.getPasswordChangedAt()).isEqualTo(changeAt);
    }
    @Test
    @DisplayName("Should require password change")
    void test_05(){
        UserCredential credential = new UserCredential(
                user,
                "encoded-password-hash",
                clock
        );
        credential.requirePasswordChange();

        assertThat(credential.isMustChangePassword()).isTrue();

        credential.changePassword("new-password-hash");

        assertThat(credential.isMustChangePassword()).isFalse();
    }
    @Test
    @DisplayName("Should configure password expiration")
    void test_06(){
        UserCredential credential = new UserCredential(
                user,
                "encoded-password-hash",
                clock
        );
        Instant expiration = initialInstant.plusSeconds(3600);

        credential.setPasswordExpiration(expiration);

        assertThat(credential.getPasswordExpiresAt()).isEqualTo(expiration);
    }
    @Test
    @DisplayName("Should reject password expiration in the past")
    void test_07(){
        UserCredential credential = new UserCredential(
                user,
                "encoded-password-hash",
                clock
        );
        Instant expiration = initialInstant.minusSeconds(60);

        assertThatThrownBy(() ->
                credential.setPasswordExpiration(expiration))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password expiration must be in the future");
    }
    @Test
    @DisplayName("Should allow removing password expiration")
    void test_08(){
        UserCredential credential = new UserCredential(
                user,
                "encoded-password-hash",
                clock
        );

        credential.setPasswordExpiration(initialInstant.plusSeconds(3600));
        credential.setPasswordExpiration(null);

        assertThat(credential.getPasswordExpiresAt()).isNull();
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
