package com.user.identity.repository;

import com.user.AbstractPostgresTest;
import com.user.identity.entity.User;
import com.user.identity.entity.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest extends AbstractPostgresTest {
    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Should persist and retrieve user")
    void test_01(){
        User user = new User(
                "Pedro",
                "Pedro",
                "Silva"
        );
        User saved = userRepository.saveAndFlush(user);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getDisplayName()).isEqualTo("Pedro");
        assertThat(saved.getStatus()).isEqualTo(UserStatus.PENDING_VERIFICATION);
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.getVersion()).isEqualTo(0);

        User found = userRepository.findById(saved.getId()).orElseThrow();

        assertThat(found.getDisplayName()).isEqualTo("Pedro");

    }
    @Test
    @DisplayName("Should increment version when user is updated")
    void test_02(){
        User user = new User(
                "Pedro",
                "Pedro",
                "Silva"
        );

        User saved = userRepository.saveAndFlush(user);
        long initialVersion = saved.getVersion();

        saved.updateDisplayName("Pedro Santos");

        userRepository.saveAndFlush(saved);

        assertThat(saved.getVersion()).isEqualTo(initialVersion + 1);
    }
    @Test
    @DisplayName("Should find only users that are not logically deleted")
    void test_03(){
        User activeUser = new User("Pedro", "Pedro", "Silva");
        User deletedUser = new User("Maria", "Maria", "Santos");

        User savedActiveUser = userRepository.save(activeUser);
        User savedDeletedUser = userRepository.save(deletedUser);

        savedDeletedUser.softDelete();

        userRepository.saveAndFlush(savedDeletedUser);

        assertThat(userRepository.findByIdAndDeletedAtIsNull(savedActiveUser.getId())).isPresent();
        assertThat(userRepository.findByIdAndDeletedAtIsNull(savedDeletedUser.getId())).isEmpty();
    }
    @Test
    @DisplayName("Should return only users that are not logically deleted")
    void test_04(){
        User activeUser = new User("Pedro", "Pedro", "Silva");
        User anotherActiveUser = new User("João", "João", "Souza");
        User deletedUser = new User("Maria", "Maria", "Santos");

        userRepository.save(activeUser);
        userRepository.save(anotherActiveUser);

        deletedUser.softDelete();
        userRepository.save(deletedUser);

        userRepository.flush();

        List<User> users = userRepository.findAllByDeletedAtIsNull();

        assertThat(users)
                .hasSize(2)
                .extracting(User::getDisplayName)
                .containsExactlyInAnyOrder("Pedro", "João");
    }
    @Test
    @DisplayName("Should verify existence only for users that are not logically deleted")
    void test_05(){
        User activeUser = new User("Pedro", "Pedro", "Silva");
        User deletedUser = new User("Maria", "Maria", "Santos");

        User savedActiveUser = userRepository.save(activeUser);

        deletedUser.softDelete();
        User savedDeletedUser = userRepository.save(deletedUser);

        userRepository.flush();

        assertThat(userRepository.existsByIdAndDeletedAtIsNull(savedActiveUser.getId())).isTrue();
        assertThat(userRepository.existsByIdAndDeletedAtIsNull(savedDeletedUser.getId())).isFalse();
        assertThat(userRepository.existsByIdAndDeletedAtIsNull(UUID.randomUUID())).isFalse();
    }
    @Test
    @DisplayName("Should return only non-deleted users with the specified status")
    void test_06(){
        User activeUser = new User("Pedro", "Pedro", "Silva");
        activeUser.activate();

        User anotherActiveUser = new User("João", "João", "Souza");
        anotherActiveUser.activate();

        User pendingUser = new User("Maria", "Maria", "Santos");

        User deletedUser = new User("Carlos", "Carlos", "Oliveira");
        deletedUser.activate();
        deletedUser.softDelete();

        userRepository.save(activeUser);
        userRepository.save(anotherActiveUser);
        userRepository.save(pendingUser);
        userRepository.save(deletedUser);

        userRepository.flush();

        List<User> users = userRepository.findAllByStatusAndDeletedAtIsNull(UserStatus.ACTIVE);

        assertThat(users)
                .hasSize(2)
                .extracting(User::getDisplayName)
                .containsExactlyInAnyOrder("Pedro", "João");
    }
}
