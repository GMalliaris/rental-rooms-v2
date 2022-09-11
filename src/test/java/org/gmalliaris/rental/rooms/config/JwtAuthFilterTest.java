package org.gmalliaris.rental.rooms.config;

import io.jsonwebtoken.Claims;
import org.gmalliaris.rental.rooms.dto.JwtType;
import org.gmalliaris.rental.rooms.service.AccountUserSecurityService;
import org.gmalliaris.rental.rooms.service.BlacklistService;
import org.gmalliaris.rental.rooms.util.JwtUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @InjectMocks
    private JwtAuthFilter filter;

    @Mock
    private AccountUserSecurityService accountUserSecurityService;
    @Mock
    private BlacklistService blacklistService;

    @Test
    void doFilterInternalTest_emptyClaims() throws ServletException, IOException {
        var mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getRequestURI())
                .thenReturn("/irrelevant");
        when(mockRequest.getHeader(anyString()))
                .thenReturn(null);

        try(var jwtUtils = mockStatic(JwtUtils.class);
            var ctxUtils = mockStatic(SecurityContextHolder.class)){
            var ctx = mock(SecurityContext.class);
            ctxUtils.when(SecurityContextHolder::getContext)
                            .thenReturn(ctx);
            jwtUtils.when(() -> JwtUtils.extractValidClaimsFromHeader(nullable(String.class), any(JwtType.class)))
                    .thenCallRealMethod();

            filter.doFilterInternal(mockRequest, mock(HttpServletResponse.class), mock(FilterChain.class));

            jwtUtils.verify(() -> JwtUtils.extractValidClaimsFromHeader(null, JwtType.ACCESS));
            jwtUtils.verifyNoMoreInteractions();
            verifyNoInteractions(accountUserSecurityService);
            verifyNoInteractions(ctx);
        }
    }

    @Test
    void doFilterInternalTest_tokenIsBlacklisted() throws ServletException, IOException {
        var mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getRequestURI())
                .thenReturn("/irrelevant");
        when(mockRequest.getHeader(anyString()))
                .thenReturn(null);


        try(var jwtUtils = mockStatic(JwtUtils.class);
            var ctxUtils = mockStatic(SecurityContextHolder.class)){
            var ctx = mock(SecurityContext.class);
            ctxUtils.when(SecurityContextHolder::getContext)
                    .thenReturn(ctx);
            var mockClaims = mock(Claims.class);
            jwtUtils.when(() -> JwtUtils.extractValidClaimsFromHeader(nullable(String.class), any(JwtType.class)))
                    .thenReturn(Optional.of(mockClaims));
            when(blacklistService.tokenWithClaimsIsBlackListed(any(Claims.class)))
                    .thenReturn(true);

            filter.doFilterInternal(mockRequest, mock(HttpServletResponse.class), mock(FilterChain.class));

            jwtUtils.verify(() -> JwtUtils.extractValidClaimsFromHeader(null, JwtType.ACCESS));
            verify(blacklistService).tokenWithClaimsIsBlackListed(mockClaims);
            jwtUtils.verify(() -> JwtUtils.extractUserIdFromValidClaims(mockClaims, JwtType.ACCESS), never());
            verifyNoInteractions(accountUserSecurityService);
            verifyNoInteractions(ctx);
        }
    }

    @Test
    void doFilterInternalTest_emptyUserId() throws ServletException, IOException {
        var mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getRequestURI())
                .thenReturn("/irrelevant");
        when(mockRequest.getHeader(anyString()))
                .thenReturn(null);


        try(var jwtUtils = mockStatic(JwtUtils.class);
            var ctxUtils = mockStatic(SecurityContextHolder.class)){
            var ctx = mock(SecurityContext.class);
            ctxUtils.when(SecurityContextHolder::getContext)
                    .thenReturn(ctx);
            var mockClaims = mock(Claims.class);
            jwtUtils.when(() -> JwtUtils.extractValidClaimsFromHeader(nullable(String.class), any(JwtType.class)))
                    .thenReturn(Optional.of(mockClaims));
            when(blacklistService.tokenWithClaimsIsBlackListed(any(Claims.class)))
                    .thenReturn(false);
            jwtUtils.when(() -> JwtUtils.extractUserIdFromValidClaims(any(Claims.class), any(JwtType.class)))
                            .thenReturn(null);

            filter.doFilterInternal(mockRequest, mock(HttpServletResponse.class), mock(FilterChain.class));

            jwtUtils.verify(() -> JwtUtils.extractValidClaimsFromHeader(null, JwtType.ACCESS));
            verify(blacklistService).tokenWithClaimsIsBlackListed(mockClaims);
            jwtUtils.verify(() -> JwtUtils.extractUserIdFromValidClaims(mockClaims, JwtType.ACCESS));
            verifyNoInteractions(accountUserSecurityService);
            verifyNoInteractions(ctx);
        }
    }

    @Test
    void doFilterInternalTest_userNotFound() throws ServletException, IOException {
        var mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getRequestURI())
                .thenReturn("/irrelevant");
        var header = "randomToken";
        when(mockRequest.getHeader(anyString()))
                .thenReturn(header);

        try (var jwtUtils = mockStatic(JwtUtils.class);
            var ctxUtils = mockStatic(SecurityContextHolder.class)){

            var ctx = mock(SecurityContext.class);
            ctxUtils.when(SecurityContextHolder::getContext)
                    .thenReturn(ctx);
            var userId = UUID.randomUUID();
            var mockClaims = mock(Claims.class);
            jwtUtils.when(() -> JwtUtils.extractValidClaimsFromHeader(nullable(String.class), any(JwtType.class)))
                    .thenReturn(Optional.of(mockClaims));
            when(blacklistService.tokenWithClaimsIsBlackListed(any(Claims.class)))
                    .thenReturn(false);
            jwtUtils.when(() -> JwtUtils.extractUserIdFromValidClaims(any(Claims.class), any(JwtType.class)))
                    .thenReturn(userId);
            when(accountUserSecurityService.loadUserById(any(UUID.class)))
                    .thenReturn(null);

            filter.doFilterInternal(mockRequest, mock(HttpServletResponse.class), mock(FilterChain.class));

            jwtUtils.verify(() -> JwtUtils.extractValidClaimsFromHeader(header, JwtType.ACCESS));
            verify(blacklistService).tokenWithClaimsIsBlackListed(mockClaims);
            jwtUtils.verify(() -> JwtUtils.extractUserIdFromValidClaims(mockClaims, JwtType.ACCESS));
            verify(accountUserSecurityService).loadUserById(userId);
            verifyNoInteractions(ctx);
        }
    }

    @Test
    void doFilterInternalTest_withTypeAccess() throws ServletException, IOException {
        var mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getRequestURI())
                .thenReturn("/irrelevant");
        var header = "randomHeader";
        when(mockRequest.getHeader(anyString()))
                .thenReturn(header);

        try (var jwtUtils = mockStatic(JwtUtils.class);
             var ctxUtils = mockStatic(SecurityContextHolder.class)){

            var ctx = mock(SecurityContext.class);
            ctxUtils.when(SecurityContextHolder::getContext)
                    .thenReturn(ctx);
            var userId = UUID.randomUUID();
            var mockClaims = mock(Claims.class);
            jwtUtils.when(() -> JwtUtils.extractValidClaimsFromHeader(nullable(String.class), any(JwtType.class)))
                    .thenReturn(Optional.of(mockClaims));
            when(blacklistService.tokenWithClaimsIsBlackListed(any(Claims.class)))
                    .thenReturn(false);
            jwtUtils.when(() -> JwtUtils.extractUserIdFromValidClaims(any(Claims.class), any(JwtType.class)))
                    .thenReturn(userId);

            var mockUserDetails = mock(UserDetails.class);
            when(mockUserDetails.getAuthorities())
                    .thenReturn(List.of());
            when(accountUserSecurityService.loadUserById(any(UUID.class)))
                    .thenReturn(mockUserDetails);

            filter.doFilterInternal(mockRequest, mock(HttpServletResponse.class), mock(FilterChain.class));

            jwtUtils.verify(() -> JwtUtils.extractValidClaimsFromHeader(header, JwtType.ACCESS));
            verify(blacklistService).tokenWithClaimsIsBlackListed(mockClaims);
            jwtUtils.verify(() -> JwtUtils.extractUserIdFromValidClaims(mockClaims, JwtType.ACCESS));
            verify(accountUserSecurityService).loadUserById(userId);
            verify(ctx).setAuthentication(any());
        }
    }

    @Test
    void doFilterInternalTest_withTypeRefresh() throws ServletException, IOException {
        var refreshUri = (String) ReflectionTestUtils.getField(JwtAuthFilter.class,
                "REFRESH_URI");
        var mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getRequestURI())
                .thenReturn(refreshUri);
        var header = "randomHeader";
        when(mockRequest.getHeader(anyString()))
                .thenReturn(header);

        try (var jwtUtils = mockStatic(JwtUtils.class);
             var ctxUtils = mockStatic(SecurityContextHolder.class)){

            var ctx = mock(SecurityContext.class);
            ctxUtils.when(SecurityContextHolder::getContext)
                    .thenReturn(ctx);
            var userId = UUID.randomUUID();
            var mockClaims = mock(Claims.class);
            jwtUtils.when(() -> JwtUtils.extractValidClaimsFromHeader(nullable(String.class), any(JwtType.class)))
                    .thenReturn(Optional.of(mockClaims));
            when(blacklistService.tokenWithClaimsIsBlackListed(any(Claims.class)))
                    .thenReturn(false);
            jwtUtils.when(() -> JwtUtils.extractUserIdFromValidClaims(any(Claims.class), any(JwtType.class)))
                    .thenReturn(userId);

            var mockUserDetails = mock(UserDetails.class);
            when(mockUserDetails.getAuthorities())
                    .thenReturn(List.of());
            when(accountUserSecurityService.loadUserById(any(UUID.class)))
                    .thenReturn(mockUserDetails);

            filter.doFilterInternal(mockRequest, mock(HttpServletResponse.class), mock(FilterChain.class));

            jwtUtils.verify(() -> JwtUtils.extractValidClaimsFromHeader(header, JwtType.REFRESH));
            verify(blacklistService).tokenWithClaimsIsBlackListed(mockClaims);
            jwtUtils.verify(() -> JwtUtils.extractUserIdFromValidClaims(mockClaims, JwtType.REFRESH));
            verify(accountUserSecurityService).loadUserById(userId);
            verify(ctx).setAuthentication(any());
        }
    }
}