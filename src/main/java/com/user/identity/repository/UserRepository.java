package com.user.identity.repository;

import com.user.identity.entity.User;
import com.user.identity.entity.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByIdAndDeletedAtIsNull(UUID id);

    Page<User> findAllByStatusAndDeletedAtIsNull(UserStatus status, Pageable pageable);

    Page<User> findAllByDeletedAtIsNull(Pageable pageable);

    boolean existsByIdAndDeletedAtIsNull(UUID id);
}
