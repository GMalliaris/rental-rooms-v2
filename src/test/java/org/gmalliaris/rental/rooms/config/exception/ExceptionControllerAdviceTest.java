package org.gmalliaris.rental.rooms.config.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.gmalliaris.rental.rooms.UnitTestConfig;
import org.gmalliaris.rental.rooms.controller.AuthController;
import org.gmalliaris.rental.rooms.dto.LoginRequest;
import org.gmalliaris.rental.rooms.service.AccountUserService;
import org.gmalliaris.rental.rooms.service.SecurityService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(UnitTestConfig.class)
@ActiveProfiles("test-security")
class ExceptionControllerAdviceTest {

    @MockBean
    private SecurityService securityService;

    @MockBean
    private AccountUserService accountUserService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    private final String email = "admin@example.eg";
    private final String password = "12345678";

    @Test
    void internalServerError() throws Exception {
        var body = new LoginRequest(email, password);

        when(accountUserService.login(any(LoginRequest.class)))
                .thenThrow(new NullPointerException("Null pointer"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.httpStatus").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.message").value("Null pointer"));
    }

    @Test
    void accessDeniedException() throws Exception {
        var body = new LoginRequest(email, password);

        when(accountUserService.login(any(LoginRequest.class)))
                .thenThrow(AccessDeniedException.class);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.httpStatus").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Access is denied."));
    }

    @Test
    void apiException() throws Exception {
        var body = new LoginRequest(email, password);

        when(accountUserService.login(any(LoginRequest.class)))
                .thenThrow(new ApiException(HttpStatus.NOT_FOUND, "Not found"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Not found"));
    }
}