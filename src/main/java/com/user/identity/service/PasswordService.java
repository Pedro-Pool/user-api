package com.user.identity.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class PasswordService {

    private final PasswordEncoder passwordEncoder;

    public PasswordService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = Objects.requireNonNull(passwordEncoder);
    }

    public String encode(String rawPassword){
        if (rawPassword == null || rawPassword.isBlank()){
            throw new IllegalArgumentException("Password is required");
        }

        return passwordEncoder.encode(rawPassword);
    }
    public boolean matches(String rawPassword, String encodedPassword){
        if (rawPassword == null || encodedPassword == null || encodedPassword.isBlank()){
            return false;
        }

        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
    public boolean upgradeEncoding(String encodedPassword){
        if (encodedPassword == null || encodedPassword.isBlank()){
            return false;
        }

        return passwordEncoder.upgradeEncoding(encodedPassword);
    }
}
