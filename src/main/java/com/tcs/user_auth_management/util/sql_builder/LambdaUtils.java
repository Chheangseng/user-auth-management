package com.tcs.user_auth_management.util.sql_builder;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;

public class LambdaUtils {
    public static <T> String getPropertyName(SFunction<T, ?> func) {
        try {
            Method method = func.getClass().getDeclaredMethod("writeReplace");
            method.setAccessible(true);
            SerializedLambda lambda = (SerializedLambda) method.invoke(func);
            String methodName = lambda.getImplMethodName();
            String prop = methodName.startsWith("get") ? methodName.substring(3) : methodName.substring(2);
            return Character.toLowerCase(prop.charAt(0)) + prop.substring(1);
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract column name", e);
        }
    }
}
