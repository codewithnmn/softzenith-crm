package com.softzenith.crm.shared.web;

import com.softzenith.crm.shared.logging.LogContext;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;

/** Writes RFC 9457 problem bodies from filters, which run before Spring MVC's exception handling. */
public final class ProblemResponses {

    private ProblemResponses() {
    }

    /** @param detail fixed server-side text only; never echo request input here */
    public static void write(HttpServletResponse response, HttpStatus status, String detail) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        var requestId = LogContext.requestId();
        response.getWriter().write("{\"type\":\"about:blank\",\"title\":\"" + status.getReasonPhrase()
                + "\",\"status\":" + status.value() + ",\"detail\":\"" + detail + "\""
                + (requestId == null ? "" : ",\"requestId\":\"" + requestId + "\"") + "}");
    }
}
