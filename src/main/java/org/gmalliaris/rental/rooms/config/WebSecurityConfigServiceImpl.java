package org.gmalliaris.rental.rooms.config;

import org.gmalliaris.rental.rooms.service.AccountUserSecurityService;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;

@Component
@Profile("!disable-jwt-auth")
public class WebSecurityConfigServiceImpl implements WebSecurityConfigService {

    private final JwtAuthFilter jwtAuthFilter;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final AccountUserSecurityService accountUserSecurityService;

    public WebSecurityConfigServiceImpl(JwtAuthFilter jwtAuthFilter, BCryptPasswordEncoder bCryptPasswordEncoder, AccountUserSecurityService accountUserSecurityService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.accountUserSecurityService = accountUserSecurityService;
    }

    @Override
    public void configureHttpSecurityJwtFilter(HttpSecurity httpSecurity) {
        httpSecurity.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
    }

    @Override
    public void configureHttpSecurityAuthProvider(HttpSecurity httpSecurity) {
        var authProvider = new DaoAuthenticationProvider();
        authProvider.setPasswordEncoder(bCryptPasswordEncoder);
        authProvider.setUserDetailsService(accountUserSecurityService);
        httpSecurity.authenticationProvider(authProvider);
    }
}
