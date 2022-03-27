package org.gmalliaris.rental.rooms.config;

import org.gmalliaris.rental.rooms.service.AccountUserSecurityService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.userdetails.DaoAuthenticationConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebSecurityConfigServiceImplTest {

    @InjectMocks
    private WebSecurityConfigServiceImpl webSecurityConfigService;

    @Mock
    private JwtAuthFilter jwtAuthFilter;

    @Mock
    private AccountUserSecurityService accountUserSecurityService;

    @Test
    void configureTest() throws Exception {
        var mockDaoAuth = mock(DaoAuthenticationConfigurer.class);

        var mockAuth = mock(AuthenticationManagerBuilder.class);
        when(mockAuth.userDetailsService(any(UserDetailsService.class)))
                .thenReturn(mockDaoAuth);

        var mockBCrypt = mock(BCryptPasswordEncoder.class);

        webSecurityConfigService.configure(mockAuth, mockBCrypt);
        verify(mockAuth).userDetailsService(accountUserSecurityService);
        verify(mockDaoAuth).passwordEncoder(mockBCrypt);
    }

    @Test
    void configureHttpSecurityTest() {
         var mockHttpSecurity = mock(HttpSecurity.class);

         webSecurityConfigService.configure(mockHttpSecurity);
         verify(mockHttpSecurity).addFilterBefore(jwtAuthFilter,
                 UsernamePasswordAuthenticationFilter.class);
    }
}