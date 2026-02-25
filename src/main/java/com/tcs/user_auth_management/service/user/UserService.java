package com.tcs.user_auth_management.service.user;

import com.tcs.user_auth_management.repository.UserSessionRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {
    private final UserSessionRepository userSessionRepository;
}
