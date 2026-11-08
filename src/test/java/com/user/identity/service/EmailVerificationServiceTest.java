package com.user.identity.service;

import com.user.identity.entity.EmailVerificationToken;
import com.user.identity.entity.User;
import com.user.identity.entity.UserEmail;
import com.user.identity.repository.EmailVerificationTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailVerificationServiceTest {

    @Mock
    private EmailVerificationTokenRepository tokenRepository;

    private EmailVerificationService service;

    private final Instant fixedInstant = Instant.parse("2026-11-07T12:00:00Z");

    @BeforeEach
    void setUp(){
        Clock clock = Clock.fixed(fixedInstant, ZoneOffset.UTC);

        service = new EmailVerificationService(tokenRepository, clock);
    }

    @Test
    @DisplayName("Should return URL-safe Base64 token")
    void test_01(){
        UserEmail userEmail = createUserEmail();

        String rawToken = service.createToken(userEmail);

        assertThat(rawToken).hasSize(43).matches("[A-Za-z0-9_-]{43}");

        byte[] decodedToken = Base64.getUrlDecoder().decode(rawToken);

        assertThat(decodedToken).hasSize(32);
    }
    @Test
    @DisplayName("Should persist token hash instead of raw token")
    void test_02() throws Exception{
        UserEmail userEmail = createUserEmail();

        String encodedToken = service.createToken(userEmail);

        byte[] rawToken = Base64.getUrlDecoder().decode(encodedToken);

        byte[] expectedHash = MessageDigest.getInstance("SHA-256").digest(rawToken);

        ArgumentCaptor<EmailVerificationToken> captor = ArgumentCaptor.forClass(EmailVerificationToken.class);

        verify(tokenRepository).save(captor.capture());

        EmailVerificationToken persistedToken = captor.getValue();

        assertThat(persistedToken.getTokenHash())
                .hasSize(32)
                .isEqualTo(expectedHash)
                .isNotEqualTo(rawToken);
    }
    @Test
    @DisplayName("Should set expiration to 15 minutes")
    void test_03(){
        UserEmail userEmail = createUserEmail();

        service.createToken(userEmail);

        ArgumentCaptor<EmailVerificationToken> captor = ArgumentCaptor.forClass(EmailVerificationToken.class);

        verify(tokenRepository).save(captor.capture());

        EmailVerificationToken persistedToken = captor.getValue();

        assertThat(persistedToken.getExpiresAt())
                .isEqualTo(fixedInstant.plusSeconds(900));
    }
    @Test
    @DisplayName("Should consume valid token and verify email")
    void test_04()throws Exception{
        Clock fixedClock = Clock.fixed(fixedInstant, ZoneOffset.UTC);
        UserEmail userEmail = new UserEmail(
                new User("test", "pedro", "silva"),
                "rawEmail@email.com",
                fixedClock
        );

        byte[] rawToken = new byte[32];
        Arrays.fill(rawToken, (byte) 1);

        String encodedToken = Base64
                .getUrlEncoder()
                .withoutPadding()
                .encodeToString(rawToken);

        byte[] tokenHash = MessageDigest.getInstance("SHA-256").digest(rawToken);

        EmailVerificationToken token = new EmailVerificationToken(
                userEmail,
                tokenHash,
                fixedInstant.plusSeconds(900),
                fixedClock
        );

        when(tokenRepository
                .findByTokenHashAndUsedAtIsNullAndRevokedAtIsNull(tokenHash))
                .thenReturn(Optional.of(token));

        service.verifyToken(encodedToken);

        assertThat(token.getUsedAt()).isEqualTo(fixedInstant);
        assertThat(userEmail.getVerifiedAt()).isEqualTo(fixedInstant);
    }
    @Test
    @DisplayName("Should reject nonexistent token")
    void test_05()throws Exception{
        byte[] rawToken = new byte[32];
        Arrays.fill(rawToken, (byte) 2);

        String encodedToken = Base64
                .getUrlEncoder()
                .withoutPadding()
                .encodeToString(rawToken);

        byte[] tokenHash = MessageDigest.getInstance("SHA-256").digest(rawToken);

        when(tokenRepository
                .findByTokenHashAndUsedAtIsNullAndRevokedAtIsNull(tokenHash))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.verifyToken(encodedToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid or expired token");
    }
    @Test
    @DisplayName("Should reject malformed token")
    void test_06(){
        assertThatThrownBy(() -> service.verifyToken("invalid-token"))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(tokenRepository);
    }
    @Test
    @DisplayName("Should reject expires token")
    void test_07()throws Exception {
        MutableClock mutableClock = new MutableClock(fixedInstant);

        service = new EmailVerificationService(tokenRepository, mutableClock);

        UserEmail userEmail = createUserEmail();

        byte[] rawToken = new byte[32];
        Arrays.fill(rawToken, (byte) 3);

        String encodedToken = Base64
                .getUrlEncoder()
                .withoutPadding()
                .encodeToString(rawToken);
        byte[] tokenHash = MessageDigest.getInstance("SHA-256").digest(rawToken);

        EmailVerificationToken token = new EmailVerificationToken(
                userEmail,
                tokenHash,
                fixedInstant.plusSeconds(900),
                mutableClock
        );
        when(tokenRepository
                .findByTokenHashAndUsedAtIsNullAndRevokedAtIsNull(tokenHash))
                .thenReturn(Optional.of(token));

        mutableClock.advanceSeconds(901);

        assertThatThrownBy(() -> service.verifyToken(encodedToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid or expired token");

        assertThat(token.getUsedAt()).isNull();
        assertThat(userEmail.getVerifiedAt()).isNull();
    }
    @Test
    @DisplayName("Should reject previously consumed token")
    void test_08() throws Exception{
        byte[] rawToken = new byte[32];
        Arrays.fill(rawToken, (byte) 4);

        String encodedToken = Base64
                .getUrlEncoder()
                .withoutPadding()
                .encodeToString(rawToken);

        byte[] tokenHash = MessageDigest.getInstance("SHA-256").digest(rawToken);

        when(tokenRepository
                .findByTokenHashAndUsedAtIsNullAndRevokedAtIsNull(tokenHash))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.verifyToken(encodedToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid or expired token");

        verify(tokenRepository).findByTokenHashAndUsedAtIsNullAndRevokedAtIsNull(tokenHash);
    }

    private UserEmail createUserEmail() {
        User user = new User("Test", "pedro", "silva");

        return new UserEmail(user, "rawEmail@email.com");
    }
    private static class MutableClock extends Clock {

        private Instant instant;

        MutableClock(Instant instant) {
            this.instant = instant;
        }

        void advanceSeconds(long seconds) {
            instant = instant.plusSeconds(seconds);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
