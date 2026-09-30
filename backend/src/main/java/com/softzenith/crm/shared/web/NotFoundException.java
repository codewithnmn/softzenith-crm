package com.softzenith.crm.shared.web;

import com.softzenith.crm.shared.text.Text;

public class NotFoundException extends RuntimeException {

    /** @param id may come from the URL, so it is cleaned before it reaches the logs */
    public NotFoundException(String entity, Object id) {
        super(entity + " " + Text.singleLine(String.valueOf(id)) + " not found");
    }
}
