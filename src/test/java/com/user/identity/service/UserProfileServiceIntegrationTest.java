package com.user.identity.service;

import com.user.AbstractPostgresTest;
import com.user.identity.dto.user.UserProfileUpdateRequest;
import com.user.identity.entity.User;
import com.user.identity.exception.UserProfileValidationException;
import com.user.identity.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class UserProfileServiceIntegrationTest extends AbstractPostgresTest {

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Should persist profile update in database")
    void test_01(){
        User user = new User(
                "John Doe",
                "John",
                "Doe",
                Clock.systemUTC()
        );

        User savedUser = userRepository.saveAndFlush(user);

        UserProfileUpdateRequest request =
                new UserProfileUpdateRequest("Jonathan Doe", null, null);

        userProfileService.updateProfile(savedUser.getId(), request);


        User updatedUser = userRepository.findByIdAndDeletedAtIsNull(savedUser.getId())
                        .orElseThrow();

        assertThat(updatedUser.getDisplayName()).isEqualTo("Jonathan Doe");
        assertThat(updatedUser.getFirstName()).isEqualTo("John");
        assertThat(updatedUser.getLastName()).isEqualTo("Doe");
    }
    @Test
    @DisplayName("Should persist all profile fields update in database")
    void test_02(){
        User user = new User(
                "John Doe",
                "John",
                "Doe",
                Clock.systemUTC()
        );

        User savedUser = userRepository.saveAndFlush(user);

        UserProfileUpdateRequest request =
                new UserProfileUpdateRequest("Jonathan Doe", "Jonathan", "James");

        userProfileService.updateProfile(savedUser.getId(), request);


        User updatedUser = userRepository.findByIdAndDeletedAtIsNull(savedUser.getId())
                .orElseThrow();

        assertThat(updatedUser.getDisplayName()).isEqualTo("Jonathan Doe");
        assertThat(updatedUser.getFirstName()).isEqualTo("Jonathan");
        assertThat(updatedUser.getLastName()).isEqualTo("James");
    }
    @Test
    @DisplayName("Should persist all profile fields update in database")
    void test_03(){
        User user1 = new User(
                "John Doe",
                "John",
                "Doe",
                Clock.systemUTC()
        );
        User user2 = new User(
                "Maria Silva",
                "Maria",
                "Silva",
                Clock.systemUTC()
        );

        User savedUser1 = userRepository.saveAndFlush(user1);
        User savedUser2 = userRepository.saveAndFlush(user2);

        UserProfileUpdateRequest request1 =
                new UserProfileUpdateRequest("Jonathan Doe", "Jonathan", "James");

        userProfileService.updateProfile(savedUser1.getId(), request1);

        User updatedUser1 = userRepository.findByIdAndDeletedAtIsNull(savedUser1.getId())
                .orElseThrow();

        User updatedUser2 = userRepository.findByIdAndDeletedAtIsNull(savedUser2.getId())
                .orElseThrow();

        assertThat(updatedUser1.getDisplayName()).isEqualTo("Jonathan Doe");
        assertThat(updatedUser1.getFirstName()).isEqualTo("Jonathan");
        assertThat(updatedUser1.getLastName()).isEqualTo("James");

        assertThat(updatedUser2.getDisplayName()).isEqualTo("Maria Silva");
        assertThat(updatedUser2.getFirstName()).isEqualTo("Maria");
        assertThat(updatedUser2.getLastName()).isEqualTo("Silva");
    }
    @Test
    @DisplayName("Should persist all profile fields update in database")
    void test_04(){
        UUID userId = UUID.randomUUID();

        UserProfileUpdateRequest request =
                new UserProfileUpdateRequest("Jonathan Doe", "Jonathan", "James");

        assertThatThrownBy(() -> userProfileService.updateProfile(userId, request))
                .isInstanceOf(IllegalArgumentException.class);
    }
    @Test
    @DisplayName("Should not persist profile update when validation fails")
    void test_05(){
        User user = new User(
                "John Doe",
                "John",
                "Doe",
                Clock.systemUTC()
        );

        User savedUser = userRepository.saveAndFlush(user);

        UserProfileUpdateRequest request =
                new UserProfileUpdateRequest("J".repeat(121), null, null);

        assertThatThrownBy(() -> userProfileService.updateProfile(savedUser.getId(), request))
                .isInstanceOf(UserProfileValidationException.class);

        User unchangedUser = userRepository.findByIdAndDeletedAtIsNull(savedUser.getId())
                .orElseThrow();

        assertThat(unchangedUser.getDisplayName()).isEqualTo("John Doe");
        assertThat(unchangedUser.getFirstName()).isEqualTo("John");
        assertThat(unchangedUser.getLastName()).isEqualTo("Doe");
    }
    @Test
    @DisplayName("Should preserve profile when all update fields are null")
    void test_06(){
        User user = new User(
                "John Doe",
                "John",
                "Doe",
                Clock.systemUTC()
        );

        User savedUser = userRepository.saveAndFlush(user);

        UserProfileUpdateRequest request =
                new UserProfileUpdateRequest(null, null, null);

        assertThatThrownBy(() -> userProfileService.updateProfile(savedUser.getId(), request))
                .isInstanceOf(UserProfileValidationException.class);;

        User unchangedUser = userRepository.findByIdAndDeletedAtIsNull(savedUser.getId())
                .orElseThrow();

        assertThat(unchangedUser.getDisplayName()).isEqualTo("John Doe");
        assertThat(unchangedUser.getFirstName()).isEqualTo("John");
        assertThat(unchangedUser.getLastName()).isEqualTo("Doe");
    }
    @Test
    @DisplayName("Should reject null profile update request")
    void test_07(){
        User user = new User(
                "John Doe",
                "John",
                "Doe",
                Clock.systemUTC()
        );

        User savedUser = userRepository.saveAndFlush(user);

        assertThatThrownBy(() -> userProfileService.updateProfile(savedUser.getId(), null))
                .isInstanceOf(UserProfileValidationException.class);;

        User unchangedUser = userRepository.findByIdAndDeletedAtIsNull(savedUser.getId())
                .orElseThrow();

        assertThat(unchangedUser.getDisplayName()).isEqualTo("John Doe");
        assertThat(unchangedUser.getFirstName()).isEqualTo("John");
        assertThat(unchangedUser.getLastName()).isEqualTo("Doe");
    }
    @Test
    @DisplayName("Should update updatedAt without changing createdAt")
    void test_08(){
        User user = new User(
                "John Doe",
                "John",
                "Doe",
                Clock.systemUTC()
        );

        User savedUser = userRepository.saveAndFlush(user);

        Instant originalCreatedAt = savedUser.getCreatedAt().truncatedTo(ChronoUnit.MICROS);
        Instant originalUpdatedAt = savedUser.getUpdatedAt().truncatedTo(ChronoUnit.MICROS);

        UserProfileUpdateRequest request =
                new UserProfileUpdateRequest("Jonathan Doe", null, null);

        userProfileService.updateProfile(savedUser.getId(), request);

        User updatedUser = userRepository.findByIdAndDeletedAtIsNull(savedUser.getId())
                .orElseThrow();

        assertThat(updatedUser.getCreatedAt()).isCloseTo(originalCreatedAt, within(1, ChronoUnit.MICROS));
        assertThat(updatedUser.getUpdatedAt()).isAfter(originalUpdatedAt);
    }
    @Test
    @DisplayName("Should increment version when profile is updated")
    void test_09(){
        User user = new User(
                "John Doe",
                "John",
                "Doe",
                Clock.systemUTC()
        );

        User savedUser = userRepository.saveAndFlush(user);

        long originalVersion = savedUser.getVersion();

        UserProfileUpdateRequest request =
                new UserProfileUpdateRequest("Jonathan Doe", null, null);

        userProfileService.updateProfile(savedUser.getId(), request);

        User updatedUser = userRepository.findByIdAndDeletedAtIsNull(savedUser.getId())
                .orElseThrow();

        assertThat(updatedUser.getVersion()).isEqualTo(originalVersion + 1);
    }
    @Test
    @DisplayName("Should not update version when profile values remain unchanged")
    void test_10(){
        User user = new User(
                "John Doe",
                "John",
                "Doe",
                Clock.systemUTC()
        );

        User savedUser = userRepository.saveAndFlush(user);

        long originalVersion = savedUser.getVersion();
        Instant originalUpdatedAt = savedUser.getUpdatedAt();

        UserProfileUpdateRequest request =
                new UserProfileUpdateRequest("John Doe", null, null);

        userProfileService.updateProfile(savedUser.getId(), request);

        User unchangedUser = userRepository.findByIdAndDeletedAtIsNull(savedUser.getId())
                .orElseThrow();

        assertThat(unchangedUser.getVersion()).isEqualTo(originalVersion);
        assertThat(unchangedUser.getUpdatedAt()).isCloseTo(originalUpdatedAt, within(1, ChronoUnit.MICROS));
    }
    @Test
    @DisplayName("Should not update profile when user is soft deleted")
    void test_11(){
        User user = new User(
                "John Doe",
                "John",
                "Doe",
                Clock.systemUTC()
        );

        User savedUser = userRepository.saveAndFlush(user);

        savedUser.softDelete();
        userRepository.saveAndFlush(savedUser);

        UserProfileUpdateRequest request =
                new UserProfileUpdateRequest("Jonathan Doe", null, null);

        assertThatThrownBy(() -> userProfileService.updateProfile(savedUser.getId(), request))
                .isInstanceOf(IllegalArgumentException.class);

        User deletedUser = userRepository.findById(savedUser.getId())
                .orElseThrow();

        assertThat(deletedUser.getDeletedAt()).isNotNull();
        assertThat(deletedUser.getDisplayName()).isEqualTo("John Doe");
        assertThat(deletedUser.getFirstName()).isEqualTo("John");
        assertThat(deletedUser.getLastName()).isEqualTo("Doe");
    }
}
