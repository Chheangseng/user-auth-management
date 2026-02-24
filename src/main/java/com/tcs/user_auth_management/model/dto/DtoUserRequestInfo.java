package com.tcs.user_auth_management.model.dto;

import com.maxmind.geoip2.model.CityResponse;
import com.tcs.user_auth_management.util.ClientRequestUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;

@Data
public class DtoUserRequestInfo {
  private final String ip;
  private final String userAgent;
  private final DtoRequestLocation location;

  public DtoUserRequestInfo(HttpServletRequest request, CityResponse cityResponse) {
    this.ip = ClientRequestUtil.getClientIp(request);
    this.userAgent = ClientRequestUtil.getUserAgent(request);
    this.location = new DtoRequestLocation(cityResponse);
  }

  public DtoUserRequestInfo(HttpServletRequest request) {
    this.ip = ClientRequestUtil.getClientIp(request);
    this.userAgent = ClientRequestUtil.getUserAgent(request);
    this.location = null;
  }
}
