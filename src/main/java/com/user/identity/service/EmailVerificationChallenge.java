package com.user.identity.service;


import java.time.Instant;
import java.util.UUID;

public record EmailVerificationChallenge(
        UUID emailID,
        String rawToken,
        Instant expiresAt
) {
}
