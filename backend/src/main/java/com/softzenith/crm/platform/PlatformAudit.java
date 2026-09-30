package com.softzenith.crm.platform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

/** Append-only record of platform actions (platform_audit): who did what to which tenant. Joins the caller's transaction. */
@Component
class PlatformAudit {

    private static final Logger log = LoggerFactory.getLogger(PlatformAudit.class);

    private final JdbcClient jdbc;

    PlatformAudit(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /** @param detail never passwords or tokens */
    void record(String admin, String action, String target, String detail) {
        jdbc.sql("insert into platform_audit (admin, action, target, detail) values (?, ?, ?, ?)")
                .params(admin, action, target, detail)
                .update();
        log.info("Platform audit: {} {} {}", admin, action, target == null ? "" : target);
    }
}
