package org.gmalliaris.rental.rooms.config;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

public interface WebSecurityConfigService {

    void configureHttpSecurityJwtFilter(HttpSecurity httpSecurity);
    void configureHttpSecurityAuthProvider(HttpSecurity httpSecurity);
}
