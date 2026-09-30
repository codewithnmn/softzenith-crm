package com.softzenith.crm.shared.logging;

import org.slf4j.MDC;

/**
 * Keys put in the logging MDC so every log line says which request, tenant and user it belongs to
 * (see the {@code logging.pattern.level} setting). The request id is also returned to clients in the
 * {@value #REQUEST_ID_HEADER} header and in error bodies, so a reported problem can be found in the logs.
 */
public final class LogContext {

    public static final String REQUEST_ID_HEADER = "X-Request-Id";

    public static final String REQUEST_ID = "requestId";
    public static final String TENANT_ID = "tenantId";
    public static final String USER_ID = "userId";

    private LogContext() {
    }

    /** The current request id, or null outside a request (e.g. a startup job). */
    public static String requestId() {
        return MDC.get(REQUEST_ID);
    }
}
