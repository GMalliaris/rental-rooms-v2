package org.gmalliaris.rental.rooms;

import org.junit.jupiter.api.Test;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class WebSecurityConfigServiceTestImplTest {

    private final WebSecurityConfigServiceTestImpl service = new WebSecurityConfigServiceTestImpl();

    @Test
    void configureHttpSecurityTest() {
        var mockHttpSecurity = mock(HttpSecurity.class);

        service.configureHttpSecurityJwtFilter(mockHttpSecurity);
        verifyNoInteractions(mockHttpSecurity);
    }
}