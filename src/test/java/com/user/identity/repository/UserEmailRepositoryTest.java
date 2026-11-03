
package com.user.identity.repository;

import com.user.AbstractPostgresTest;
import com.user.identity.entity.User;
import com.user.identity.entity.UserEmail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class UserEmailRepositoryTest extends AbstractPostgresTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserEmailRepository userEmailRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User(
                "Test User",
                "Test",
                "User"
        );

        user.activate();

        user = userRepository.saveAndFlush(user);
    }
    @Test
    @DisplayName("Should Persist And Retrieve User Email")
    void test_01() {

        UserEmail email = new UserEmail(
                user,
                "  Test@Example.com "
        );

        UserEmail saved = userEmailRepository.saveAndFlush(email);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEmail()).isEqualTo("Test@Example.com");
        assertThat(saved.getNormalizedEmail())
                .isEqualTo("test@example.com");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.isPrimary()).isFalse();
        assertThat(saved.getVerifiedAt()).isNull();
    }

    @Test
    @DisplayName("Should verify email")
    void test_02() {
        UserEmail email = new UserEmail(user, "test@example.com");

        email.verify();

        assertThat(email.getVerifiedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should allow verified email to become primary")
    void test_03() {
        UserEmail email = new UserEmail(user, "test@example.com");

        email.verify();
        email.makePrimary();

        assertThat(email.isPrimary()).isTrue();
    }

    @Test
    @DisplayName("Should not allow unverified email to become primary")
    void test_04() {
        UserEmail email = new UserEmail(user, "test@example.com");

        assertThatThrownBy(email::makePrimary)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Should retire email and remove primary status")
    void test_05() {
        UserEmail email = new UserEmail(user, "test@example.com");

        email.verify();
        email.makePrimary();
        email.retire();

        assertThat(email.isPrimary()).isFalse();
        assertThat(email.getRetiredAt()).isNotNull();
    }

    @Test
    @DisplayName("Should reject duplicate active email")
    void test_06() {
        User anotherUser = new User(
                "Another User",
                "Another",
                "User"
        );

        anotherUser.activate();

        anotherUser = userRepository.saveAndFlush(anotherUser);

        userEmailRepository.saveAndFlush(
                new UserEmail(user, "test@example.com")
        );

        User finalAnotherUser = anotherUser;

        assertThatThrownBy(() ->
                userEmailRepository.saveAndFlush(
                        new UserEmail(
                                finalAnotherUser,
                                "TEST@example.com"
                        )
                )
        ).isInstanceOf(DataIntegrityViolationException.class);
    }
}
