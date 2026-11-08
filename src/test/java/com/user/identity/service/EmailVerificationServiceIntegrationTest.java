
package com.user.identity.service;

import com.user.AbstractPostgresTest;
import com.user.identity.entity.EmailVerificationToken;
import com.user.identity.entity.User;
import com.user.identity.entity.UserEmail;
import com.user.identity.repository.EmailVerificationTokenRepository;
import com.user.identity.repository.UserEmailRepository;
import com.user.identity.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.security.MessageDigest;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class EmailVerificationServiceIntegrationTest extends AbstractPostgresTest {

    @Autowired
    private EmailVerificationService verificationService;

    @Autowired
    private EmailVerificationTokenRepository tokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserEmailRepository userEmailRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    @DisplayName("Only one transaction should consume the same token")
    void test_01() throws Exception {

        User user = userRepository.saveAndFlush(
                new User("Test", "pedro", "silva")
        );

        UserEmail userEmail = userEmailRepository.saveAndFlush(
                new UserEmail(
                        user,
                        "concurrency-" + UUID.randomUUID() + "@email.com"
                )
        );

        String rawToken = verificationService.createToken(userEmail);

        byte[] tokenHash = MessageDigest.getInstance("SHA-256")
                .digest(Base64.getUrlDecoder().decode(rawToken));

        TransactionTemplate transactionTemplate =
                new TransactionTemplate(transactionManager);

        CountDownLatch lockAcquired = new CountDownLatch(1);
        CountDownLatch releaseFirstTransaction = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Future<?> firstTransaction = executor.submit(() ->
                    transactionTemplate.execute(status -> {

                        EmailVerificationToken token = tokenRepository
                                .findByTokenHashAndUsedAtIsNullAndRevokedAtIsNull(tokenHash)
                                .orElseThrow(() ->
                                        new IllegalArgumentException("Active token not found"));

                        lockAcquired.countDown();

                        await(releaseFirstTransaction);

                        token.consume();
                        token.getUserEmail().verify();

                        return null;
                    })
            );

            assertThat(lockAcquired.await(5, TimeUnit.SECONDS))
                    .isTrue();

            CountDownLatch secondStarted = new CountDownLatch(1);

            Future<Boolean> secondTransaction = executor.submit(() -> {
                secondStarted.countDown();

                try {
                    verificationService.verifyToken(rawToken);
                    return true;
                } catch (IllegalArgumentException exception) {
                    return false;
                }
            });

            assertThat(secondStarted.await(5, TimeUnit.SECONDS))
                    .isTrue();

            try {
                secondTransaction.get(500, TimeUnit.MILLISECONDS);
                throw new AssertionError(
                        "Second transaction should still be waiting"
                );
            } catch (TimeoutException expected) {
            }

            releaseFirstTransaction.countDown();
            firstTransaction.get(5, TimeUnit.SECONDS);

            assertThat(secondTransaction.get(5, TimeUnit.SECONDS))
                    .isFalse();

        } finally {
            releaseFirstTransaction.countDown();
            executor.shutdownNow();
        }

        EmailVerificationToken persistedToken = transactionTemplate.execute(status ->
                        tokenRepository.findAllByUserEmail_Id(userEmail.getId())
                                .stream()
                                .filter(token -> MessageDigest.isEqual(
                                        token.getTokenHash(), tokenHash
                                ))
                                .findFirst()
                                .orElseThrow()
                );


        assertThat(persistedToken.getUsedAt()).isNotNull();

        UserEmail persistedEmail = userEmailRepository
                .findById(userEmail.getId())
                .orElseThrow();

        assertThat(persistedEmail.getVerifiedAt()).isNotNull();
    }

    private void await(CountDownLatch latch) {
        try {
            if (!latch.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timed out waiting for latch");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "Thread interrupted while waiting",
                    exception
            );
        }
    }
}
