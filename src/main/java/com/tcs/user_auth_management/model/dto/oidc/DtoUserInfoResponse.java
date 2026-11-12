package com.tcs.user_auth_management.model.dto.oidc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DtoUserInfoResponse(
    @JsonProperty("sub") String sub,
    @JsonProperty("name") String name,
    @JsonProperty("preferred_username") String preferredUsername,
    @JsonProperty("email") String email,
    @JsonProperty("email_verified") Boolean emailVerified,
    @JsonProperty("picture") String picture,
    @JsonProperty("phone_number") String phoneNumber,
    @JsonProperty("address") String address,
    @JsonProperty("locale") String locale,
    @JsonProperty("zoneinfo") String zoneinfo,
    @JsonProperty("updated_at") Long updatedAt
) {}
