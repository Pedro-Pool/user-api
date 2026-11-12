package com.user.identity.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordService {

    private final PasswordEncoder passwordEncoder;

    public PasswordService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public String encode(String rawPassword){
        if (rawPassword == null || rawPassword.isBlank()){
            throw new IllegalArgumentException("Password is required");
        }

        return passwordEncoder.encode(rawPassword);
    }
}
