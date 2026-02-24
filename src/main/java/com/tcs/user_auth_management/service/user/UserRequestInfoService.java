package com.tcs.user_auth_management.service.user;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.tcs.user_auth_management.model.dto.DtoUserRequestInfo;
import com.tcs.user_auth_management.util.ClientRequestUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class UserRequestInfoService {
  private final DatabaseReader databaseReader;

  public DtoUserRequestInfo userRequestInfo(HttpServletRequest httpServletRequest) {
    var location = getLocationInfo(ClientRequestUtil.getClientIp(httpServletRequest));
    return location
        .map(cityResponse -> new DtoUserRequestInfo(httpServletRequest, cityResponse))
        .orElseGet(() -> new DtoUserRequestInfo(httpServletRequest));
  }

  public Optional<CityResponse> getLocationInfo(String ip) {
    log.info("Looking up location for IP: {}", ip);
    if (StringUtils.isBlank(ip)) return Optional.empty();
    try {
      InetAddress ipAddress = InetAddress.getByName(ip);
      CityResponse response = databaseReader.city(ipAddress);
      return Optional.ofNullable(response);
    } catch (GeoIp2Exception | IOException e) {
      log.warn("Location resolver fail to resolve");
      return Optional.empty();
    }
  }
}
