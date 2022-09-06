package org.gmalliaris.rental.rooms.config;

import org.gmalliaris.rental.rooms.service.AccountUserSecurityService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WebSecurityConfigServiceImplTest {

    @InjectMocks
    private WebSecurityConfigServiceImpl webSecurityConfigService;

    @Mock
    private JwtAuthFilter jwtAuthFilter;

    @Mock
    private AccountUserSecurityService accountUserSecurityService;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Test
    void configureHttpSecurityJwtFilterTest() {
         var mockHttpSecurity = mock(HttpSecurity.class);

         webSecurityConfigService.configureHttpSecurityJwtFilter(mockHttpSecurity);
         verify(mockHttpSecurity).addFilterBefore(jwtAuthFilter,
                 UsernamePasswordAuthenticationFilter.class);
    }

    @Test
    void configureHttpSecurityAuthProviderTest() {
        var mockHttpSecurity = mock(HttpSecurity.class);

        webSecurityConfigService.configureHttpSecurityAuthenticationProvider(mockHttpSecurity);
        var authProviderCaptor = ArgumentCaptor.forClass(AuthenticationProvider.class);

        verify(mockHttpSecurity).authenticationProvider(authProviderCaptor.capture());
        var authProvider = authProviderCaptor.getValue();
        assertNotNull(authProvider);
        assertEquals(DaoAuthenticationProvider.class, authProvider.getClass());
        assertEquals(bCryptPasswordEncoder,
                ReflectionTestUtils.invokeGetterMethod(authProvider, "getPasswordEncoder"));
        assertEquals(accountUserSecurityService,
                ReflectionTestUtils.invokeGetterMethod(authProvider, "getUserDetailsService"));
    }
}