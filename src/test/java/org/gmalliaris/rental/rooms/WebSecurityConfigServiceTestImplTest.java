package org.gmalliaris.rental.rooms;

import org.junit.jupiter.api.Test;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class WebSecurityConfigServiceTestImplTest {

    private final WebSecurityConfigServiceTestImpl service = new WebSecurityConfigServiceTestImpl();

    @Test
    void configureTest(){

        var mockAuth = mock(AuthenticationManagerBuilder.class);

        var mockBCrypt = mock(BCryptPasswordEncoder.class);

        service.configure(mockAuth, mockBCrypt);
        verifyNoInteractions(mockAuth, mockBCrypt);
    }

    @Test
    void configureHttpSecurityTest() {
        var mockHttpSecurity = mock(HttpSecurity.class);

        service.configure(mockHttpSecurity);
        verifyNoInteractions(mockHttpSecurity);
    }
}