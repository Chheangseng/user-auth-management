package com.tcs.user_auth_management.util;

public class Util {
  public static String camelToSnake(String str) {
    return str.replaceAll("([a-z])([A-Z]+)", "$1_$2").toLowerCase();
  }
}
