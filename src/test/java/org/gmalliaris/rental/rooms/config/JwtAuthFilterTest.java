package org.gmalliaris.rental.rooms.config;

import org.gmalliaris.rental.rooms.dto.JwtType;
import org.gmalliaris.rental.rooms.service.AccountUserSecurityService;
import org.gmalliaris.rental.rooms.util.JwtUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @InjectMocks
    private JwtAuthFilter filter;

    @Mock
    private AccountUserSecurityService accountUserSecurityService;

    @Test
    void doFilterInternalTest_emailNotFound() throws ServletException, IOException {
        var mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getRequestURI())
                .thenReturn("/irrelevant");
        when(mockRequest.getHeader(anyString()))
                .thenReturn(null);

        try(var jwtUtils = mockStatic(JwtUtils.class)){
            jwtUtils.when(() -> JwtUtils.extractUserEmailFromHeader(nullable(String.class), any(JwtType.class)))
                    .thenCallRealMethod();

            filter.doFilterInternal(mockRequest, mock(HttpServletResponse.class), mock(FilterChain.class));

            jwtUtils.verify(() -> JwtUtils.extractUserEmailFromHeader(null, JwtType.ACCESS));
            verify(accountUserSecurityService, never()).loadUserByUsername(anyString());
        }
    }

    @Test
    void doFilterInternalTest_userNotFound() throws ServletException, IOException {
        var mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getRequestURI())
                .thenReturn("/irrelevant");
        var token = "randomToken";
        when(mockRequest.getHeader(anyString()))
                .thenReturn(token);

        try(var jwtUtils = mockStatic(JwtUtils.class)){
            var email = "email@example.eg";
            jwtUtils.when(() -> JwtUtils.extractUserEmailFromHeader(nullable(String.class), any(JwtType.class)))
                    .thenReturn(Optional.of(email));
            when(accountUserSecurityService.loadUserByUsername(anyString()))
                    .thenReturn(null);

            filter.doFilterInternal(mockRequest, mock(HttpServletResponse.class), mock(FilterChain.class));

            jwtUtils.verify(() -> JwtUtils.extractUserEmailFromHeader(token, JwtType.ACCESS));
            verify(accountUserSecurityService).loadUserByUsername(email);
        }
    }

    @Test
    void doFilterInternalTest_withTypeAccess() throws ServletException, IOException {
        var mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getRequestURI())
                .thenReturn("/irrelevant");
        var token = "randomToken";
        when(mockRequest.getHeader(anyString()))
                .thenReturn(token);

        try(var jwtUtils = mockStatic(JwtUtils.class)){
            var email = "email@example.eg";
            jwtUtils.when(() -> JwtUtils.extractUserEmailFromHeader(nullable(String.class), any(JwtType.class)))
                    .thenReturn(Optional.of(email));

            var mockUserDetails = mock(UserDetails.class);
            when(mockUserDetails.getAuthorities())
                    .thenReturn(List.of());
            when(accountUserSecurityService.loadUserByUsername(anyString()))
                    .thenReturn(mockUserDetails);

            filter.doFilterInternal(mockRequest, mock(HttpServletResponse.class), mock(FilterChain.class));

            jwtUtils.verify(() -> JwtUtils.extractUserEmailFromHeader(token, JwtType.ACCESS));
            verify(accountUserSecurityService).loadUserByUsername(email);
        }
    }

    @Test
    void doFilterInternalTest_withTypeRefresh() throws ServletException, IOException {
        var refreshUri = (String) ReflectionTestUtils.getField(JwtAuthFilter.class,
                "REFRESH_URI");
        var mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getRequestURI())
                .thenReturn(refreshUri);
        var token = "randomToken";
        when(mockRequest.getHeader(anyString()))
                .thenReturn(token);

        try(var jwtUtils = mockStatic(JwtUtils.class)){
            var email = "email@example.eg";
            jwtUtils.when(() -> JwtUtils.extractUserEmailFromHeader(nullable(String.class), any(JwtType.class)))
                    .thenReturn(Optional.of(email));

            var mockUserDetails = mock(UserDetails.class);
            when(mockUserDetails.getAuthorities())
                    .thenReturn(List.of());
            when(accountUserSecurityService.loadUserByUsername(anyString()))
                    .thenReturn(mockUserDetails);

            filter.doFilterInternal(mockRequest, mock(HttpServletResponse.class), mock(FilterChain.class));

            jwtUtils.verify(() -> JwtUtils.extractUserEmailFromHeader(token, JwtType.REFRESH));
            verify(accountUserSecurityService).loadUserByUsername(email);
        }
    }
}