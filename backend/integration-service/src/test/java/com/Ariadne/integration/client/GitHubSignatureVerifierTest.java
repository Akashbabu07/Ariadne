package com.Ariadne.integration.client;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class GitHubSignatureVerifierTest {

    private GitHubSignatureVerifier verifierWithSecret(String secret) {
        GitHubSignatureVerifier verifier = new GitHubSignatureVerifier();
        ReflectionTestUtils.setField(verifier, "webhookSecret", secret);
        return verifier;
    }

    @Test
    void isValid_knownGitHubExampleVector_returnsTrue() {

        GitHubSignatureVerifier verifier = verifierWithSecret("It's a Secret to Everybody");
        String payload = "Hello, World!";
        String signature = "sha256=757107ea0eb2509fc211221cce984b8a37570b6d7586c22c46f4379c8b043e17";

        assertThat(verifier.isValid(payload, signature)).isTrue();
    }

    @Test
    void isValid_wrongSecret_returnsFalse() {
        GitHubSignatureVerifier verifier = verifierWithSecret("wrong-secret");
        String signature = "sha256=757107ea0eb2509fc211221cce984b8a37570b6d7586c22c46f4379c8b043e17";

        assertThat(verifier.isValid("Hello, World!", signature)).isFalse();
    }

    @Test
    void isValid_tamperedPayload_returnsFalse() {
        GitHubSignatureVerifier verifier = verifierWithSecret("It's a Secret to Everybody");
        String signature = "sha256=757107ea0eb2509fc211221cce984b8a37570b6d7586c22c46f4379c8b043e17";

         assertThat(verifier.isValid("Hello, World?", signature)).isFalse();
    }

    @Test
    void isValid_noSecretConfigured_failsClosed() {
        GitHubSignatureVerifier verifier = verifierWithSecret("");
        assertThat(verifier.isValid("anything", "sha256=deadbeef")).isFalse();
    }

    @Test
    void isValid_nullSecretConfigured_failsClosed() {
        GitHubSignatureVerifier verifier = verifierWithSecret(null);
        assertThat(verifier.isValid("anything", "sha256=deadbeef")).isFalse();
    }

    @Test
    void isValid_missingSignatureHeader_returnsFalse() {
        GitHubSignatureVerifier verifier = verifierWithSecret("some-secret");
        assertThat(verifier.isValid("payload", null)).isFalse();
    }

    @Test
    void isValid_signatureMissingSha256Prefix_returnsFalse() {
        GitHubSignatureVerifier verifier = verifierWithSecret("some-secret");
         assertThat(verifier.isValid("payload",
                "757107ea0eb2509fc211221cce984b8a37570b6d7586c22c46f4379c8b043e17")).isFalse();
    }

    @Test
    void isValid_malformedSignatureValue_doesNotThrow() {
        GitHubSignatureVerifier verifier = verifierWithSecret("some-secret");
        assertThat(verifier.isValid("payload", "sha256=not-valid-hex!!")).isFalse();
    }
}