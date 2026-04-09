package com.tcs.user_auth_management.util;

import com.tcs.user_auth_management.exception.ApiExceptionStatusException;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class StringParseUtil {
    public static UUID toUuid(String value){
        try{
            return UUID.fromString(value);
        }catch (IllegalArgumentException e){
            throw new ApiExceptionStatusException("Invalid jwt payload", HttpStatus.UNAUTHORIZED);
        }
    }
}
