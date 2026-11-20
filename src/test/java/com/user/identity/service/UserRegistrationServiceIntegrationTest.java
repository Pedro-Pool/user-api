package com.user.identity.service;

import com.user.AbstractPostgresTest;
import com.user.identity.dto.user.UserRegistrationRequest;
import com.user.identity.dto.user.UserRegistrationResponse;
import com.user.identity.entity.User;
import com.user.identity.entity.UserCredential;
import com.user.identity.entity.UserEmail;
import com.user.identity.entity.UserStatus;
import com.user.identity.repository.EmailVerificationTokenRepository;
import com.user.identity.repository.UserCredentialRepository;
import com.user.identity.repository.UserEmailRepository;
import com.user.identity.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@DisplayName("UserRegistrationService integration tests")
class UserRegistrationServiceIntegrationTest extends AbstractPostgresTest {

    @Autowired
    private UserRegistrationService userRegistrationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserCredentialRepository userCredentialRepository;

    @Autowired
    private UserEmailRepository userEmailRepository;

    @Autowired
    private EmailVerificationTokenRepository tokenRepository;

    private String uniqueEmail() {
        return "registration-" + UUID.randomUUID() + "@example.com";
    }

    @Test
    @DisplayName("Should register user with credential and email verification")
    void test_01() {
        String email = uniqueEmail();

        UserRegistrationRequest request = new UserRegistrationRequest(
                "Test User",
                "Test",
                "User",
                email,
                "StrongPassword123!"
        );

        UserRegistrationResponse response =
                userRegistrationService.register(request);

        User persistedUser = userRepository
                .findByIdAndDeletedAtIsNull(response.user().id())
                .orElseThrow();

        assertThat(persistedUser.getDisplayName())
                .isEqualTo("Test User");

        assertThat(persistedUser.getFirstName())
                .isEqualTo("Test");

        assertThat(persistedUser.getLastName())
                .isEqualTo("User");

        assertThat(persistedUser.getCreatedAt())
                .isNotNull();

        assertThat(persistedUser.getStatus())
                .isEqualTo(UserStatus.PENDING_VERIFICATION);

        assertThat(response.user().status())
                .isEqualTo(UserStatus.PENDING_VERIFICATION);

        assertThat(response.user().id())
                .isEqualTo(persistedUser.getId());

        UserCredential persistedCredential = userCredentialRepository
                .findByUser_Id(persistedUser.getId())
                .orElseThrow();

        assertThat(persistedCredential.getUser().getId())
                .isEqualTo(persistedUser.getId());

        assertThat(persistedCredential.getPasswordHash())
                .isNotEqualTo(request.password())
                .isNotBlank();

        UserEmail persistedEmail = userEmailRepository
                .findById(response.emailVerification().emailId())
                .orElseThrow();

        assertThat(persistedEmail.getUser().getId())
                .isEqualTo(persistedUser.getId());

        assertThat(persistedEmail.getEmail())
                .isEqualTo(email);

        assertThat(persistedEmail.getNormalizedEmail())
                .isEqualTo(email);

        assertThat(persistedEmail.getVerifiedAt())
                .isNull();

        assertThat(response.emailVerification().rawToken())
                .isNotBlank();

        assertThat(response.emailVerification().expiresAt())
                .isNotNull();

        assertThat(tokenRepository
                .findAllByUserEmail_IdAndUsedAtIsNullAndRevokedAtIsNull(
                        persistedEmail.getId()
                ))
                .hasSize(1);
    }

    @Test
    @DisplayName("Should reject registration with duplicated email")
    void test_02() {
        String email = uniqueEmail();

        UserRegistrationRequest firstRequest = new UserRegistrationRequest(
                "First User",
                "First",
                "User",
                email,
                "StrongPassword123!"
        );

        UserRegistrationResponse firstResponse =
                userRegistrationService.register(firstRequest);

        Pageable pageable = PageRequest.of(0, 20);

        long usersBeforeSecondRegistration =
                userRepository.findAllByDeletedAtIsNull(pageable).getTotalElements();

        UserRegistrationRequest secondRequest = new UserRegistrationRequest(
                "Second User",
                "Second",
                "User",
                email.toUpperCase(),
                "StrongPassword123!"
        );

        assertThatThrownBy(() ->
                userRegistrationService.register(secondRequest)
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Email is already in use");

        Page<User> usersAfterSecondRegistration =
                userRepository.findAllByDeletedAtIsNull(pageable);

        assertThat(usersAfterSecondRegistration.getTotalElements())
                .isEqualTo(usersBeforeSecondRegistration);

        assertThat(usersAfterSecondRegistration.getContent())
                .extracting(User::getDisplayName)
                .doesNotContain("Second User");

        assertThat(userRepository.findByIdAndDeletedAtIsNull(
                firstResponse.user().id()
        )).isPresent();

        assertThat(userCredentialRepository.findByUser_Id(
                firstResponse.user().id()
        )).isNotNull();

        assertThat(userEmailRepository
                .findByNormalizedEmailAndRetiredAtIsNull(email))
                .isPresent();
    }
    @Test
    @DisplayName("Should rollback registration when password validation fails")
    void test_03() {
        String email = uniqueEmail();

        UserRegistrationRequest request = new UserRegistrationRequest(
                "Invalid Password User",
                "Invalid",
                "Password",
                email,
                "weak"
        );

        Pageable pageable = PageRequest.of(0, 20);

        long usersBefore = userRepository.findAllByDeletedAtIsNull(pageable).getTotalElements();

        long credentialsBefore = userCredentialRepository.count();
        long emailsBefore = userEmailRepository.count();
        long tokensBefore = tokenRepository.count();

        assertThatThrownBy(() ->
                userRegistrationService.register(request)
        )
                .isInstanceOf(
                        com.user.identity.exception.PasswordValidationException.class
                );

        Page<User> usersAfter = userRepository.findAllByDeletedAtIsNull(pageable);

        assertThat(usersAfter.getTotalElements())
                .isEqualTo(usersBefore);

        assertThat(usersAfter.getContent())
                .extracting(User::getDisplayName)
                .doesNotContain("Invalid Password User");

        assertThat(userEmailRepository
                .findByNormalizedEmailAndRetiredAtIsNull(email))
                .isEmpty();

        assertThat(userCredentialRepository.count())
                .isEqualTo(credentialsBefore);

        assertThat(userEmailRepository.count())
                .isEqualTo(emailsBefore);

        assertThat(tokenRepository.count())
                .isEqualTo(tokensBefore);
    }
    @Test
    @DisplayName("Should rollback user and credential creation when email is already in use")
    void test_04() {
        String email = uniqueEmail();

        UserRegistrationRequest firstRequest = new UserRegistrationRequest(
                "First User",
                "First",
                "User",
                email,
                "StrongPassword123!"
        );

        userRegistrationService.register(firstRequest);

        long usersBefore = userRepository.count();
        long credentialsBefore = userCredentialRepository.count();
        long emailsBefore = userEmailRepository.count();
        long tokensBefore = tokenRepository.count();

        UserRegistrationRequest secondRequest = new UserRegistrationRequest(
                "Second User",
                "Second",
                "User",
                email,
                "AnotherStrongPassword123!"
        );

        assertThatThrownBy(() -> userRegistrationService.register(secondRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Email is already in use");

        assertThat(userRepository.count()).isEqualTo(usersBefore);
        assertThat(userCredentialRepository.count()).isEqualTo(credentialsBefore);
        assertThat(userEmailRepository.count()).isEqualTo(emailsBefore);
        assertThat(tokenRepository.count()).isEqualTo(tokensBefore);
    }
}
