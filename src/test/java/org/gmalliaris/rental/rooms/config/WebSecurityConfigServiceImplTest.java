package org.gmalliaris.rental.rooms.config;

import org.gmalliaris.rental.rooms.service.AccountUserSecurityService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
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
    void configureHttpSecurityTest() {
         var mockHttpSecurity = mock(HttpSecurity.class);

         webSecurityConfigService.configureHttpSecurityJwtFilter(mockHttpSecurity);
         verify(mockHttpSecurity).addFilterBefore(jwtAuthFilter,
                 UsernamePasswordAuthenticationFilter.class);
    }
}