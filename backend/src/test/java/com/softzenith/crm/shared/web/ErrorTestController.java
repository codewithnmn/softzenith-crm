package com.softzenith.crm.shared.web;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;

import java.sql.SQLException;

/** Test-only endpoints that raise each exception {@link GlobalExceptionHandler} maps (see ErrorHandlingTests). */
@RestController
class ErrorTestController {

    @GetMapping("/api/v1/public/test-only/stale")
    String stale() {
        throw new OptimisticLockingFailureException("row was updated by another transaction");
    }

    @GetMapping("/api/v1/public/test-only/integrity")
    String integrity() {
        var sql = new SQLException("duplicate key value (phone)=(+919999999999)", "23505");
        throw new DataIntegrityViolationException("could not execute statement",
                new org.hibernate.exception.ConstraintViolationException("duplicate", sql, "leads_open_person_uq"));
    }

    @GetMapping("/api/v1/public/test-only/timeout")
    String timeout() {
        throw new AsyncRequestTimeoutException();
    }

    @PostMapping("/api/v1/public/test-only/echo")
    Echo echo(@RequestBody Echo body) {
        return body;
    }

    record Echo(String text) {
    }
}
