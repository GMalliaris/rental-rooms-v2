package org.gmalliaris.rental.rooms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig {

    private static final String[] PERMITTED_ENDPOINTS = new String[]{"/auth/register",
            "/auth/login", "/auth/confirm/**"};
    public static final String[] SWAGGER_ENDPOINTS = new String[]{ "/swagger-ui/**", "/v3/api-docs/**"};
    private final WebSecurityConfigService serviceConfig;
    private final CustomAuthenticationEntryPointConfig customAuthenticationEntryPointConfig;

    public WebSecurityConfig(WebSecurityConfigService serviceConfig, CustomAuthenticationEntryPointConfig customAuthenticationEntryPointConfig) {
        this.serviceConfig = serviceConfig;
        this.customAuthenticationEntryPointConfig = customAuthenticationEntryPointConfig;
    }

    @Bean
    protected SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeRequests()
                .antMatchers(PERMITTED_ENDPOINTS).permitAll()
                .antMatchers(SWAGGER_ENDPOINTS).permitAll()
                .anyRequest().authenticated()
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(customAuthenticationEntryPointConfig)
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        serviceConfig.configureHttpSecurityJwtFilter(http);

        return http.build();
    }

}
