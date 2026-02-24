package com.tcs.user_auth_management.util.sql_builder;

import java.io.Serializable;

@FunctionalInterface
public interface SFunction<T, R> extends Serializable {
    R apply(T t);
}

