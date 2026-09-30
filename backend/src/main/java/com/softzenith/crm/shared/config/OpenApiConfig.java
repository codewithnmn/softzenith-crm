package com.softzenith.crm.shared.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
class OpenApiConfig {

    private static final String BEARER = "supabaseJwt";
    private static final String TENANT = "tenantHeader";

    @Bean
    OpenAPI crmOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("SoftZenith CRM API")
                        .version("v1")
                        .description("""
                                Multi-tenant CRM platform. First tenant: Western World.

                                Staff sign in with phone OTP via Supabase Auth and send the access token as a Bearer token. \
                                Users who are staff of several tenants pick one with the X-Tenant-ID header \
                                (see GET /api/v1/me/memberships)."""))
                .components(new Components()
                        .addSecuritySchemes(BEARER, new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT"))
                        .addSecuritySchemes(TENANT, new SecurityScheme().type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER).name("X-Tenant-ID")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER).addList(TENANT));
    }
}
