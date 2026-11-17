package com.user.identity.dto.user;

import com.user.identity.entity.UserStatus;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String displayName,
        String firstName,
        String lastName,
        UserStatus status,
        Instant createdAt
) {
}
