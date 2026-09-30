package com.softzenith.crm.platform;

import com.softzenith.crm.shared.web.ProblemResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

/**
 * Its own filter chain for /api/platform/**, ahead of the staff chains: platform tokens only (see {@link PlatformTokens}),
 * no tenant context, no staff lookup. Only the login endpoint is open.
 */
@Configuration(proxyBeanMethods = false)
class PlatformSecurityConfig {

    static final String AUTHORITY = "PLATFORM_ADMIN";
    static final String LOGIN_PATH = "/api/platform/auth/login";

    private static final Logger log = LoggerFactory.getLogger(PlatformSecurityConfig.class);
    private static final BearerTokenAuthenticationEntryPoint bearer = new BearerTokenAuthenticationEntryPoint();

    @Bean
    @Order(0)
    SecurityFilterChain platformSecurity(HttpSecurity http, PlatformTokens tokens) throws Exception {
        var authorities = new JwtAuthenticationConverter();
        authorities.setJwtGrantedAuthoritiesConverter(jwt -> List.of(new SimpleGrantedAuthority(AUTHORITY)));
        // A stale token in the browser must not turn the login request itself into a 401.
        var tokenResolver = new DefaultBearerTokenResolver();

        return http
                .securityMatcher("/api/platform/**")
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, LOGIN_PATH).permitAll()
                        .anyRequest().hasAuthority(AUTHORITY))
                .oauth2ResourceServer(oauth -> oauth
                        .bearerTokenResolver(request -> LOGIN_PATH.equals(request.getRequestURI()) ? null : tokenResolver.resolve(request))
                        .jwt(jwt -> jwt.decoder(tokens.decoder()).jwtAuthenticationConverter(authorities))
                        .authenticationEntryPoint((request, response, e) -> {
                            log.info("401 {} {}: {}", request.getMethod(), request.getRequestURI(), e.getMessage());
                            bearer.commence(request, response, e);
                        }))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((request, response, ex) -> {
                            log.info("401 {} {}: no platform token", request.getMethod(), request.getRequestURI());
                            bearer.commence(request, response, ex);
                        })
                        .accessDeniedHandler((request, response, denied) -> {
                            log.warn("403 {} {}: {}", request.getMethod(), request.getRequestURI(), denied.getMessage());
                            ProblemResponses.write(response, HttpStatus.FORBIDDEN, "Platform administrators only");
                        }))
                .build();
    }
}
