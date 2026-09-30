package com.softzenith.crm.shared.web;

import com.softzenith.crm.shared.logging.LogContext;
import jakarta.servlet.http.HttpServletRequest;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.sql.SQLException;
import java.util.stream.Collectors;

/**
 * Maps exceptions to RFC 9457 problem responses and logs each one: expected client errors (4xx) as one line, anything
 * unexpected as ERROR with the stack trace. Every problem body carries the {@code requestId} to look up in the logs.
 */
@RestControllerAdvice
class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Lists the invalid fields, e.g. "phone: must not be blank; email: must be a well-formed email address". */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException e, HttpHeaders headers,
                                                                  HttpStatusCode status, WebRequest request) {
        var detail = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .sorted()
                .collect(Collectors.joining("; "));
        log.info("400 validation failed: {}", detail);
        return ResponseEntity.badRequest().body(problem(HttpStatus.BAD_REQUEST, detail));
    }

    /**
     * Spring MVC's own errors (malformed JSON, wrong method, missing parameter...). The request id is added after
     * {@code super}, because for many of them (e.g. 405) Spring only builds the problem body there.
     */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception e, Object body, HttpHeaders headers,
                                                             HttpStatusCode status, WebRequest request) {
        if (status.is5xxServerError()) {
            log.error("{} {}", status.value(), e.getMessage(), e);
        } else {
            log.info("{} {}", status.value(), e.getMessage());
        }
        var response = super.handleExceptionInternal(e, body, headers, status, request);
        if (response != null && response.getBody() instanceof ProblemDetail pd) {
            pd.setProperty("requestId", LogContext.requestId());
        }
        return response;
    }

    @ExceptionHandler(NotFoundException.class)
    ProblemDetail notFound(NotFoundException e) {
        log.info("404 {}", e.getMessage());
        return problem(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    ProblemDetail conflict(ConflictException e) {
        log.info("409 {}", e.getMessage());
        return problem(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(InvalidInputException.class)
    ProblemDetail invalid(InvalidInputException e) {
        log.info("400 {}", e.getMessage());
        return problem(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(FeatureNotEntitledException.class)
    ProblemDetail featureNotEntitled(FeatureNotEntitledException e) {
        log.info("403 (plan) {}", e.getMessage());
        return problem(HttpStatus.FORBIDDEN, e.getMessage());
    }

    @ExceptionHandler(TooManyRequestsException.class)
    ResponseEntity<ProblemDetail> tooMany(TooManyRequestsException e) {
        log.warn("429 {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header(HttpHeaders.RETRY_AFTER, String.valueOf(e.retryAfterSeconds()))
                .body(problem(HttpStatus.TOO_MANY_REQUESTS, e.getMessage()));
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    ProblemDetail staleWrite(OptimisticLockingFailureException e) {
        log.warn("409 concurrent edit: {}", e.getMessage());
        return problem(HttpStatus.CONFLICT, "Record was modified by someone else; reload and retry");
    }

    /**
     * The database refused the write. Logs the constraint name and SQL state only: the database's message repeats
     * the offending values (phones, emails), which must not reach the logs.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail integrity(DataIntegrityViolationException e) {
        String constraint = null, sqlState = null;
        for (Throwable t = e; t != null; t = t.getCause()) {
            if (t instanceof ConstraintViolationException cve && constraint == null) constraint = cve.getConstraintName();
            if (t instanceof SQLException sql && sqlState == null) sqlState = sql.getSQLState();
        }
        log.warn("409 data integrity violation: constraint={} sqlState={}", constraint, sqlState);
        return problem(HttpStatus.CONFLICT, "Request conflicts with existing data");
    }

    /** Anything not mapped above is a bug or an outage: log everything, tell the client only the request id. */
    @ExceptionHandler(Exception.class)
    ProblemDetail unexpected(Exception e, HttpServletRequest request) throws Exception {
        if (e instanceof AccessDeniedException || e instanceof AuthenticationException) {
            throw e; // Spring Security turns these into 403 / 401
        }
        log.error("500 unexpected error on {} {}", request.getMethod(), request.getRequestURI(), e);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR,
                "Something went wrong on our side. Quote request id " + LogContext.requestId() + " when reporting it.");
    }

    private static ProblemDetail problem(HttpStatus status, String detail) {
        var pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setProperty("requestId", LogContext.requestId());
        return pd;
    }
}
