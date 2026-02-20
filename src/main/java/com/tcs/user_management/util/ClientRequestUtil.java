package com.tcs.user_management.util;

import jakarta.servlet.http.HttpServletRequest;

public class ClientRequestUtil {
  public static String getClientIp(HttpServletRequest request) {
    String ip = request.getHeader("X-Forwarded-For"); // standard header for proxied requests
    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("Proxy-Client-IP");
    }
    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("WL-Proxy-Client-IP"); // WebLogic
    }
    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("HTTP_CLIENT_IP");
    }
    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("HTTP_X_FORWARDED_FOR");
    }
    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getRemoteAddr();
    }

    if (ip != null && ip.contains(",")) {
      ip = ip.split(",")[0].trim();
    }

    if (ip == null || ip.isEmpty()) {
      ip = "UNKNOWN";
    }

    return ip;
  }

  public static String getUserAgent(HttpServletRequest request) {
    String ua = request.getHeader("User-Agent");
    return (ua == null || ua.isEmpty()) ? "UNKNOWN" : ua;
  }
}
