package com.tcs.user_auth_management.service.oidc;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import org.springframework.stereotype.Component;

@Component
public class PkceValidator {

  public boolean validate(String codeVerifier, String codeChallenge, String codeChallengeMethod) {
    if (codeChallenge == null || codeChallenge.isBlank()) {
      return false;
    }
    if (codeVerifier == null || codeVerifier.isBlank()) {
      return false;
    }

    String method = (codeChallengeMethod == null || codeChallengeMethod.isBlank())
        ? "plain"
        : codeChallengeMethod.toUpperCase();

    if ("PLAIN".equals(method)) {
      return codeVerifier.equals(codeChallenge);
    } else if ("S256".equals(method)) {
      try {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
        String computed = Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        return computed.equals(codeChallenge);
      } catch (NoSuchAlgorithmException e) {
        throw new RuntimeException("SHA-256 algorithm not available", e);
      }
    }
    return false;
  }
}
