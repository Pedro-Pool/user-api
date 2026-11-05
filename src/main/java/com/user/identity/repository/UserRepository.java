package com.user.identity.repository;

import com.user.identity.entity.User;
import com.user.identity.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByIdAndDeletedAtIsNull(UUID id);

    List<User> findAllByStatus(UserStatus status);

    List<User> findAllByDeletedAtIsNull();

    boolean existsByIdAndDeletedAtIsNull(UUID id);
}
