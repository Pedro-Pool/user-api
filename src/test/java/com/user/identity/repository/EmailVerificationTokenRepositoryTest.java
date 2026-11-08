
package com.user.identity.repository;

import com.user.AbstractPostgresTest;
import com.user.identity.entity.EmailVerificationToken;
import com.user.identity.entity.User;
import com.user.identity.entity.UserEmail;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class EmailVerificationTokenRepositoryTest extends AbstractPostgresTest {

    @Autowired
    private EmailVerificationTokenRepository tokenRepository;

    @Autowired
    private UserEmailRepository userEmailRepository;

    @Autowired
    private UserRepository userRepository;


    @Test
    @DisplayName("Should consume active token")
    void test_01() {
        UserEmail userEmail = createUserEmail();

        EmailVerificationToken token = new EmailVerificationToken(
                userEmail,
                new byte[32],
                Instant.now().plusSeconds(900));

        token.consume();

        assertThat(token.getUsedAt()).isNotNull();
        assertThat(token.isActive()).isFalse();
    }
    @Test
    @DisplayName("Should not consume token twice")
    void test_02(){
        UserEmail userEmail = createUserEmail();

        EmailVerificationToken token = new EmailVerificationToken(
                userEmail,
                new byte[32],
                Instant.now().plusSeconds(900));

        token.consume();

        assertThatThrownBy(token::consume)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Token is no longer active");
    }
    @Test
    @DisplayName("Should not consume revoked token")
    void test_03(){
        UserEmail userEmail = createUserEmail();

        EmailVerificationToken token = new EmailVerificationToken(
                userEmail,
                new byte[32],
                Instant.now().plusSeconds(900));

        token.revoke();

        assertThat(token.getUsedAt()).isNull();
        assertThat(token.isActive()).isFalse();

        assertThatThrownBy(token::consume)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Token is no longer active");
    }
    @Test
    @DisplayName("Should not consume expired token")
    void test_04(){
        UserEmail userEmail = createUserEmail();

        Instant initialInstant = Instant.parse("2026-11-07T12:00:00Z");
        MutableClock clock = new MutableClock(initialInstant);

        EmailVerificationToken token = new EmailVerificationToken(
                userEmail,
                new byte[32],
                initialInstant.plusSeconds(60),
                clock);

        clock.advanceSeconds(61);

        assertThat(token.getUsedAt()).isNull();
        assertThat(token.isActive()).isFalse();

        assertThatThrownBy(token::consume)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Token is no longer active");
    }

    private UserEmail createUserEmail() {
        User user = new User("Test", "pedro", "silva");
        user = userRepository.saveAndFlush(user);

        UserEmail userEmail = new UserEmail(user, "rawEmail@email.com");
        userEmail = userEmailRepository.saveAndFlush(userEmail);

        return userEmail;
    }
    private static class MutableClock extends Clock {
        private Instant currentInstant;

        public MutableClock(Instant initialInstant){
            this.currentInstant = initialInstant;
        }
        @Override
        public ZoneId getZone(){
            return ZoneOffset.UTC;
        }
        @Override
        public Clock withZone(ZoneId zone){
            throw new UnsupportedOperationException();
        }
        @Override
        public Instant instant(){
            return currentInstant;
        }

        public void advanceSeconds(long seconds){
            currentInstant = currentInstant.plusSeconds(seconds);
        }
    }
}

