package com.softzenith.crm.shared.web;

/** The request is valid but clashes with current state (duplicate, illegal status transition, ...). */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
