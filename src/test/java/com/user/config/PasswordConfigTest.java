package com.user.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordConfigTest {

    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp(){
        PasswordConfig passwordConfig = new PasswordConfig();
        passwordEncoder = passwordConfig.passwordEncoder();
    }

    @Test
    @DisplayName("Should encode password")
    void test_01(){
        String rawPassword = "StrongPassword@123";

        String encodePassword = passwordEncoder.encode(rawPassword);

        assertThat(encodePassword).isNotBlank().isNotEqualTo(rawPassword);
    }
    @Test
    @DisplayName("Should match correct password")
    void test_02(){
        String rawPassword = "StrongPassword@123";

        String encodePassword = passwordEncoder.encode(rawPassword);

        assertThat(passwordEncoder.matches(rawPassword, encodePassword)).isTrue();
    }
    @Test
    @DisplayName("Should reject incorrect password")
    void test_03(){
        String rawPassword = "StrongPassword@123";
        String encodePassword = passwordEncoder.encode(rawPassword);

        assertThat(passwordEncoder.matches("wrongPassword123", encodePassword)).isFalse();
    }
    @Test
    @DisplayName("Should generate different hashes for same password")
    void test_04(){
        String rawPassword = "StrongPassword@123";

        String firstHash = passwordEncoder.encode(rawPassword);
        String secondHash = passwordEncoder.encode(rawPassword);

        assertThat(firstHash).isNotEqualTo(secondHash);
    }
    @Test
    @DisplayName("Should not expose original password in encode value")
    void test_05(){
        String rawPassword = "StrongPassword@123";

        String encodePassword = passwordEncoder.encode(rawPassword);

        assertThat(encodePassword).doesNotContain(rawPassword);
    }
}
