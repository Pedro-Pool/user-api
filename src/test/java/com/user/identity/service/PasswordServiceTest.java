package com.user.identity.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class PasswordServiceTest {

    private PasswordService passwordService;
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp(){
        passwordEncoder = mock(PasswordEncoder.class);
        passwordService = new PasswordService(passwordEncoder);
    }

    @Test
    @DisplayName("Should encode password")
    void test_01(){
        String rawPassword = "StrongPassword@123";
        String encodePassword = "argon2id-encoded-hash";

        when(passwordEncoder.encode(rawPassword)).thenReturn(encodePassword);

        String result = passwordService.encode(rawPassword);

        assertThat(result).isEqualTo(encodePassword);

        verify(passwordEncoder, times(1)).encode(rawPassword);
        verifyNoMoreInteractions(passwordEncoder);
    }
    @Test
    @DisplayName("Should reject null password")
    void test_02(){
        assertThatThrownBy(() -> passwordService.encode(null))
                .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("Password is required");

        verifyNoMoreInteractions(passwordEncoder);
    }
    @Test
    @DisplayName("Should reject empty password")
    void test_03(){
        assertThatThrownBy(() -> passwordService.encode(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password is required");

        verifyNoMoreInteractions(passwordEncoder);
    }
    @Test
    @DisplayName("Should reject blank password")
    void test_04(){
        assertThatThrownBy(() -> passwordService.encode("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password is required");

        verifyNoMoreInteractions(passwordEncoder);
    }
}
