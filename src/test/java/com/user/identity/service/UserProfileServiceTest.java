package com.user.identity.service;

import com.user.identity.dto.user.UserProfileUpdateRequest;
import com.user.identity.dto.user.UserResponse;
import com.user.identity.entity.User;
import com.user.identity.entity.UserStatus;
import com.user.identity.exception.UserProfileValidationException;
import com.user.identity.repository.UserRepository;
import com.user.identity.validation.ValidationResult;
import com.user.identity.validation.user.UserProfileValidation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserProfileServiceTest {

    @Mock
    private UserProfileValidation profileValidation;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserProfileService userProfileService;

    @Test
    @DisplayName("Should return user profile when user exist")
    void test_01(){
        UUID userId = UUID.randomUUID();

        User user = new User(
                "John Doe",
                "John",
                "Doe",
                Clock.systemUTC()
        );

        when(userRepository.findByIdAndDeletedAtIsNull(userId)).thenReturn(Optional.of(user));

        UserResponse response = userProfileService.getProfile(userId);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(user.getId());
        assertThat(response.displayName()).isEqualTo("John Doe");
        assertThat(response.firstName()).isEqualTo("John");
        assertThat(response.lastName()).isEqualTo("Doe");
        assertThat(response.status()).isEqualTo(UserStatus.PENDING_VERIFICATION);

        verify(userRepository).findByIdAndDeletedAtIsNull(userId);
    }
    @Test
    @DisplayName("Should throw exception when user does not exist")
    void test_02(){
        UUID userId = UUID.randomUUID();

        when(userRepository.findByIdAndDeletedAtIsNull(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userProfileService.getProfile(userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User not found");

        verify(userRepository).findByIdAndDeletedAtIsNull(userId);
    }
    @Test
    @DisplayName("Should throw exception when user ID is null")
    void test_03(){
        assertThatThrownBy(() -> userProfileService.getProfile(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("User ID is required");

        verifyNoInteractions(userRepository);
    }
    @Test
    @DisplayName("Should update only display name")
    void test_04(){
        UUID userId = UUID.randomUUID();

        User user = new User(
                "John Doe",
                "John",
                "Doe",
                Clock.systemUTC()
        );

        UserProfileUpdateRequest request =
                new UserProfileUpdateRequest("Jonathan Doe", null, null);

        when(profileValidation.validate(request)).thenReturn(ValidationResult.success());
        when(userRepository.findByIdAndDeletedAtIsNull(userId)).thenReturn(Optional.of(user));

        UserResponse response = userProfileService.updateProfile(userId, request);

        assertThat(response.displayName()).isEqualTo("Jonathan Doe");
        assertThat(response.firstName()).isEqualTo("John");
        assertThat(response.lastName()).isEqualTo("Doe");

        verify(profileValidation).validate(request);
        verify(userRepository).findByIdAndDeletedAtIsNull(userId);
    }
    @Test
    @DisplayName("Should update only first name")
    void test_05(){
        UUID userId = UUID.randomUUID();

        User user = new User(
                "John Doe",
                "John",
                "Doe",
                Clock.systemUTC()
        );

        UserProfileUpdateRequest request =
                new UserProfileUpdateRequest(null, "Jonathan", null);

        when(profileValidation.validate(request)).thenReturn(ValidationResult.success());
        when(userRepository.findByIdAndDeletedAtIsNull(userId)).thenReturn(Optional.of(user));

        UserResponse response = userProfileService.updateProfile(userId, request);

        assertThat(response.displayName()).isEqualTo("John Doe");
        assertThat(response.firstName()).isEqualTo("Jonathan");
        assertThat(response.lastName()).isEqualTo("Doe");

        verify(profileValidation).validate(request);
        verify(userRepository).findByIdAndDeletedAtIsNull(userId);
    }
    @Test
    @DisplayName("Should update only last name")
    void test_06(){
        UUID userId = UUID.randomUUID();

        User user = new User(
                "John Doe",
                "John",
                "Doe",
                Clock.systemUTC()
        );

        UserProfileUpdateRequest request =
                new UserProfileUpdateRequest(null, null, "James");

        when(profileValidation.validate(request)).thenReturn(ValidationResult.success());
        when(userRepository.findByIdAndDeletedAtIsNull(userId)).thenReturn(Optional.of(user));

        UserResponse response = userProfileService.updateProfile(userId, request);

        assertThat(response.displayName()).isEqualTo("John Doe");
        assertThat(response.firstName()).isEqualTo("John");
        assertThat(response.lastName()).isEqualTo("James");

        verify(profileValidation).validate(request);
        verify(userRepository).findByIdAndDeletedAtIsNull(userId);
    }
    @Test
    @DisplayName("Should update all profile fields")
    void test_07(){
        UUID userId = UUID.randomUUID();

        User user = new User(
                "John Doe",
                "John",
                "Doe",
                Clock.systemUTC()
        );

        UserProfileUpdateRequest request =
                new UserProfileUpdateRequest("Jonathan James", "Jonathan", "James");

        when(profileValidation.validate(request)).thenReturn(ValidationResult.success());
        when(userRepository.findByIdAndDeletedAtIsNull(userId)).thenReturn(Optional.of(user));

        UserResponse response = userProfileService.updateProfile(userId, request);

        assertThat(response.displayName()).isEqualTo("Jonathan James");
        assertThat(response.firstName()).isEqualTo("Jonathan");
        assertThat(response.lastName()).isEqualTo("James");

        verify(profileValidation).validate(request);
        verify(userRepository).findByIdAndDeletedAtIsNull(userId);
    }
    @Test
    @DisplayName("Should reject null profile update request")
    void test_08(){
        UUID userId = UUID.randomUUID();

        when(profileValidation.validate(null)).thenReturn(ValidationResult.failure(
                List.of("User profile update request is required")
        ));

        assertThatThrownBy(() -> userProfileService.updateProfile(userId, null))
                .isInstanceOf(UserProfileValidationException.class)
                .hasMessage("User profile validation failed");

        verify(profileValidation).validate(null);
        verifyNoInteractions(userRepository);
    }
    @Test
    @DisplayName("Should reject null user ID when updating profile")
    void test_09(){
        assertThatThrownBy(() -> userProfileService.updateProfile(null, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("User ID is required");

        verifyNoInteractions(userRepository, profileValidation);
    }
    @Test
    @DisplayName("Should throw exception when updating profile of non-existent user")
    void test_10(){
        UUID userId = UUID.randomUUID();

        UserProfileUpdateRequest request =
                new UserProfileUpdateRequest("John Doe", null, null);

        when(profileValidation.validate(request)).thenReturn(ValidationResult.success());
        when(userRepository.findByIdAndDeletedAtIsNull(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userProfileService.updateProfile(userId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User not found");

        verify(profileValidation).validate(request);
        verify(userRepository).findByIdAndDeletedAtIsNull(userId);
    }
    @Test
    @DisplayName("Should not access repository when profile validation fails")
    void test_11(){
        UUID userId = UUID.randomUUID();

        UserProfileUpdateRequest request =
                new UserProfileUpdateRequest("  ", null, null);

        when(profileValidation.validate(request)).thenReturn(ValidationResult.failure(
                List.of("Display name cannot be blank")
        ));

        assertThatThrownBy(() -> userProfileService.updateProfile(userId, request))
                .isInstanceOf(UserProfileValidationException.class)
                .hasMessage("User profile validation failed")
                .satisfies(exception -> {
                    UserProfileValidationException validationException =
                            (UserProfileValidationException) exception;

                    assertThat(validationException.getErrors())
                            .containsExactly("Display name cannot be blank");
                });

        verify(profileValidation).validate(request);
        verifyNoInteractions(userRepository);
    }
    @Test
    @DisplayName("Should reject update request with all fields null")
    void test_12(){
        UUID userId = UUID.randomUUID();

        UserProfileUpdateRequest request =
                new UserProfileUpdateRequest(null, null, null);

        when(profileValidation.validate(request)).thenReturn(ValidationResult.failure(
                List.of("At least one profile field must be provided")
        ));

        assertThatThrownBy(() -> userProfileService.updateProfile(userId, request))
                .isInstanceOf(UserProfileValidationException.class)
                .hasMessage("User profile validation failed");

        verify(profileValidation).validate(request);
        verifyNoInteractions(userRepository);
    }
}
