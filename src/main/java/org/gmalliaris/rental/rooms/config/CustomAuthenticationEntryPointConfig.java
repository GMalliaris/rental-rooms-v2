package org.gmalliaris.rental.rooms.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.gmalliaris.rental.rooms.config.exception.ExceptionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CustomAuthenticationEntryPointConfig implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(CustomAuthenticationEntryPointConfig.class);

    private final ObjectMapper objectMapper;

    public CustomAuthenticationEntryPointConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {

        logger.debug(authException.getMessage(), authException);
        var message = new ExceptionResponse(HttpStatus.UNAUTHORIZED, "User is unauthorized.");
        var writer = response.getWriter();
        response.setContentType(MediaType.APPLICATION_JSON.toString());
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        writer.write(objectMapper.writeValueAsString(message));
        writer.flush();
        writer.close();

    }
}
