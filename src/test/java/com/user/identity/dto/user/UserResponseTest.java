package com.user.identity.dto.user;

import com.user.identity.entity.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserResponseTest {

    @Test
    @DisplayName("Should create user response with all fields")
    void test_01(){
        UUID id = UUID.randomUUID();
        String displayName = "John";
        String firstName = "John";
        String lastName = "Doe";
        UserStatus status = UserStatus.ACTIVE;
        Instant createdAt = Instant.parse("2026-11-12T12:00:00Z");

        UserResponse response = new UserResponse(
                id,
                displayName,
                firstName,
                lastName,
                status,
                createdAt
        );

        assertThat(response.id()).isEqualTo(id);
        assertThat(response.displayName()).isEqualTo(displayName);
        assertThat(response.firstName()).isEqualTo(firstName);
        assertThat(response.lastName()).isEqualTo(lastName);
        assertThat(response.status()).isEqualTo(status);
        assertThat(response.createdAt()).isEqualTo(createdAt);
    }
    @Test
    @DisplayName("Should expose only public user fields")
    void test_02(){
        assertThat(UserResponse.class.getRecordComponents())
                .extracting(component -> component.getName())
                .containsExactly(
                        "id",
                        "displayName",
                        "firstName",
                        "lastName",
                        "status",
                        "createdAt"
                );
    }
}
