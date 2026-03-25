package com.tcs.user_auth_management.config.security.jwt;

import com.tcs.user_auth_management.exception.ApiExceptionStatusException;
import com.tcs.user_auth_management.model.dto.DtoJwtClaim;
import com.tcs.user_auth_management.model.entity.user.UserAuth;
import com.tcs.user_auth_management.model.entity.user.UserSecurity;
import com.tcs.user_auth_management.model.entity.user.UserSession;
import com.tcs.user_auth_management.service.AuthService;
import com.tcs.user_auth_management.service.user.UserSessionService;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import lombok.AllArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class AuthenticationJwtConverter implements Converter<Jwt, AbstractAuthenticationToken> {
  private final AuthService authService;
  private final UserSessionService userSessionService;
  private final Executor executor;

  @Override
  public AbstractAuthenticationToken convert(Jwt source) {
    try{
      UUID sessionId = DtoJwtClaim.getSessionId(source);
      UUID jwtId = DtoJwtClaim.getJwtId(source);
      UUID userId = DtoJwtClaim.getUserId(source);
      userSessionService.verifyUserSession(sessionId, jwtId);
      UserAuth user = authService.isUserActive(userId);
      var userSecurity = new UserSecurity(user, jwtId, sessionId);
      return new JwtAuthenticationToken(source, userSecurity.getAuthorities(), source.getSubject()) {
        @Override
        public Object getPrincipal() {
          return userSecurity;
        }
      };
    }catch (ApiExceptionStatusException e){
      throw e;
    }catch (Exception e){
      throw new ApiExceptionStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
