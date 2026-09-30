package com.softzenith.crm.shared.persistence;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/** created_by / updated_by come from the AuditorAware bean in the identity module (the signed-in staff user). */
@Configuration(proxyBeanMethods = false)
@EnableJpaAuditing
class JpaAuditingConfig {
}
