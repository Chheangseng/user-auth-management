package com.tcs.user_auth_management.model.dto;

import java.util.Set;
import java.util.UUID;

public record DtoUserAuth(
        UUID userId,
        UUID jwtId,
        UUID sessionId,
        String roleId,
        String roleName,
        Set<String> permissions
) {}
