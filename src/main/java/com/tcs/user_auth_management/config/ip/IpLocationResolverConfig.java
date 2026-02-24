package com.tcs.user_auth_management.config.ip;

import com.maxmind.geoip2.DatabaseReader;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

@Configuration
public class IpLocationResolverConfig {
  @Bean
  public DatabaseReader databaseReader(ResourceLoader resourceLoader) throws IOException {
    Resource resource = resourceLoader.getResource("classpath:ip-location/GeoLite2-City.mmdb");

    try (InputStream inputStream = resource.getInputStream()) {
      return new DatabaseReader.Builder(inputStream).build();
    }
  }
}