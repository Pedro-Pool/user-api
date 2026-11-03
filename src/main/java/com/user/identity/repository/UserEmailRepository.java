package com.user.identity.repository;

import com.user.identity.entity.UserEmail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserEmailRepository extends JpaRepository<UserEmail, UUID> {
    List<UserEmail> findAllByUserIdAndRetiredAtIsNull(UUID userId);

    Optional<UserEmail> findByNormalizedEmailAndRetiredAtIsNull(String normalizedEmail);

    Optional<UserEmail> findByUserIdAndPrimaryTrueAndRetiredAtIsNull(UUID userId);

    boolean existsByNormalizedEmailAndRetiredAtIsNull(String normalizedEmail);
}
