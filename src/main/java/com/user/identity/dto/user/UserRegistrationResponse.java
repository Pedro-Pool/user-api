package com.user.identity.dto.user;

import com.user.identity.service.EmailVerificationChallenge;

public record UserRegistrationResponse(
        UserResponse user,
        EmailVerificationChallenge emailVerification
) {
}
