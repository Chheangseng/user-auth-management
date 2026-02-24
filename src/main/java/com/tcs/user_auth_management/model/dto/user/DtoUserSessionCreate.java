package com.tcs.user_auth_management.model.dto.user;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;
@Data
public class DtoUserSessionCreate {
    private Instant expiryDate;

    private UUID userAuthId;

    private boolean invoked = false;
}
