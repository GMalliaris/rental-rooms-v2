package org.gmalliaris.rental.rooms.config;

import org.gmalliaris.rental.rooms.service.AccountUserSecurityService;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;

@Component
@Profile("!test-security")
public class WebSecurityConfigServiceImpl implements WebSecurityConfigService {

    private final JwtAuthFilter jwtAuthFilter;
    private final AccountUserSecurityService accountUserSecurityService;

    public WebSecurityConfigServiceImpl(JwtAuthFilter jwtAuthFilter, AccountUserSecurityService accountUserSecurityService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.accountUserSecurityService = accountUserSecurityService;
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth,
                          BCryptPasswordEncoder bCryptPasswordEncoder) throws Exception {
        auth.userDetailsService(accountUserSecurityService)
                .passwordEncoder(bCryptPasswordEncoder);
    }

    @Override
    public void configure(HttpSecurity http) {
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
    }
}
