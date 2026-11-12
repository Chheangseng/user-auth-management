package com.tcs.user_auth_management.util;

import com.fasterxml.uuid.Generators;
import jakarta.servlet.http.HttpServletRequest;

public final class TraceIdUtil {
  public static final String TRACE_HEADER = "X-Trace-Id";
  public static final String REQUEST_ID_HEADER = "X-Request-Id";

  private TraceIdUtil() {}

  public static String getOrCreateTraceId(HttpServletRequest request) {
    if (request != null) {
      String traceId = request.getHeader(TRACE_HEADER);
      if (traceId != null && !traceId.isBlank()) {
        return traceId;
      }
      String requestId = request.getHeader(REQUEST_ID_HEADER);
      if (requestId != null && !requestId.isBlank()) {
        return requestId;
      }
      Object attr = request.getAttribute(TRACE_HEADER);
      if (attr instanceof String s && !s.isBlank()) {
        return s;
      }
    }
    String generated = Generators.timeBasedEpochGenerator().generate().toString();
    if (request != null) {
      request.setAttribute(TRACE_HEADER, generated);
    }
    return generated;
  }
}
