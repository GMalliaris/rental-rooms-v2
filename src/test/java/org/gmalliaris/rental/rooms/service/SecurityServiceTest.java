package org.gmalliaris.rental.rooms.service;

import org.gmalliaris.rental.rooms.dto.AccountUserSecurityDetails;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityServiceTest {

    @InjectMocks
    private SecurityService securityService;

    @Test
    void getCurrentUserIdTest_principalNull(){

        var authentication = mock(Authentication.class);

        var context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(authentication);

        try (var securityUtils = mockStatic(SecurityContextHolder.class)){
            securityUtils.when(SecurityContextHolder::getContext)
                    .thenReturn(context);
            var exception = assertThrows(IllegalStateException.class,
                    () -> securityService.getCurrentUserId());

            assertEquals("Security principle of invalid type is null",
                    exception.getMessage());
        }
    }

    @Test
    void getCurrentUserIdTest_principalInvalid(){

        var principal = mock(UserDetails.class);

        var authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(principal);

        var context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(authentication);

        try (var securityUtils = mockStatic(SecurityContextHolder.class)){
            securityUtils.when(SecurityContextHolder::getContext)
                    .thenReturn(context);
            var exception = assertThrows(IllegalStateException.class,
                    () -> securityService.getCurrentUserId());

            var errMsg = String.format("Security principle of invalid type %s",
                    principal.getClass());
            assertEquals(errMsg, exception.getMessage());
        }
    }

    @Test
    void getCurrentUserIdTest(){

        var randomId = UUID.randomUUID();
        var principal = mock(AccountUserSecurityDetails.class);
        when(principal.getId()).thenReturn(randomId);

        var authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(principal);

        var context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(authentication);

        try (var securityUtils = mockStatic(SecurityContextHolder.class)){
            securityUtils.when(SecurityContextHolder::getContext)
                    .thenReturn(context);

            var id = securityService.getCurrentUserId();
            assertEquals(randomId, id);
        }
    }

    @Test
    void getCurrentUserEmailTest(){

        var randomEmail = "random@example.eg";
        var principal = mock(AccountUserSecurityDetails.class);
        when(principal.getUsername()).thenReturn(randomEmail);

        var authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(principal);

        var context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(authentication);

        try (var securityUtils = mockStatic(SecurityContextHolder.class)){
            securityUtils.when(SecurityContextHolder::getContext)
                    .thenReturn(context);

            var email = securityService.getCurrentUserEmail();
            assertEquals(randomEmail, email);
        }
    }
}