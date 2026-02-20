package com.tcs.user_management.config.ip;

import com.maxmind.geoip2.DatabaseReader;
import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class IpLocationResolverConfig {
  @Bean
  public DatabaseReader databaseReader() throws IOException {
    ClassPathResource resource = new ClassPathResource("ip-location/GeoLite2-City.mmdb");
    return new DatabaseReader.Builder(resource.getFile()).build();
  }
}
