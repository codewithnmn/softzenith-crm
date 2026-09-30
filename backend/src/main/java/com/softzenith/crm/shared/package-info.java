/**
 * Shared kernel: persistence base classes, error handling, phone normalisation and cross-cutting config.
 * Open module: every other module may depend on it; it depends on none of them.
 */
@org.springframework.modulith.ApplicationModule(type = org.springframework.modulith.ApplicationModule.Type.OPEN)
package com.softzenith.crm.shared;
