package org.gmalliaris.rental.rooms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.gmalliaris.rental.rooms.UnitTestConfig;
import org.gmalliaris.rental.rooms.dto.AccountUserAuthResponse;
import org.gmalliaris.rental.rooms.dto.CreateUserRequest;
import org.gmalliaris.rental.rooms.dto.LoginRequest;
import org.gmalliaris.rental.rooms.entity.UserRoleName;
import org.gmalliaris.rental.rooms.service.AccountUserService;
import org.gmalliaris.rental.rooms.service.SecurityService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(UnitTestConfig.class)
@ActiveProfiles("test-security")
class AuthControllerTest {

    @MockBean
    private SecurityService securityService;

    @MockBean
    private AccountUserService accountUserService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registerAccountUserTest_requiredValidationErrors() throws Exception {
        var body = new CreateUserRequest(null, null, null,
                "lastName", null, null);

        mockMvc.perform(post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.messages.length()").value(4))
                .andExpect(jsonPath("$.messages.email")
                        .value("Email is required"))
                .andExpect(jsonPath("$.messages.firstName")
                        .value("First name is required and must not be blank"))
                .andExpect(jsonPath("$.messages.password")
                        .value("Password is required and must not be blank"))
                .andExpect(jsonPath("$.messages.roles")
                        .value("Roles list is required"));
    }

    @Test
    void registerAccountUserTest_invalidValidationErrors() throws Exception {
        var body = new CreateUserRequest("12345678", "mal", "first",
                "lastName", "6912345678", List.of());

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.messages.length()").value(4))
                .andExpect(jsonPath("$.messages.email")
                        .value("Email must be a valid email address"))
                .andExpect(jsonPath("$.messages.phoneNumber")
                        .value("Phone number must be a valid phone number with country prefix"))
                .andExpect(jsonPath("$.messages.roles")
                        .value("Roles list must contain either one or two roles"))
                .andExpect(jsonPath("$.messages.password")
                        .value("Password must contain at least one upper-case letter, one lower-case letter, one digit, one special character and must be at least 10 characters long"));
    }

    @Test
    void registerAccountUserTest() throws Exception {
        var body = new CreateUserRequest("12345678aA!", "mal@example.eg", "first",
                "lastName", "+30 6912345678", List.of(UserRoleName.ROLE_HOST));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated());

        var argCaptor = ArgumentCaptor.forClass(CreateUserRequest.class);
        verify(accountUserService).createAccountUser(argCaptor.capture());

        var request = argCaptor.getValue();
        assertEquals(body.getEmail(), request.getEmail());
        assertEquals(body.getPassword(), request.getPassword());
        assertEquals(body.getFirstName(), request.getFirstName());
        assertEquals(body.getLastName(), request.getLastName());
        assertEquals(body.getPhoneNumber(), request.getPhoneNumber());
        assertEquals(body.getRoles(), request.getRoles());
    }

    @Test
    void loginTest_requiredValidationErrors() throws Exception {
        var body = new LoginRequest(null, " ");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.messages.length()").value(2))
                .andExpect(jsonPath("$.messages.username")
                        .value("Username is required"))
                .andExpect(jsonPath("$.messages.password")
                        .value("Password is required and must not be blank"));
    }

    @Test
    void loginTest_invalidValidationErrors() throws Exception {
        var body = new LoginRequest("test", "12345678");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.messages.length()").value(1))
                .andExpect(jsonPath("$.messages.username")
                        .value("Username must be a valid email address"));
    }

    @Test
    void loginTest() throws Exception {
        var body = new LoginRequest("test@example.eg", "12345678");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());

        var argCaptor = ArgumentCaptor.forClass(LoginRequest.class);
        verify(accountUserService).login(argCaptor.capture());

        var request = argCaptor.getValue();
        assertEquals(body.getUsername(), request.getUsername());
        assertEquals(body.getPassword(), request.getPassword());
    }

    @Test
    void refreshAuthTokens_isForbidden() throws Exception {
        mockMvc.perform(get("/auth/refresh"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void refreshAuthTokens() throws Exception {
        var randomId = UUID.randomUUID();
        when(securityService.getCurrentUserId()).thenReturn(randomId);

        var accessToken = "access";
        var refreshToken = "refresh";
        var authHeader = "Authorization";
        var authHeaderValue = "value";

        var authResponse = new AccountUserAuthResponse(accessToken, refreshToken);
        when(accountUserService.refreshAuthTokens(any(UUID.class), anyString()))
                .thenReturn(authResponse);

        mockMvc.perform(get("/auth/refresh")
                        .header(authHeader, authHeaderValue))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(accessToken))
                .andExpect(jsonPath("$.refreshToken").value(refreshToken));

        verify(securityService).getCurrentUserId();
        verify(accountUserService).refreshAuthTokens(randomId, authHeaderValue);
    }

    @Test
    void confirmAccountUserRegistrationTest() throws Exception {

        var randomUUID = UUID.randomUUID();
        var uri = String.format("/auth/confirm/%s", randomUUID);
        mockMvc.perform(post(uri))
                .andExpect(status().isCreated());

        verify(accountUserService).confirmAccountUserRegistration(randomUUID);
    }

    @Test
    void resetConfirmationProcessTest_isForbidden() throws Exception {

        mockMvc.perform(post("/auth/confirm-reset"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void resetConfirmationProcessTest() throws Exception {

        var userId = UUID.randomUUID();
        when(securityService.getCurrentUserId())
                .thenReturn(userId);

        mockMvc.perform(post("/auth/confirm-reset"))
                .andExpect(status().isCreated());

        verify(securityService).getCurrentUserId();
        verify(accountUserService).resetConfirmationProcess(userId);
    }
}