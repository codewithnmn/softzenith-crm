package com.softzenith.crm;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

/** Fails the build if a module reaches into another module's internals or modules depend on each other cyclically. */
class ModularityTests {

    @Test
    void verifiesModuleBoundaries() {
        ApplicationModules.of(CrmApplication.class).verify();
    }
}
