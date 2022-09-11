package org.gmalliaris.rental.rooms.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;


@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig {

    private static final String[] PERMITTED_ENDPOINTS = new String[]{"/auth/register",
            "/auth/login", "/auth/confirm/**"};
    private static final String[] SWAGGER_ENDPOINTS = new String[]{ "/swagger-ui/**", "/v3/api-docs/**"};

    @Value("${cors.allowedOrigins:}")
    private List<String> allowedOrigins;

    private final WebSecurityConfigService serviceConfig;
    private final CustomAuthenticationEntryPointConfig customAuthenticationEntryPointConfig;

    public WebSecurityConfig(WebSecurityConfigService serviceConfig, CustomAuthenticationEntryPointConfig customAuthenticationEntryPointConfig) {
        this.serviceConfig = serviceConfig;
        this.customAuthenticationEntryPointConfig = customAuthenticationEntryPointConfig;
    }

    @Bean
    protected SecurityFilterChain securityFilterChain(HttpSecurity http, CorsConfiguration corsConfiguration) throws Exception {
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

        http.cors().configurationSource(request -> corsConfiguration);

        serviceConfig.configureHttpSecurityJwtFilter(http);

        return http.build();
    }

    @Bean
    protected CorsConfiguration corsConfiguration() {
        var corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(allowedOrigins);
        corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE"));
        corsConfiguration.setAllowCredentials(false);
        corsConfiguration.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type"));

        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return corsConfiguration;
    }

}
