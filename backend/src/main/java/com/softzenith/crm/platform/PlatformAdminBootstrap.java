package com.softzenith.crm.platform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Creates the first platform admin from {@code crm.platform.bootstrap-admin.*} (env CRM_PLATFORM_ADMIN_USERNAME /
 * _PASSWORD) when there is none yet. Never changes an existing admin, so the values can (and should) be removed after
 * the first start.
 */
@Component
class PlatformAdminBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(PlatformAdminBootstrap.class);

    private final PlatformAuthService auth;
    private final String username;
    private final String password;

    PlatformAdminBootstrap(PlatformAuthService auth,
                           @Value("${crm.platform.bootstrap-admin.username:}") String username,
                           @Value("${crm.platform.bootstrap-admin.password:}") String password) {
        this.auth = auth;
        this.username = username;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (auth.anyAdminExists()) {
            return;
        }
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            log.info("No platform admin yet: set CRM_PLATFORM_ADMIN_USERNAME and CRM_PLATFORM_ADMIN_PASSWORD to create one");
            return;
        }
        auth.createAdmin(username, password);
    }
}
