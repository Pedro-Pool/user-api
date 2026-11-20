package com.user.identity.service;

import com.user.AbstractPostgresTest;
import com.user.identity.entity.User;
import com.user.identity.entity.UserEmail;
import com.user.identity.repository.EmailVerificationTokenRepository;
import com.user.identity.repository.UserEmailRepository;
import com.user.identity.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@DisplayName("UserEmailService integration tests")
class UserEmailServiceIntegrationTest extends AbstractPostgresTest {

    @Autowired
    private UserEmailService userEmailService;

    @Autowired
    private EmailVerificationService verificationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserEmailRepository userEmailRepository;

    @Autowired
    private EmailVerificationTokenRepository tokenRepository;

    private User createUser() {
        return userRepository.saveAndFlush(
                new User("Test User", "Test", "User")
        );
    }

    private String uniqueEmail() {
        return "test-" + UUID.randomUUID() + "@example.com";
    }
    @Test
    @DisplayName("Should persist registered email")
    void test_01() {
        User user = createUser();
        String email = uniqueEmail();

        EmailVerificationChallenge challenge =
                userEmailService.registerEmail(user.getId(), email);

        UserEmail persistedEmail = userEmailRepository
                .findById(challenge.emailId())
                .orElseThrow();

        assertThat(persistedEmail.getId()).isEqualTo(challenge.emailId());
        assertThat(persistedEmail.getUser().getId()).isEqualTo(user.getId());
        assertThat(persistedEmail.getEmail()).isEqualTo(email);
        assertThat(persistedEmail.getNormalizedEmail()).isEqualTo(email);
        assertThat(persistedEmail.getVerifiedAt()).isNull();
        assertThat(persistedEmail.getRetiredAt()).isNull();
        assertThat(persistedEmail.isPrimary()).isFalse();
        assertThat(persistedEmail.getCreatedAt()).isNotNull();

        assertThat(challenge.rawToken()).isNotBlank();
        assertThat(challenge.expiresAt()).isNotNull();
    }
    @Test
    @DisplayName("Should normalize email before persistence")
    void test_02() {
        User user = createUser();

        EmailVerificationChallenge challenge =
                userEmailService.registerEmail(
                        user.getId(),
                        "  TEST@EXAMPLE.COM  "
                );

        UserEmail persistedEmail = userEmailRepository
                .findById(challenge.emailId())
                .orElseThrow();

        assertThat(persistedEmail.getEmail())
                .isEqualTo("TEST@EXAMPLE.COM");

        assertThat(persistedEmail.getNormalizedEmail())
                .isEqualTo("test@example.com");
    }
    @Test
    @DisplayName("Should reject duplicated email")
    void test_03() {
        User user = createUser();

        String email = uniqueEmail();

        userEmailService.registerEmail(user.getId(), email);

        assertThatThrownBy(() ->
                userEmailService.registerEmail(
                        user.getId(),
                        email.toUpperCase()
                ))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Email is already in use");

        assertThat(userEmailRepository
                .findByNormalizedEmailAndRetiredAtIsNull(email))
                .isPresent();
    }
    @Test
    @DisplayName("Should register different emails for different users")
    void test_04() {
        User firstUser = createUser();
        User secondUser = createUser();

        String firstEmail = uniqueEmail();
        String secondEmail = uniqueEmail();

        EmailVerificationChallenge firstChallenge =
                userEmailService.registerEmail(
                        firstUser.getId(),
                        firstEmail
                );

        EmailVerificationChallenge secondChallenge =
                userEmailService.registerEmail(
                        secondUser.getId(),
                        secondEmail
                );

        UserEmail persistedFirstEmail = userEmailRepository
                .findById(firstChallenge.emailId())
                .orElseThrow();

        UserEmail persistedSecondEmail = userEmailRepository
                .findById(secondChallenge.emailId())
                .orElseThrow();

        assertThat(persistedFirstEmail.getUser().getId())
                .isEqualTo(firstUser.getId());

        assertThat(persistedSecondEmail.getUser().getId())
                .isEqualTo(secondUser.getId());

        assertThat(persistedFirstEmail.getId())
                .isNotEqualTo(persistedSecondEmail.getId());

        assertThat(persistedFirstEmail.getNormalizedEmail())
                .isEqualTo(firstEmail);

        assertThat(persistedSecondEmail.getNormalizedEmail())
                .isEqualTo(secondEmail);
    }
    @Test
    @DisplayName("Should revoke previous verification token when resending")
    void test_05() {
        User user = createUser();

        EmailVerificationChallenge firstChallenge =
                userEmailService.registerEmail(
                        user.getId(),
                        uniqueEmail()
                );

        EmailVerificationChallenge secondChallenge =
                userEmailService.resendVerification(
                        user.getId(),
                        firstChallenge.emailId()
                );

        assertThat(secondChallenge.rawToken())
                .isNotEqualTo(firstChallenge.rawToken());

        assertThat(tokenRepository
                .findAllByUserEmail_IdAndUsedAtIsNullAndRevokedAtIsNull(
                        firstChallenge.emailId()
                ))
                .hasSize(1);

        assertThatThrownBy(() ->
                verificationService.verifyToken(firstChallenge.rawToken()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid or expired token");

        verificationService.verifyToken(secondChallenge.rawToken());

        UserEmail verifiedEmail = userEmailRepository
                .findById(firstChallenge.emailId())
                .orElseThrow();

        assertThat(verifiedEmail.getVerifiedAt()).isNotNull();

        assertThat(tokenRepository
                .findAllByUserEmail_IdAndUsedAtIsNullAndRevokedAtIsNull(
                        firstChallenge.emailId()
                ))
                .isEmpty();
    }
    @Test
    @DisplayName("Should set and replace primary email")
    void test_06() {
        User user = createUser();

        EmailVerificationChallenge firstChallenge =
                userEmailService.registerEmail(
                        user.getId(),
                        uniqueEmail()
                );

        verificationService.verifyToken(firstChallenge.rawToken());

        userEmailService.setPrimaryEmail(
                user.getId(),
                firstChallenge.emailId()
        );

        EmailVerificationChallenge secondChallenge =
                userEmailService.registerEmail(
                        user.getId(),
                        uniqueEmail()
                );

        verificationService.verifyToken(secondChallenge.rawToken());

        userEmailService.setPrimaryEmail(
                user.getId(),
                secondChallenge.emailId()
        );

        UserEmail firstEmail = userEmailRepository
                .findById(firstChallenge.emailId())
                .orElseThrow();

        UserEmail secondEmail = userEmailRepository
                .findById(secondChallenge.emailId())
                .orElseThrow();

        assertThat(firstEmail.isPrimary()).isFalse();
        assertThat(secondEmail.isPrimary()).isTrue();

        UserEmail persistedPrimaryEmail = userEmailRepository
                .findByUserIdAndPrimaryTrueAndRetiredAtIsNull(user.getId())
                .orElseThrow();

        assertThat(persistedPrimaryEmail.getId())
                .isEqualTo(secondChallenge.emailId());
    }
    @Test
    @DisplayName("Should retire email and persist retired state")
    void test_07() {
        User user = createUser();

        EmailVerificationChallenge challenge =
                userEmailService.registerEmail(
                        user.getId(),
                        uniqueEmail()
                );

        verificationService.verifyToken(challenge.rawToken());

        userEmailService.setPrimaryEmail(
                user.getId(),
                challenge.emailId()
        );

        userEmailService.retireEmail(
                user.getId(),
                challenge.emailId()
        );

        UserEmail retiredEmail = userEmailRepository
                .findById(challenge.emailId())
                .orElseThrow();

        assertThat(retiredEmail.getRetiredAt()).isNotNull();
        assertThat(retiredEmail.isPrimary()).isFalse();

        assertThat(userEmailRepository
                .findByIdAndUser_IdAndRetiredAtIsNull(
                        challenge.emailId(),
                        user.getId()
                ))
                .isEmpty();

        assertThat(userEmailRepository
                .findByNormalizedEmailAndRetiredAtIsNull(
                        retiredEmail.getNormalizedEmail()
                ))
                .isEmpty();
    }
    @Test
    @DisplayName("Should prevent one user from modifying another user's email")
    void test_08() {
        User owner = createUser();
        User anotherUser = createUser();

        EmailVerificationChallenge challenge =
                userEmailService.registerEmail(
                        owner.getId(),
                        uniqueEmail()
                );

        assertThatThrownBy(() ->
                userEmailService.retireEmail(
                        anotherUser.getId(),
                        challenge.emailId()
                ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Active email not found for user");

        UserEmail persistedEmail = userEmailRepository
                .findById(challenge.emailId())
                .orElseThrow();

        assertThat(persistedEmail.getUser().getId())
                .isEqualTo(owner.getId());

        assertThat(persistedEmail.getRetiredAt()).isNull();

        Pageable pageable = PageRequest.of(0, 20);

        assertThat(userEmailRepository
                .findAllByUserIdAndRetiredAtIsNull(owner.getId(), pageable)
                .getContent())
                .hasSize(1);

        assertThat(userEmailRepository
                .findAllByUserIdAndRetiredAtIsNull(anotherUser.getId(), pageable)
                .getContent())
                .isEmpty();
    }
    @Test
    @DisplayName("Should enforce active normalized email uniqueness at database level")
    void test_09() {
        User firstUser = createUser();
        User secondUser = createUser();

        String email = uniqueEmail();

        UserEmail firstEmail = new UserEmail(firstUser, email);
        userEmailRepository.saveAndFlush(firstEmail);

        UserEmail duplicateEmail = new UserEmail(
                secondUser,
                "  " + email.toUpperCase() + "  "
        );

        assertThat(duplicateEmail.getNormalizedEmail())
                .isEqualTo(email);

        assertThatThrownBy(() ->
                userEmailRepository.saveAndFlush(duplicateEmail))
                .isInstanceOf(DataIntegrityViolationException.class);

        assertThat(userEmailRepository
                .findByNormalizedEmailAndRetiredAtIsNull(email))
                .hasValueSatisfying(persistedEmail ->
                        assertThat(persistedEmail.getUser().getId())
                                .isEqualTo(firstUser.getId()));
    }
}
