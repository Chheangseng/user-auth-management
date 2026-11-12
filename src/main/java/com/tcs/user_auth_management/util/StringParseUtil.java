package com.tcs.user_auth_management.util;

import com.tcs.user_auth_management.exception.AppException;
import com.tcs.user_auth_management.exception.errorcode.AuthErrorCode;
import java.util.UUID;

public class StringParseUtil {
    public static UUID toUuid(String value){
        try{
            return UUID.fromString(value);
        }catch (IllegalArgumentException e){
            throw new AppException(AuthErrorCode.INVALID_TOKEN_PAYLOAD, "Invalid jwt payload");
        }
    }
}
