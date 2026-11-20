
package com.user.identity.service;

import com.user.identity.dto.user.UserRegistrationRequest;
import com.user.identity.dto.user.UserRegistrationResponse;
import com.user.identity.entity.User;
import com.user.identity.exception.PasswordValidationException;
import com.user.identity.repository.UserRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRegistrationServiceTest {

    private static final String DISPLAY_NAME = "Pedro";
    private static final String FIRST_NAME = "Pedro";
    private static final String LAST_NAME = "Silva";
    private static final String EMAIL = "pedro@example.com";
    private static final String PASSWORD = "StrongPassword123!";

    @Mock
    private Validator validator;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserCredentialService userCredentialService;

    @Mock
    private UserEmailService userEmailService;

    private UUID generatedUserId;

    private EmailVerificationChallenge challenge;

    private void stubValidRegistration(){
        when(validator.validate(any(UserRegistrationRequest.class)))
                .thenReturn(Set.of());

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);
                    setUserId(user, generatedUserId);
                    return user;
                });
    }

    @BeforeEach
    void setUp() {
        generatedUserId = UUID.randomUUID();

        challenge = new EmailVerificationChallenge(
                UUID.randomUUID(),
                "verification-token",
                java.time.Instant.now().plusSeconds(900)
        );
    }
    @Test
    @DisplayName("Should register user successfully")
    void test_01() {
        stubValidRegistration();

        when(userEmailService.registerEmail(generatedUserId, EMAIL))
                .thenReturn(challenge);

        UserRegistrationRequest request = validRequest();

        UserRegistrationResponse response =
                createService().register(request);

        assertThat(response).isNotNull();
        assertThat(response.user().id()).isEqualTo(generatedUserId);
        assertThat(response.user().displayName()).isEqualTo(DISPLAY_NAME);
        assertThat(response.user().firstName()).isEqualTo(FIRST_NAME);
        assertThat(response.user().lastName()).isEqualTo(LAST_NAME);
        assertThat(response.user().status().name())
                .isEqualTo("PENDING_VERIFICATION");
        assertThat(response.emailVerification()).isEqualTo(challenge);

        verify(userRepository).save(any(User.class));
        verify(userCredentialService)
                .createCredential(any(User.class), eq(PASSWORD));
        verify(userEmailService)
                .registerEmail(generatedUserId, EMAIL);
    }
    @Test
    @DisplayName("Should reject null registration request")
    void test_02() {
        assertThatThrownBy(() -> createService().register(null))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(userRepository);
        verifyNoInteractions(userCredentialService);
        verifyNoInteractions(userEmailService);
    }
    @Test
    @DisplayName("Should reject invalid display name")
    void test_03() {
        UserRegistrationRequest request = new UserRegistrationRequest(
                "",
                FIRST_NAME,
                LAST_NAME,
                EMAIL,
                PASSWORD
        );

        stubValidationFailure("Display name is required");

        assertThatThrownBy(() -> createService().register(request))
                .isInstanceOf(ConstraintViolationException.class);

        verifyNoInteractions(userRepository);
        verifyNoInteractions(userCredentialService);
        verifyNoInteractions(userEmailService);
    }
    @Test
    @DisplayName("Should reject invalid first name")
    void test_04() {
        UserRegistrationRequest request = new UserRegistrationRequest(
                DISPLAY_NAME,
                "",
                LAST_NAME,
                EMAIL,
                PASSWORD
        );

        stubValidationFailure("First name is required");

        assertThatThrownBy(() -> createService().register(request))
                .isInstanceOf(ConstraintViolationException.class);

        verifyNoInteractions(userRepository);
        verifyNoInteractions(userCredentialService);
        verifyNoInteractions(userEmailService);
    }
    @Test
    @DisplayName("Should reject invalid last name")
    void test_05() {
        UserRegistrationRequest request = new UserRegistrationRequest(
                DISPLAY_NAME,
                FIRST_NAME,
                "",
                EMAIL,
                PASSWORD
        );

        stubValidationFailure("Last name is required");

        assertThatThrownBy(() -> createService().register(request))
                .isInstanceOf(ConstraintViolationException.class);

        verifyNoInteractions(userRepository);
        verifyNoInteractions(userCredentialService);
        verifyNoInteractions(userEmailService);
    }
    @Test
    @DisplayName("Should reject invalid email")
    void test_06() {
        UserRegistrationRequest request = new UserRegistrationRequest(
                DISPLAY_NAME,
                FIRST_NAME,
                LAST_NAME,
                "invalid-email",
                PASSWORD
        );

        stubValidationFailure("Email must be valid");

        assertThatThrownBy(() -> createService().register(request))
                .isInstanceOf(ConstraintViolationException.class);

        verifyNoInteractions(userRepository);
        verifyNoInteractions(userCredentialService);
        verifyNoInteractions(userEmailService);
    }
    @Test
    @DisplayName("Should reject invalid password")
    void test_07() {
        stubValidRegistration();

        UserRegistrationRequest request = validRequest();

        doThrow(new PasswordValidationException(
                List.of("Password does not meet security requirements")
        )).when(userCredentialService)
                .createCredential(any(User.class), eq(PASSWORD));

        assertThatThrownBy(() -> createService().register(request))
                .isInstanceOf(PasswordValidationException.class);

        verify(userRepository).save(any(User.class));
        verify(userCredentialService)
                .createCredential(any(User.class), eq(PASSWORD));

        verifyNoInteractions(userEmailService);
    }
    @Test
    @DisplayName("Should propagate duplicate email exception")
    void test_08() {
        stubValidRegistration();

        UserRegistrationRequest request = validRequest();

        when(userEmailService.registerEmail(generatedUserId, EMAIL))
                .thenThrow(new IllegalStateException("Email is already in use"));

        assertThatThrownBy(() -> createService().register(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Email is already in use");

        verify(userRepository).save(any(User.class));
        verify(userCredentialService)
                .createCredential(any(User.class), eq(PASSWORD));
        verify(userEmailService)
                .registerEmail(generatedUserId, EMAIL);
    }
    @Test
    @DisplayName("Should not register email when credential creation fails")
    void test_09() {
        stubValidRegistration();

        UserRegistrationRequest request = validRequest();

        doThrow(new IllegalStateException("Credential creation failed"))
                .when(userCredentialService)
                .createCredential(any(User.class), eq(PASSWORD));

        assertThatThrownBy(() -> createService().register(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Credential creation failed");

        verify(userRepository).save(any(User.class));
        verify(userCredentialService)
                .createCredential(any(User.class), eq(PASSWORD));

        verifyNoInteractions(userEmailService);
    }
    @Test
    @DisplayName("Should propagate email registration failure")
    void test_10() {
        stubValidRegistration();

        UserRegistrationRequest request = validRequest();

        when(userEmailService.registerEmail(generatedUserId, EMAIL))
                .thenThrow(new IllegalStateException("Email registration failed"));

        assertThatThrownBy(() -> createService().register(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Email registration failed");

        verify(userRepository).save(any(User.class));
        verify(userCredentialService)
                .createCredential(any(User.class), eq(PASSWORD));
        verify(userEmailService)
                .registerEmail(generatedUserId, EMAIL);
    }

    private UserRegistrationRequest validRequest() {
        return new UserRegistrationRequest(
                DISPLAY_NAME,
                FIRST_NAME,
                LAST_NAME,
                EMAIL,
                PASSWORD
        );
    }

    private void stubValidationFailure(String message) {
        @SuppressWarnings("unchecked")
        ConstraintViolation<UserRegistrationRequest> violation =
                mock(ConstraintViolation.class);

        when(violation.getMessage()).thenReturn(message);

        when(validator.validate(any(UserRegistrationRequest.class)))
                .thenReturn(Set.of(violation));
    }

    private UserRegistrationService createService() {
        return new UserRegistrationService(
                validator,
                userRepository,
                userCredentialService,
                userEmailService
        );
    }

    private void setUserId(User user, UUID id) {
        try {
            Field field = User.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(user, id);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
