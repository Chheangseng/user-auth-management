package com.tcs.user_auth_management.model.dto;

import com.maxmind.geoip2.model.CityResponse;
import lombok.Data;
import org.apache.commons.lang3.ObjectUtils;

@Data
public class DtoLocation {
  private String countryName;
  private String countryCode;
  private String region;
  private String city;
  private double latitude;
  private double longitude;
  private String timeZone;

  public DtoLocation(CityResponse cityResponse) {
    if (cityResponse != null) {
      if (cityResponse.getCountry() != null) {
        this.countryName = cityResponse.getCountry().getName();
        this.countryCode = cityResponse.getCountry().getIsoCode();
      }

      if (cityResponse.getMostSpecificSubdivision() != null) {
        this.region = cityResponse.getMostSpecificSubdivision().getName();
      }

      if (cityResponse.getCity() != null) {
        this.city = cityResponse.getCity().getName();
      }

      if (cityResponse.getLocation() != null) {
        this.latitude = ObjectUtils.getIfNull(cityResponse.getLocation().getLatitude(), 0.0);
        this.longitude = ObjectUtils.getIfNull(cityResponse.getLocation().getLongitude(), 0.0);
        this.timeZone = cityResponse.getLocation().getTimeZone();
      }
    }
  }
}
