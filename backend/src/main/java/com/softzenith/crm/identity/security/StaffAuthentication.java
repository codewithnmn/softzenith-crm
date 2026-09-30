package com.softzenith.crm.identity.security;

import com.softzenith.crm.identity.StaffPrincipal;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.stream.Stream;

/**
 * Authentication for a staff request. Authorities are the role's permissions (e.g. {@code LEAD_ASSIGN})
 * plus {@code ROLE_<code>}, so endpoints use {@code @PreAuthorize("hasAuthority('LEAD_ASSIGN')")}.
 */
final class StaffAuthentication extends AbstractAuthenticationToken {

    private final StaffPrincipal staff;
    private final Jwt token;

    StaffAuthentication(StaffPrincipal staff, Jwt token) {
        super(authorities(staff));
        this.staff = staff;
        this.token = token;
        setAuthenticated(true);
    }

    private static List<GrantedAuthority> authorities(StaffPrincipal staff) {
        return Stream.concat(
                        staff.permissions().stream().map(Enum::name),
                        Stream.of("ROLE_" + staff.roleCode()))
                .<GrantedAuthority>map(SimpleGrantedAuthority::new)
                .toList();
    }

    @Override
    public StaffPrincipal getPrincipal() {
        return staff;
    }

    @Override
    public Jwt getCredentials() {
        return token;
    }

    @Override
    public String getName() {
        return staff.userId().toString();
    }
}
