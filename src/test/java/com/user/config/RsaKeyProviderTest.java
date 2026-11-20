package com.user.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;

import static org.assertj.core.api.Assertions.assertThat;

class RsaKeyProviderTest {

    @Test
    @DisplayName("Should load matching RSA keys and validate their signature")
    void test_01() throws Exception{
        RsaKeyProvider provider = new RsaKeyProvider();

        JwtProperties properties = new JwtProperties(
                "user-api",
                900,
                System.getenv("JWT_PRIVATE_KEY_PATH"),
                System.getenv("JWT_PUBLIC_KEY_PATH")
        );

        PrivateKey privateKey = provider.jwtPrivateKey(properties);
        PublicKey publicKey = provider.jwtPublicKey(properties);

        byte[] message = "user-api-rsa-test".getBytes();

        Signature signer = Signature.getInstance("SHA256withRSA");
        signer.initSign(privateKey);
        signer.update(message);

        byte[] signedMessage = signer.sign();

        Signature verifier = Signature.getInstance("SHA256withRSA");
        verifier.initVerify(publicKey);
        verifier.update(message);

        assertThat(verifier.verify(signedMessage)).isTrue();
    }
}
