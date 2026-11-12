# Unexpected Error Logging Guidelines

## Purpose
Enforce robust diagnostic logging across the entire codebase whenever an unexpected error, unhandled exception, or failure condition occurs.

## Core Rules

1. **Always Log Unexpected Errors**:
   - Any unexpected exception, unhandled edge case, catch-all block, or fallback branch must log an error using SLF4J at `ERROR` level (`log.error(...)`).
   - Never let an unexpected error fail silently or go unrecorded.

2. **Always Include the Throwable / Stack Trace**:
   - Always pass the exception instance as the last argument in `log.error(...)`:
     ```java
     log.error("Unexpected error processing operation for user {}: {}", userId, ex.getMessage(), ex);
     ```
   - Never log only the message (e.g., `log.error(e.getMessage())`), as this drops the stack trace.

3. **Never Silently Swallow Unexpected Exceptions**:
   - Empty catch blocks (`catch (Exception ignored) {}`) are strictly forbidden for unexpected or unknown exceptions.
   - If an exception must be ignored by design (e.g., RFC 7009 token revocation where invalid tokens do not return an error), you must:
     - Document the explicit reason in a code comment.
     - Log at `DEBUG` or `WARN` level if diagnostic tracing might be needed.

4. **Preserve Context & Correlate with Trace ID**:
   - Include relevant business identifiers (`userId`, `clientId`, `sessionId`, request URI) in the log message.
   - When handling requests or filters, include or preserve the `traceId` (from `TraceIdUtil` or MDC).

5. **Distinguish Domain Exceptions from Unexpected Exceptions**:
   - Expected domain exceptions (`AppException`) represent validation or business rejections; they are handled via `ApiExceptionHandler` with standard `ErrorResponse`.
   - Any unexpected `RuntimeException`, `NullPointerException`, `SQLException`, or generic `Exception` must be logged with its full stack trace.
