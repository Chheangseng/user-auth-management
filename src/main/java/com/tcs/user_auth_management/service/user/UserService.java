package com.tcs.user_auth_management.service.user;

import com.tcs.user_auth_management.model.dto.DtoUserSession;
import com.tcs.user_auth_management.repository.UserSessionRepository;
import com.tcs.user_auth_management.util.pagination.PaginationParam;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {
  private final UserSessionRepository userSessionRepository;

  public Page<DtoUserSession> userSessionPage(PaginationParam pagination) {
    var response = userSessionRepository.findAll(pagination.toPageable());
    return response.map(DtoUserSession::new);
  }
}
