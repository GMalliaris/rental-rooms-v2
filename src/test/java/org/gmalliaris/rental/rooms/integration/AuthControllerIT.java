package org.gmalliaris.rental.rooms.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.gmalliaris.rental.rooms.VerifyMailUtils;
import org.gmalliaris.rental.rooms.config.exception.ApiExceptionMessageConstants;
import org.gmalliaris.rental.rooms.dto.CreateUserRequest;
import org.gmalliaris.rental.rooms.dto.LoginRequest;
import org.gmalliaris.rental.rooms.entity.UserRoleName;
import org.gmalliaris.rental.rooms.repository.AccountUserRepository;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class AuthControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountUserRepository accountUserRepository;

    @Test
    void adminUserExistsAndCanLogin() throws Exception {
        var adminUsersCount = accountUserRepository.countAdminAccountUsers();
        assertEquals(1, adminUsersCount);

        var loginRequest = new LoginRequest("admin@example.eg",
                "12345678");

        var result = mockMvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        var accessToken = JsonPath.read(result.getResponse().getContentAsString(), "$.accessToken");
        var refreshToken = JsonPath.read(result.getResponse().getContentAsString(), "$.refreshToken");
        assertNotNull(accessToken);
        assertNotNull(refreshToken);

        mockMvc.perform(get("/users/me").
                        header("Authorization", String.format("Bearer %s", accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("admin@example.eg"))
                .andExpect(jsonPath("$.firstName").value("Admin"))
                .andExpect(jsonPath("$.lastName").value("istrator"))
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.roles.length()").value(1))
                .andExpect(jsonPath("$.roles", Matchers.hasItem(UserRoleName.ROLE_ADMIN.toString())));

        mockMvc.perform(get("/auth/refresh").
                    header("Authorization", String.format("Bearer %s", refreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").doesNotExist());
    }

    @Test
    void userRegistersAndCanLogin() throws Exception {
        var email = "testUser@example.eg";
        var password = "12345678aA!";
        var firstName = "first-name";
        var lastName = "last-name";
        var phoneNumber = "+30 6988888888";
        var rolesList = List.of(UserRoleName.ROLE_HOST, UserRoleName.ROLE_GUEST);

        var registerRequest = new CreateUserRequest(password, email, firstName, lastName, phoneNumber, rolesList);
        mockMvc.perform(post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        var loginRequest = new LoginRequest(email, password);

        var result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        var accessToken = JsonPath.read(result.getResponse().getContentAsString(), "$.accessToken");
        var refreshToken = JsonPath.read(result.getResponse().getContentAsString(), "$.refreshToken");
        assertNotNull(accessToken);
        assertNotNull(refreshToken);

        mockMvc.perform(get("/users/me").
                        header("Authorization", String.format("Bearer %s", accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.firstName").value(firstName))
                .andExpect(jsonPath("$.lastName").value(lastName))
                .andExpect(jsonPath("$.enabled").value(false))
                .andExpect(jsonPath("$.roles.length()").value(2))
                .andExpect(jsonPath("$.roles", Matchers.hasItem(UserRoleName.ROLE_HOST.toString())))
                .andExpect(jsonPath("$.roles", Matchers.hasItem(UserRoleName.ROLE_GUEST.toString())));
    }

    @Test
    void registerUserAndConfirm() throws Exception {
        var reqBody = new CreateUserRequest("12345678aA!", "malliaris@example.eg", "first",
                "lastName", "+30 6912345679", List.of(UserRoleName.ROLE_HOST));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqBody)))
                .andExpect(status().isCreated());

        var restTemplate = new RestTemplate();

        var response = restTemplate.exchange("http://localhost:8025/api/v2/messages", HttpMethod.GET,
                null, String.class);
        var jsonRoot = objectMapper.readTree(response.getBody());
        var mailBody = VerifyMailUtils.verifyMailAndExtractBody(jsonRoot,
                reqBody.getEmail(), "Registration Confirmation");
        var confirmationToken = VerifyMailUtils.extractTokenFromConfirmationToken(mailBody);

        var loginRequest = new LoginRequest(reqBody.getEmail(), reqBody.getPassword());
        var result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();
        var accessToken = JsonPath.read(result.getResponse().getContentAsString(), "$.accessToken");

        mockMvc.perform(get("/users/me").
                        header("Authorization", String.format("Bearer %s", accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(reqBody.getEmail()))
                .andExpect(jsonPath("$.firstName").value(reqBody.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(reqBody.getLastName()))
                .andExpect(jsonPath("$.enabled").value(false))
                .andExpect(jsonPath("$.roles.length()").value(1))
                .andExpect(jsonPath("$.roles", Matchers.hasItem(UserRoleName.ROLE_HOST.toString())));

        var confirmUri = String.format("/auth/confirm/%s", confirmationToken);
        mockMvc.perform(post(confirmUri))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/users/me").
                        header("Authorization", String.format("Bearer %s", accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(reqBody.getEmail()))
                .andExpect(jsonPath("$.firstName").value(reqBody.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(reqBody.getLastName()))
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.roles.length()").value(1))
                .andExpect(jsonPath("$.roles", Matchers.hasItem(UserRoleName.ROLE_HOST.toString())));
    }

    @Test
    void registerUserAndResetAndConfirm() throws Exception {
        var reqBody = new CreateUserRequest("12345678aA!", "malliaris@example.eg", "first",
                "lastName", "+30 6912345679", List.of(UserRoleName.ROLE_HOST));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqBody)))
                .andExpect(status().isCreated());

        var restTemplate = new RestTemplate();

        var response = restTemplate.exchange("http://localhost:8025/api/v2/messages", HttpMethod.GET,
                null, String.class);
        var jsonRoot = objectMapper.readTree(response.getBody());
        var mailBody = VerifyMailUtils.verifyMailAndExtractBody(jsonRoot,
                reqBody.getEmail(), "Registration Confirmation");
        var oldConfirmationToken = VerifyMailUtils.extractTokenFromConfirmationToken(mailBody);

        var loginRequest = new LoginRequest(reqBody.getEmail(), reqBody.getPassword());
        var result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();
        var accessToken = JsonPath.read(result.getResponse().getContentAsString(), "$.accessToken");

        mockMvc.perform(get("/users/me").
                        header("Authorization", String.format("Bearer %s", accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(reqBody.getEmail()))
                .andExpect(jsonPath("$.firstName").value(reqBody.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(reqBody.getLastName()))
                .andExpect(jsonPath("$.enabled").value(false))
                .andExpect(jsonPath("$.roles.length()").value(1))
                .andExpect(jsonPath("$.roles", Matchers.hasItem(UserRoleName.ROLE_HOST.toString())));

        mockMvc.perform(post("/auth/confirm-reset").
                        header("Authorization", String.format("Bearer %s", accessToken)))
                .andExpect(status().isCreated());

        response = restTemplate.exchange("http://localhost:8025/api/v2/messages", HttpMethod.GET,
                null, String.class);
        jsonRoot = objectMapper.readTree(response.getBody());
        mailBody = VerifyMailUtils.verifyMailAndExtractBody(jsonRoot,
                reqBody.getEmail(), "Registration Confirmation");
        var newConfirmationToken = VerifyMailUtils.extractTokenFromConfirmationToken(mailBody);

        var confirmUri = String.format("/auth/confirm/%s", oldConfirmationToken);
        mockMvc.perform(post(confirmUri))
                .andExpect(status().isNotFound());

        confirmUri = String.format("/auth/confirm/%s", newConfirmationToken);
        mockMvc.perform(post(confirmUri))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/users/me").
                        header("Authorization", String.format("Bearer %s", accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(reqBody.getEmail()))
                .andExpect(jsonPath("$.firstName").value(reqBody.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(reqBody.getLastName()))
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.roles.length()").value(1))
                .andExpect(jsonPath("$.roles", Matchers.hasItem(UserRoleName.ROLE_HOST.toString())));

        mockMvc.perform(post(confirmUri))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value(ApiExceptionMessageConstants.CONFIRMATION_TOKEN_ALREADY_USED));
    }
}
