package com.user.identity.repository;


import com.user.AbstractPostgresTest;
import com.user.identity.entity.User;
import com.user.identity.entity.UserCredential;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Clock;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserCredentialRepositoryTest extends AbstractPostgresTest {

    @Autowired
    private UserCredentialRepository credentialRepository;

    @Autowired
    private UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    @DisplayName("Should persist credential associated with user")
    void test_01(){
        User user = createUser();

        UserCredential credential = new UserCredential(
                user,
                "encoded-password-hash",
                Clock.systemUTC()
        );
        UserCredential persistCredential = credentialRepository.saveAndFlush(credential);

        assertThat(persistCredential.getId()).isEqualTo(user.getId());
        assertThat(persistCredential.getUser().getId()).isEqualTo(user.getId());
        assertThat(persistCredential.getPasswordHash()).isEqualTo("encoded-password-hash");
    }
    @Test
    @DisplayName("Should find credential by user ID")
    void test_02(){
        User user = createUser();

        UserCredential credential = new UserCredential(
                user,
                "encoded-password-hash",
                Clock.systemUTC()
        );
        credentialRepository.saveAndFlush(credential);

        UserCredential persistCredential = credentialRepository.findById(user.getId()).orElseThrow();

        assertThat(persistCredential.getUser().getId()).isEqualTo(user.getId());
    }
    @Test
    @DisplayName("Should not persist two credentials for same user")
    void test_03(){
        User user = createUser();

        UserCredential firstCredential = new UserCredential(
                user,
                "first-password-hash",
                Clock.systemUTC()
        );

        credentialRepository.saveAndFlush(firstCredential);

        UserCredential secondCredential = new UserCredential(
                user,
                "second-password-hash",
                Clock.systemUTC()
        );

        assertThatThrownBy(() ->
                credentialRepository.saveAndFlush(secondCredential))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
    @Test
    @DisplayName("Should cascade delete credential when user is deleted")
    void test_04() {
        User user = createUser();

        UUID userId = user.getId();

        UserCredential credential = new UserCredential(
                user,
                "encoded-password-hash",
                Clock.systemUTC()
        );

        credentialRepository.saveAndFlush(credential);

        entityManager.clear();

        User persistedUser = userRepository.findById(user.getId())
                .orElseThrow();

        userRepository.delete(persistedUser);
        userRepository.flush();
        entityManager.clear();

        assertThat(credentialRepository.findById(userId)).isEmpty();
    }

    private User createUser(){
        User user = new User("test", "pedro", "silva");

        return userRepository.saveAndFlush(user);
    }
}
