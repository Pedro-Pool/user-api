package com.user.identity.dto.user;

public record UserProfileUpdateRequest(
        String displayName,
        String firstName,
        String lastName
) {
}
