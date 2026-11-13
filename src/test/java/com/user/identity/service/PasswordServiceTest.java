package com.user.identity.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class PasswordServiceTest {

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
        String encodedPassword = "argon2id-encoded-hash";

        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);

        String result = passwordService.encode(rawPassword);

        assertThat(result).isEqualTo(encodedPassword);

        verify(passwordEncoder, times(1)).encode(rawPassword);
        verifyNoMoreInteractions(passwordEncoder);
    }
    @Test
    @DisplayName("Should reject null password")
    void test_02(){
        assertThatThrownBy(() -> passwordService.encode(null))
                .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("Password is required");

        verifyNoInteractions(passwordEncoder);
    }
    @Test
    @DisplayName("Should reject empty password")
    void test_03(){
        assertThatThrownBy(() -> passwordService.encode(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password is required");

        verifyNoInteractions(passwordEncoder);
    }
    @Test
    @DisplayName("Should reject blank password")
    void test_04(){
        assertThatThrownBy(() -> passwordService.encode("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password is required");

        verifyNoInteractions(passwordEncoder);
    }
    @Test
    @DisplayName("Should match correct password")
    void test_05(){
        String rawPassword = "StrongPassword@123";
        String encodedPassword = "argon2id-encoded-hash";

        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);

        boolean result = passwordService.matches(rawPassword, encodedPassword);

        assertThat(result).isTrue();

        verify(passwordEncoder, times(1)).matches(rawPassword, encodedPassword);
        verifyNoMoreInteractions(passwordEncoder);
    }
    @Test
    @DisplayName("Should reject incorrect password")
    void test_06(){
        String rawPassword = "WrongPassword@123";
        String encodedPassword = "argon2id-encoded-hash";

        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(false);

        boolean result = passwordService.matches(rawPassword, encodedPassword);

        assertThat(result).isFalse();

        verify(passwordEncoder, times(1)).matches(rawPassword, encodedPassword);
        verifyNoMoreInteractions(passwordEncoder);
    }
    @Test
    @DisplayName("Should reject null raw password")
    void test_07(){
        boolean result = passwordService.matches(null, "argon2id-encoded-hash");

        assertThat(result).isFalse();

        verifyNoInteractions(passwordEncoder);
    }
    @Test
    @DisplayName("Should reject null encoded password")
    void test_08(){
        boolean result = passwordService.matches("StrongPassword@123",null);

        assertThat(result).isFalse();

        verifyNoInteractions(passwordEncoder);
    }
    @Test
    @DisplayName("Should reject blank encoded password")
    void test_09(){
        boolean result = passwordService.matches("StrongPassword@123","   ");

        assertThat(result).isFalse();

        verifyNoInteractions(passwordEncoder);
    }
    @Test
    @DisplayName("Should indicate when password encoding needs upgrade")
    void test_10(){
        String encodedPassword = "argon2id-encoded-hash";

        when(passwordEncoder.upgradeEncoding(encodedPassword)).thenReturn(true);

        boolean result = passwordService.upgradeEncoding(encodedPassword);

        assertThat(result).isTrue();

        verify(passwordEncoder, times(1)).upgradeEncoding(encodedPassword);
        verifyNoMoreInteractions(passwordEncoder);
    }
    @Test
    @DisplayName("Should indicate when password encoding does not need upgrade")
    void test_11(){
        String encodedPassword = "argon2id-encoded-hash";

        when(passwordEncoder.upgradeEncoding(encodedPassword)).thenReturn(false);

        boolean result = passwordService.upgradeEncoding(encodedPassword);

        assertThat(result).isFalse();

        verify(passwordEncoder, times(1)).upgradeEncoding(encodedPassword);
        verifyNoMoreInteractions(passwordEncoder);
    }
    @Test
    @DisplayName("Should reject null or blank encoded password")
    void test_12(){
        assertThat(passwordService.upgradeEncoding(null)).isFalse();
        assertThat(passwordService.upgradeEncoding("   ")).isFalse();

        verifyNoInteractions(passwordEncoder);
    }
}
