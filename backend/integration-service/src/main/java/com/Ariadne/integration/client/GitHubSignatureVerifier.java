package com.Ariadne.integration.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

@Component
public class GitHubSignatureVerifier {

    @Value("${integration.github.webhook-secret:}")
    private String webhookSecret;

    public boolean isValid(String payload, String signatureHeader) {
        if (webhookSecret == null || webhookSecret.isBlank()) return false;
        if (signatureHeader == null || !signatureHeader.startsWith("sha256=")) return false;

        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] computed = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String computedHex = HexFormat.of().formatHex(computed);
            String provided = signatureHeader.substring("sha256=".length());
            return MessageDigest.isEqual(
                    computedHex.getBytes(StandardCharsets.UTF_8),
                    provided.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return false;
        }
    }
}