package com.user.config;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;

public class RsaKeyGeneratorTest {

    @Test
    void generateRsaKeyPair() throws Exception{
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");

        generator.initialize(3072);

        KeyPair keyPair = generator.generateKeyPair();

        String privateKey = Base64.getMimeEncoder(
                64, StandardCharsets.UTF_8.encode("\n").array()
        ).encodeToString(keyPair.getPrivate().getEncoded());

        String publicKey = Base64.getMimeEncoder(
                64,StandardCharsets.UTF_8.encode("\n").array()
        ).encodeToString(keyPair.getPublic().getEncoded());

        System.out.println("\n===== PRIVATE KEY =====");
        System.out.println("-----BEGIN PRIVATE KEY-----");
        System.out.println(privateKey);
        System.out.println("-----END PRIVATE KEY-----");

        System.out.println("\n===== PUBLIC KEY =====");
        System.out.println("-----BEGIN PUBLIC KEY-----");
        System.out.println(publicKey);
        System.out.println("-----END PUBLIC KEY-----");

    }
}
