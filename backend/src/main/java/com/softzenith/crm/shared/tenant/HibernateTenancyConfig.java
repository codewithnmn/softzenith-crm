package com.softzenith.crm.shared.tenant;

import org.hibernate.cfg.MultiTenancySettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

/** Plugs {@link TenantContext} into Hibernate's discriminator-based multi-tenancy ({@code @TenantId}). */
@Configuration(proxyBeanMethods = false)
class HibernateTenancyConfig {

    @Bean
    HibernatePropertiesCustomizer tenantIdentifierResolver() {
        return properties -> properties.put(MultiTenancySettings.MULTI_TENANT_IDENTIFIER_RESOLVER, new ContextTenantResolver());
    }

    static final class ContextTenantResolver implements CurrentTenantIdentifierResolver<UUID> {

        @Override
        public UUID resolveCurrentTenantIdentifier() {
            return TenantContext.resolve();
        }

        @Override
        public boolean validateExistingCurrentSessions() {
            return false;
        }

        @Override
        public boolean isRoot(UUID tenantId) {
            return TenantContext.SYSTEM.equals(tenantId);
        }
    }
}
