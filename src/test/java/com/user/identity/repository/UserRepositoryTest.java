package com.user.identity.repository;

import com.user.AbstractPostgresTest;
import com.user.identity.entity.User;
import com.user.identity.entity.UserStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class UserRepositoryTest extends AbstractPostgresTest {
    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldPersistAndRetrieveUser(){
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
}
