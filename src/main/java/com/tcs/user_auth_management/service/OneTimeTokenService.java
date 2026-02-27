package com.tcs.user_auth_management.service;

import com.tcs.user_auth_management.emuns.JwtTokenType;
import com.tcs.user_auth_management.exception.ApiExceptionStatusException;
import com.tcs.user_auth_management.model.dto.DtoJwtClaim;
import com.tcs.user_auth_management.model.entity.OneTimeToken;
import com.tcs.user_auth_management.model.entity.user.UserAuth;
import com.tcs.user_auth_management.repository.OneTimeTokenRepository;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class OneTimeTokenService {
  private final OneTimeTokenRepository repository;
  private final JwtEncoder encoder;
  private final TokenJwtVerifyService tokenJwtVerifyService;

  public String resetToken(UserAuth userAuth) {
    return generateOneTimeJwt(userAuth, JwtTokenType.RESET_PASSWORD);
  }

  public String verifyEmailToken(UserAuth userAuth) {
    return generateOneTimeJwt(userAuth, JwtTokenType.VERIFY_EMAIL);
  }

  public Jwt useOneTimeToken(String token, JwtTokenType type) {
    var jwt = this.tokenJwtVerifyService.verifyToken(token, type);
    var oneTimeToken =
        repository
            .findById(UUID.fromString(jwt.getId()))
            .orElseThrow(
                () ->
                    new ApiExceptionStatusException("Invalid jwt token", HttpStatus.UNAUTHORIZED));
    repository.deleteById(oneTimeToken.getId());
    return jwt;
  }

  private String generateOneTimeJwt(UserAuth userAuth, JwtTokenType tokenType) {
    Instant now = Instant.now();
    long tokenExpirySeconds = 300;
    Instant expireTime = now.plusSeconds(tokenExpirySeconds);

    var oneTimeToken = this.createOneTimeToken(userAuth.getId(), expireTime);

    JwtClaimsSet claims =
        DtoJwtClaim.baseClaim(
                oneTimeToken.getId().toString(), userAuth.getId().toString(), tokenType)
            .issuedAt(now)
            .expiresAt(expireTime)
            .build();

    return encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
  }

  public OneTimeToken createOneTimeToken(UUID userAuthId, Instant expireTime) {
    var oneTime = new OneTimeToken();
    oneTime.setExpireTime(expireTime);
    if (Objects.nonNull(userAuthId)) {
      oneTime.setBindingId(userAuthId.toString());
    }
    repository.save(oneTime);
    return oneTime;
  }
}
