package com.user.identity.repository;

import com.user.identity.entity.UserEmail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserEmailRepository extends JpaRepository<UserEmail, UUID> {

    Page<UserEmail> findAllByUserIdAndRetiredAtIsNull(UUID userId, Pageable pageable);

    Optional<UserEmail> findByNormalizedEmailAndRetiredAtIsNull(String normalizedEmail);

    Optional<UserEmail> findByUserIdAndPrimaryTrueAndRetiredAtIsNull(UUID userId);

    Optional<UserEmail> findByIdAndUser_IdAndRetiredAtIsNull(UUID emailId, UUID userId);

    boolean existsByNormalizedEmailAndRetiredAtIsNull(String normalizedEmail);
}
