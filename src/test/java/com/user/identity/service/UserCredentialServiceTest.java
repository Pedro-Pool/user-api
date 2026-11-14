package com.user.identity.service;

import com.user.identity.entity.User;
import com.user.identity.entity.UserCredential;
import com.user.identity.exception.PasswordValidationException;
import com.user.identity.repository.UserCredentialRepository;
import com.user.identity.validation.password.PasswordValidationResult;
import com.user.identity.validation.password.RegistrationPasswordValidation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class UserCredentialServiceTest {

    private UserCredentialService userCredentialService;
    private RegistrationPasswordValidation passwordValidation;
    private PasswordService passwordService;
    private UserCredentialRepository userCredentialRepository;
    private Clock clock;

    @BeforeEach
    void setUp(){
        passwordValidation = mock(RegistrationPasswordValidation.class);
        passwordService = mock(PasswordService.class);
        userCredentialRepository = mock(UserCredentialRepository.class);
        clock = Clock.systemUTC();

        userCredentialService = new UserCredentialService(
                passwordValidation,
                passwordService,
                userCredentialRepository,
                clock
        );
    }
    @Test
    @DisplayName("Should create and persist credential with valid password")
    void test_01(){
        User user = new User("test", "pedro", "silva");
        String rawPassword = "StrongPassword@123";
        String passwordHash = "argon2id-encoded-hash";

        when(passwordValidation.validate(rawPassword)).thenReturn(PasswordValidationResult.success());
        when(passwordService.encode(rawPassword)).thenReturn(passwordHash);
        when(userCredentialRepository.save(any(UserCredential.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserCredential result = userCredentialService.createCredential(
                user,
                rawPassword
        );

        assertThat(result).isNotNull();

        verify(passwordValidation).validate(rawPassword);
        verify(passwordService).encode(rawPassword);
        ArgumentCaptor<UserCredential> credentialCaptor =
                ArgumentCaptor.forClass(UserCredential.class);

        verify(userCredentialRepository).save(credentialCaptor.capture());

        UserCredential savedCredential = credentialCaptor.getValue();

        assertThat(savedCredential.getUser()).isSameAs(user);
        assertThat(result.getPasswordHash()).isEqualTo(passwordHash);

    }
    @Test
    @DisplayName("Should throw exception when password validation fails")
    void test_02(){
        User user = new User("test", "pedro", "silva");
        String rawPassword = "weak";

        PasswordValidationResult result = PasswordValidationResult.failure(
                List.of("Password must contain at least 15 characters")
        );

        when(passwordValidation.validate(rawPassword)).thenReturn(result);

        assertThatThrownBy(() ->
                userCredentialService.createCredential(user, rawPassword))
                .isInstanceOf(PasswordValidationException.class)
                .hasMessage("Password validation failed")
                .satisfies(exception -> {
                    PasswordValidationException passwordValidationException =
                            (PasswordValidationException) exception;

                    assertThat(passwordValidationException.getErrors())
                            .containsExactly("Password must contain at least 15 characters");
                });
    }
    @Test
    @DisplayName("Should not encode password when validation fails")
    void test_03(){
        User user = new User("test", "pedro", "silva");
        String rawPassword = "weak";

        when(passwordValidation.validate(rawPassword)).
                thenReturn(
                        PasswordValidationResult.failure(
                                List.of("Password must contain at least 15 characters")
                        )
                );

        assertThatThrownBy(() ->
                userCredentialService.createCredential(user, rawPassword))
                .isInstanceOf(PasswordValidationException.class)
                .hasMessage("Password validation failed");

        verifyNoInteractions(passwordService, userCredentialRepository);
    }
    @Test
    @DisplayName("Should not persist credential when password validation fails")
    void test_04(){
        User user = new User("test", "pedro", "silva");
        String rawPassword = "weak";

        when(passwordValidation.validate(rawPassword)).
                thenReturn(
                        PasswordValidationResult.failure(
                                List.of("Password must contain at least 15 characters")
                        )
                );

        assertThatThrownBy(() ->
                userCredentialService.createCredential(user, rawPassword))
                .isInstanceOf(PasswordValidationException.class)
                .hasMessage("Password validation failed");

        verifyNoInteractions(passwordService, userCredentialRepository);
    }
    @Test
    @DisplayName("Should reject null user")
    void test_05(){
        String rawPassword = "StrongPassword@123";

        assertThatThrownBy(() ->
                userCredentialService.createCredential(null, rawPassword))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("User is required");

        verifyNoInteractions(userCredentialRepository, passwordValidation, passwordService);
    }
}
