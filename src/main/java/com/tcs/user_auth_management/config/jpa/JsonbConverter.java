package com.tcs.user_auth_management.config.jpa;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class JsonbConverter implements AttributeConverter<Object, String> {

  private static final ObjectMapper mapper = new ObjectMapper()
          .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);


  @Override
  public String convertToDatabaseColumn(Object attribute) {
    if (attribute == null) return null;
    try {
      return mapper.writeValueAsString(attribute);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Failed to convert to JSON", e);
    }
  }

  @Override
  public Object convertToEntityAttribute(String dbData) {
    if (dbData == null) return null;
    try {
      return mapper.readValue(dbData, Object.class);
    } catch (Exception e) {
      throw new RuntimeException("Failed to convert from JSON", e);
    }
  }
}
