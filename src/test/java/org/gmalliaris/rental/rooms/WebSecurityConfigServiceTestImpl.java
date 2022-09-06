package org.gmalliaris.rental.rooms;

import org.gmalliaris.rental.rooms.config.WebSecurityConfigService;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

public class WebSecurityConfigServiceTestImpl implements WebSecurityConfigService {

    @Override
    public void configureHttpSecurityJwtFilter(HttpSecurity httpSecurity) {
        // Do nothing
    }

    @Override
    public void configureHttpSecurityAuthenticationProvider(HttpSecurity httpSecurity) {
        // Do nothing
    }
}
