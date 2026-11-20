package com.user.identity.service;

import com.user.identity.entity.User;
import com.user.identity.entity.UserEmail;
import com.user.identity.repository.UserEmailRepository;
import com.user.identity.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserEmailService unit tests")
class UserEmailServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserEmailRepository userEmailRepository;

    @Mock
    private EmailVerificationService emailVerificationService;

    @InjectMocks
    private UserEmailService userEmailService;

    private UUID userId;
    private UUID emailId;
    private User user;
    private UserEmail userEmail;

    private EmailVerificationChallenge challenge;

    private void setEmailId(UserEmail email, UUID id){
        try {
            Field field = UserEmail.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(email, id);
        } catch (ReflectiveOperationException exception){
            throw new IllegalStateException(exception);
        }
    }

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        emailId = UUID.randomUUID();

        user = new User("Test User", "Test", "User");
        userEmail = new UserEmail(user, "test@example.com");

        challenge = new EmailVerificationChallenge(
                emailId,
                "verification-token",
                java.time.Instant.now().plusSeconds(900)
        );
    }

    @Test
    @DisplayName("Should register email and generate verification challenge")
    void test_01() {
        when(userRepository.findByIdAndDeletedAtIsNull(userId))
                .thenReturn(Optional.of(user));

        when(userEmailRepository.existsByNormalizedEmailAndRetiredAtIsNull(
                "test@example.com")).thenReturn(false);

        when(userEmailRepository.saveAndFlush(any(UserEmail.class)))
                .thenReturn(userEmail);

        when(emailVerificationService.createToken(userEmail))
                .thenReturn(challenge);

        EmailVerificationChallenge result =
                userEmailService.registerEmail(userId, "test@example.com");

        assertThat(result).isEqualTo(challenge);

        verify(userEmailRepository).saveAndFlush(any(UserEmail.class));
        verify(emailVerificationService).createToken(userEmail);
    }
    @Test
    @DisplayName("Should normalize email before checking duplication")
    void test_02() {
        when(userRepository.findByIdAndDeletedAtIsNull(userId))
                .thenReturn(Optional.of(user));

        when(userEmailRepository.existsByNormalizedEmailAndRetiredAtIsNull(
                "test@example.com")).thenReturn(false);

        when(userEmailRepository.saveAndFlush(any(UserEmail.class)))
                .thenReturn(userEmail);

        when(emailVerificationService.createToken(any(UserEmail.class)))
                .thenReturn(challenge);

        userEmailService.registerEmail(userId, "  TEST@EXAMPLE.COM  ");

        verify(userEmailRepository)
                .existsByNormalizedEmailAndRetiredAtIsNull("test@example.com");

        verify(userEmailRepository).saveAndFlush(argThat(email ->
                email.getEmail().equals("TEST@EXAMPLE.COM")
                        && email.getNormalizedEmail().equals("test@example.com")
        ));
    }
    @Test
    @DisplayName("Should reject duplicated email")
    void test_03() {
        when(userRepository.findByIdAndDeletedAtIsNull(userId))
                .thenReturn(Optional.of(user));

        when(userEmailRepository.existsByNormalizedEmailAndRetiredAtIsNull(
                "test@example.com")).thenReturn(true);

        assertThatThrownBy(() ->
                userEmailService.registerEmail(userId, "test@example.com"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Email is already in use");

        verify(userEmailRepository, never()).saveAndFlush(any());
        verify(emailVerificationService, never()).createToken(any());
    }
    @Test
    @DisplayName("Should reject registration for nonexistent user")
    void test_04() {
        when(userRepository.findByIdAndDeletedAtIsNull(userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                userEmailService.registerEmail(userId, "test@example.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User not found");

        verifyNoInteractions(userEmailRepository, emailVerificationService);
    }
    @Test
    @DisplayName("Should reject null email")
    void test_05() {
        when(userRepository.findByIdAndDeletedAtIsNull(userId))
                .thenReturn(Optional.of(user));

        assertThatThrownBy(() ->
                userEmailService.registerEmail(userId, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email is required");
    }
    @Test
    @DisplayName("Should reject blank email")
    void test_06() {
        when(userRepository.findByIdAndDeletedAtIsNull(userId))
                .thenReturn(Optional.of(user));

        assertThatThrownBy(() ->
                userEmailService.registerEmail(userId, "   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email is required");
    }
    @Test
    @DisplayName("Should resend verification for active unverified email")
    void test_07() {
        when(userEmailRepository.findByIdAndUser_IdAndRetiredAtIsNull(
                emailId, userId)).thenReturn(Optional.of(userEmail));

        when(emailVerificationService.createToken(userEmail))
                .thenReturn(challenge);

        EmailVerificationChallenge result =
                userEmailService.resendVerification(userId, emailId);

        assertThat(result).isEqualTo(challenge);

        verify(emailVerificationService).createToken(userEmail);
    }
    @Test
    @DisplayName("Should reject verification resend for verified email")
    void test_08() {
        userEmail.verify();

        when(userEmailRepository.findByIdAndUser_IdAndRetiredAtIsNull(
                emailId, userId)).thenReturn(Optional.of(userEmail));

        assertThatThrownBy(() ->
                userEmailService.resendVerification(userId, emailId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email is already verified");

        verify(emailVerificationService, never()).createToken(any());
    }
    @Test
    @DisplayName("Should set verified email as primary")
    void test_09() {
        userEmail.verify();

        when(userEmailRepository.findByIdAndUser_IdAndRetiredAtIsNull(
                emailId, userId)).thenReturn(Optional.of(userEmail));

        when(userEmailRepository.findByUserIdAndPrimaryTrueAndRetiredAtIsNull(
                userId)).thenReturn(Optional.empty());

        userEmailService.setPrimaryEmail(userId, emailId);

        assertThat(userEmail.isPrimary()).isTrue();

        verify(userEmailRepository).saveAndFlush(userEmail);
    }
    @Test
    @DisplayName("Should remove previous primary email")
    void test_10() {
        UserEmail previousEmail =
                new UserEmail(user, "old@example.com");

        setEmailId(previousEmail, UUID.randomUUID());
        setEmailId(userEmail, emailId);

        previousEmail.verify();
        previousEmail.makePrimary();

        userEmail.verify();

        when(userEmailRepository.findByIdAndUser_IdAndRetiredAtIsNull(
                emailId, userId)).thenReturn(Optional.of(userEmail));

        when(userEmailRepository.findByUserIdAndPrimaryTrueAndRetiredAtIsNull(
                userId)).thenReturn(Optional.of(previousEmail));

        userEmailService.setPrimaryEmail(userId, emailId);

        assertThat(previousEmail.isPrimary()).isFalse();
        assertThat(userEmail.isPrimary()).isTrue();

        verify(userEmailRepository).saveAndFlush(previousEmail);
        verify(userEmailRepository).saveAndFlush(userEmail);
    }
    @Test
    @DisplayName("Should reject unverified email as primary")
    void test_11() {
        when(userEmailRepository.findByIdAndUser_IdAndRetiredAtIsNull(
                emailId, userId)).thenReturn(Optional.of(userEmail));

        assertThatThrownBy(() ->
                userEmailService.setPrimaryEmail(userId, emailId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email must be verified before becoming primary");

        verify(userEmailRepository, never())
                .saveAndFlush(any(UserEmail.class));
    }
    @Test
    @DisplayName("Should retire active email")
    void test_12() {
        when(userEmailRepository.findByIdAndUser_IdAndRetiredAtIsNull(
                emailId, userId)).thenReturn(Optional.of(userEmail));

        userEmailService.retireEmail(userId, emailId);

        assertThat(userEmail.getRetiredAt()).isNotNull();
        assertThat(userEmail.isPrimary()).isFalse();

        verify(userEmailRepository).saveAndFlush(userEmail);
    }
    @Test
    @DisplayName("Should reject operation on another user's email")
    void test_13() {
        when(userEmailRepository.findByIdAndUser_IdAndRetiredAtIsNull(
                emailId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                userEmailService.retireEmail(userId, emailId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Active email not found for user");

        verify(userEmailRepository, never())
                .saveAndFlush(any(UserEmail.class));
    }
    @Test
    @DisplayName("Should reject resend for retired email")
    void test_14() {
        when(userEmailRepository.findByIdAndUser_IdAndRetiredAtIsNull(
                emailId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                userEmailService.resendVerification(userId, emailId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Active email not found for user");

        verifyNoInteractions(emailVerificationService);
    }
    @Test
    @DisplayName("Should reject setting retired email as primary")
    void test_15() {
        when(userEmailRepository.findByIdAndUser_IdAndRetiredAtIsNull(
                emailId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                userEmailService.setPrimaryEmail(userId, emailId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Active email not found for user");

        verify(userEmailRepository, never())
                .saveAndFlush(any(UserEmail.class));
    }
    @Test
    @DisplayName("Should allow registering an email previously retired")
    void test_16() {
        when(userRepository.findByIdAndDeletedAtIsNull(userId))
                .thenReturn(Optional.of(user));

        when(userEmailRepository.existsByNormalizedEmailAndRetiredAtIsNull(
                "test@example.com")).thenReturn(false);

        when(userEmailRepository.saveAndFlush(any(UserEmail.class)))
                .thenReturn(userEmail);

        when(emailVerificationService.createToken(userEmail))
                .thenReturn(challenge);

        EmailVerificationChallenge result =
                userEmailService.registerEmail(userId, "test@example.com");

        assertThat(result).isEqualTo(challenge);

        verify(userEmailRepository)
                .existsByNormalizedEmailAndRetiredAtIsNull("test@example.com");

        verify(userEmailRepository).saveAndFlush(any(UserEmail.class));
        verify(emailVerificationService).createToken(userEmail);
    }
    @Test
    @DisplayName("Should reject resending verification for another user's email")
    void test_17() {
        when(userEmailRepository.findByIdAndUser_IdAndRetiredAtIsNull(
                emailId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                userEmailService.resendVerification(userId, emailId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Active email not found for user");

        verifyNoInteractions(emailVerificationService);
    }
    @Test
    @DisplayName("Should reject setting retired email as primary")
    void test_18() {
        when(userEmailRepository.findByIdAndUser_IdAndRetiredAtIsNull(
                emailId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                userEmailService.setPrimaryEmail(userId, emailId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Active email not found for user");

        verify(userEmailRepository, never())
                .saveAndFlush(any(UserEmail.class));
    }
    @Test
    @DisplayName("Should allow retiring an already retired email only when active lookup succeeds")
    void test_19() {
        when(userEmailRepository.findByIdAndUser_IdAndRetiredAtIsNull(
                emailId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                userEmailService.retireEmail(userId, emailId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Active email not found for user");

        verify(userEmailRepository, never())
                .saveAndFlush(any(UserEmail.class));
    }
    @Test
    @DisplayName("Should allow setting the same email as primary again")
    void test_20() {
        setEmailId(userEmail, emailId);

        userEmail.verify();
        userEmail.makePrimary();

        when(userEmailRepository.findByIdAndUser_IdAndRetiredAtIsNull(
                emailId, userId)).thenReturn(Optional.of(userEmail));

        when(userEmailRepository.findByUserIdAndPrimaryTrueAndRetiredAtIsNull(
                userId)).thenReturn(Optional.of(userEmail));

        userEmailService.setPrimaryEmail(userId, emailId);

        assertThat(userEmail.isPrimary()).isTrue();

        verify(userEmailRepository).saveAndFlush(userEmail);
    }

}
