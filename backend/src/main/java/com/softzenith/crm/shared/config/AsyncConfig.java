package com.softzenith.crm.shared.config;

import com.softzenith.crm.shared.logging.LogContext;
import io.micrometer.context.ContextRegistry;
import io.micrometer.context.integration.Slf4jThreadLocalAccessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/** Enables {@code @Async} event listeners (e.g. notifications after a lead commits). Runs on virtual threads. */
@Configuration(proxyBeanMethods = false)
@EnableAsync
class AsyncConfig {

    static {
        // Spring Boot's task executor copies registered context into async work (ContextPropagatingTaskDecorator).
        // Registering the logging keys means a notification that fails can be traced to the request that caused it.
        ContextRegistry.getInstance().registerThreadLocalAccessor(
                new Slf4jThreadLocalAccessor(LogContext.REQUEST_ID, LogContext.TENANT_ID, LogContext.USER_ID));
    }
}
