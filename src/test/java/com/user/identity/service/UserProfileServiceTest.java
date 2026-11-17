package com.user.identity.service;

import com.user.identity.dto.user.UserResponse;
import com.user.identity.entity.User;
import com.user.identity.entity.UserStatus;
import com.user.identity.repository.UserRepository;
import com.user.identity.validation.user.UserProfileValidation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
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
}
