package org.gmalliaris.rental.rooms.config;

import org.gmalliaris.rental.rooms.dto.JwtType;
import org.gmalliaris.rental.rooms.service.AccountUserSecurityService;
import org.gmalliaris.rental.rooms.util.JwtUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
// Else you'd have to use @MockBean for AccountUserSecurityService
// on every WebMvcTest
@Profile("!disable-jwt-auth")
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String BEARER_HEADER = "Authorization";
    private static final String REFRESH_URI = "/auth/refresh";

    private final AccountUserSecurityService accountUserSecurityService;

    public JwtAuthFilter(AccountUserSecurityService accountUserSecurityService) {
        this.accountUserSecurityService = accountUserSecurityService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        var requestURI = request.getRequestURI();
        var type = REFRESH_URI.equals(requestURI)
                ? JwtType.REFRESH : JwtType.ACCESS;

        var header = request.getHeader(BEARER_HEADER);
        var userIdOptional = JwtUtils.extractUserIdFromHeader(header, type);
        if (userIdOptional.isPresent()){
            var userDetails = accountUserSecurityService.loadUserById(userIdOptional.get());
            if (userDetails != null){
                var userPwdAuth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(userPwdAuth);
            }
        }

        filterChain.doFilter(request, response);
    }
}
