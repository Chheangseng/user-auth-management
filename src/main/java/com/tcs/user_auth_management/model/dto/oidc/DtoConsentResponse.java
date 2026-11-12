package com.tcs.user_auth_management.model.dto.oidc;

import java.util.List;

public record DtoConsentResponse(
    String clientId,
    String clientName,
    List<String> requestedScopes,
    String state,
    String redirectUri
) {}
