package com.user.identity.service;


import java.time.Instant;
import java.util.UUID;

public record EmailVerificationChallenge(
        UUID emailId,
        String rawToken,
        Instant expiresAt
) {
}
