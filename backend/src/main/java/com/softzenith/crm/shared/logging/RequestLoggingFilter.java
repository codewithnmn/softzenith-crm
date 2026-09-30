package com.softzenith.crm.shared.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * First filter of every request: assigns the request id (or keeps a well-formed one sent by the caller, e.g. the
 * website's server), returns it in {@value LogContext#REQUEST_ID_HEADER}, and writes one access-log line with
 * method, path, status and duration once the request is done. Query strings are not logged (they carry searches
 * by phone or name).
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger("crm.access");
    private static final Pattern SAFE_ID = Pattern.compile("[A-Za-z0-9._-]{8,64}");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        var incoming = request.getHeader(LogContext.REQUEST_ID_HEADER);
        var requestId = incoming != null && SAFE_ID.matcher(incoming).matches()
                ? incoming : UUID.randomUUID().toString().substring(0, 13);
        MDC.put(LogContext.REQUEST_ID, requestId);
        response.setHeader(LogContext.REQUEST_ID_HEADER, requestId);

        var start = System.nanoTime();
        var failed = false;
        try {
            chain.doFilter(request, response);
        } catch (IOException | ServletException | RuntimeException e) {
            failed = true;
            log.error("{} {} failed with an unhandled exception", request.getMethod(), request.getRequestURI(), e);
            throw e;
        } finally {
            var ms = (System.nanoTime() - start) / 1_000_000;
            var status = failed ? 500 : response.getStatus();
            var path = request.getRequestURI();
            if (isNoise(path)) {
                log.debug("{} {} -> {} in {} ms", request.getMethod(), path, status, ms);
            } else if (status >= 500) {
                log.warn("{} {} -> {} in {} ms", request.getMethod(), path, status, ms);
            } else {
                log.info("{} {} -> {} in {} ms", request.getMethod(), path, status, ms);
            }
            MDC.clear();
        }
    }

    private static boolean isNoise(String path) {
        return path.startsWith("/actuator/health") || path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs");
    }
}
