package org.gmalliaris.rental.rooms.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.gmalliaris.rental.rooms.*;
import org.gmalliaris.rental.rooms.config.exception.ApiExceptionMessageConstants;
import org.gmalliaris.rental.rooms.dto.CreateUserRequest;
import org.gmalliaris.rental.rooms.dto.JwtType;
import org.gmalliaris.rental.rooms.dto.LoginRequest;
import org.gmalliaris.rental.rooms.entity.UserRoleName;
import org.gmalliaris.rental.rooms.repository.AccountUserRepository;
import org.gmalliaris.rental.rooms.util.JwtUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.gmalliaris.rental.rooms.RequestUtils.Auth.*;
import static org.gmalliaris.rental.rooms.RequestUtils.Users.performMe;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AuthControllerIT implements PostgresTestContainer, MailHogTestContainer, RedisTestContainer {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountUserRepository accountUserRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Test
    void adminUserExistsAndCanLogin() throws Exception {
        var adminUsersCount = accountUserRepository.countAdminAccountUsers();
        assertEquals(1, adminUsersCount);

        var loginRequest = new LoginRequest("admin@example.eg",
                "12345678");

        var result = performLogin(mockMvc, objectMapper.writeValueAsString(loginRequest))
                .andExpect(status().isOk())
                .andReturn();

        var accessToken = JsonPath.read(result.getResponse().getContentAsString(), "$.accessToken");
        var refreshToken = JsonPath.read(result.getResponse().getContentAsString(), "$.refreshToken");
        assertNotNull(accessToken);
        assertNotNull(refreshToken);

        performMe(mockMvc, accessToken.toString())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("admin@example.eg"))
                .andExpect(jsonPath("$.firstName").value("Admin"))
                .andExpect(jsonPath("$.lastName").value("istrator"))
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.roles.length()").value(1))
                .andExpect(jsonPath("$.roles", Matchers.hasItem(UserRoleName.ROLE_ADMIN.toString())));

        RequestUtils.Auth.performRefreshTokens(mockMvc, refreshToken.toString())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").doesNotExist());
    }

    @Test
    void adminUserExistsAndLogoutAsExpected() throws Exception {
        var adminUsersCount = accountUserRepository.countAdminAccountUsers();
        assertEquals(1, adminUsersCount);

        var loginRequest = new LoginRequest("admin@example.eg",
                "12345678");

        var result = performLogin(mockMvc, objectMapper.writeValueAsString(loginRequest))
                .andExpect(status().isOk())
                .andReturn();

        var firstAccessToken = JsonPath.read(result.getResponse().getContentAsString(), "$.accessToken");
        var refreshToken = JsonPath.read(result.getResponse().getContentAsString(), "$.refreshToken");
        assertNotNull(firstAccessToken);
        assertNotNull(refreshToken);

        performMe(mockMvc, firstAccessToken.toString())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("admin@example.eg"))
                .andExpect(jsonPath("$.firstName").value("Admin"))
                .andExpect(jsonPath("$.lastName").value("istrator"))
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.roles.length()").value(1))
                .andExpect(jsonPath("$.roles", Matchers.hasItem(UserRoleName.ROLE_ADMIN.toString())));

        var refreshResult = performRefreshTokens(mockMvc, refreshToken.toString())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andReturn();
        var secondAccessToken = JsonPath.read(refreshResult.getResponse().getContentAsString(), "$.accessToken");
        performMe(mockMvc, secondAccessToken.toString())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("admin@example.eg"))
                .andExpect(jsonPath("$.firstName").value("Admin"))
                .andExpect(jsonPath("$.lastName").value("istrator"))
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.roles.length()").value(1))
                .andExpect(jsonPath("$.roles", Matchers.hasItem(UserRoleName.ROLE_ADMIN.toString())));

        performLogout(mockMvc, secondAccessToken.toString())
                .andExpect(status().isNoContent());

        performMe(mockMvc, firstAccessToken.toString())
                .andExpect(status().isUnauthorized());
        performMe(mockMvc, secondAccessToken.toString())
                .andExpect(status().isUnauthorized());
        performRefreshTokens(mockMvc, refreshToken.toString())
                .andExpect(status().isUnauthorized());
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
        performRegister(mockMvc, objectMapper.writeValueAsString(registerRequest))
                .andExpect(status().isCreated());

        var loginRequest = new LoginRequest(email, password);

        var result = performLogin(mockMvc, objectMapper.writeValueAsString(loginRequest))
                .andExpect(status().isOk())
                .andReturn();

        var accessToken = JsonPath.read(result.getResponse().getContentAsString(), "$.accessToken");
        var refreshToken = JsonPath.read(result.getResponse().getContentAsString(), "$.refreshToken");
        assertNotNull(accessToken);
        assertNotNull(refreshToken);

        performMe(mockMvc, accessToken.toString())
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
    void userRegistersAndCanLogin_validateTokenGroupId() throws Exception {
        var email = "testUser@example.eg";
        var password = "12345678aA!";
        var firstName = "first-name";
        var lastName = "last-name";
        var phoneNumber = "+30 6988888888";
        var rolesList = List.of(UserRoleName.ROLE_HOST, UserRoleName.ROLE_GUEST);

        var registerRequest = new CreateUserRequest(password, email, firstName, lastName, phoneNumber, rolesList);
        performRegister(mockMvc, objectMapper.writeValueAsString(registerRequest))
                .andExpect(status().isCreated());

        var loginRequest = new LoginRequest(email, password);

        var result = performLogin(mockMvc, objectMapper.writeValueAsString(loginRequest))
                .andExpect(status().isOk())
                .andReturn();

        var accessToken = (String) JsonPath.read(result.getResponse().getContentAsString(), "$.accessToken");
        var accClaims = JwtUtils.extractValidClaimsFromToken(accessToken, JwtType.ACCESS);
        assertNotNull(accClaims);
        assertTrue(accClaims.isPresent());
        var accTokenGroupId = JwtUtils.extractTokenGroupIdFromClaims(accClaims.get());
        var refreshToken = (String) JsonPath.read(result.getResponse().getContentAsString(), "$.refreshToken");
        var refClaims = JwtUtils.extractValidClaimsFromToken(refreshToken, JwtType.REFRESH);
        assertNotNull(refClaims);
        assertTrue(refClaims.isPresent());
        var refTokenGroupId = JwtUtils.extractTokenGroupIdFromClaims(refClaims.get());
        assertEquals(accTokenGroupId, refTokenGroupId);
        assertNotNull(accessToken);
        assertNotNull(refreshToken);

    }

    @Test
    void userRegistersAndCanLogin_throwsBecauseNotAccessToken() throws Exception {
        var email = "testUser@example.eg";
        var password = "12345678aA!";
        var firstName = "first-name";
        var lastName = "last-name";
        var phoneNumber = "+30 6988888888";
        var rolesList = List.of(UserRoleName.ROLE_HOST, UserRoleName.ROLE_GUEST);

        var registerRequest = new CreateUserRequest(password, email, firstName, lastName, phoneNumber, rolesList);
        performRegister(mockMvc, objectMapper.writeValueAsString(registerRequest))
                .andExpect(status().isCreated());

        var loginRequest = new LoginRequest(email, password);

        var result = performLogin(mockMvc, objectMapper.writeValueAsString(loginRequest))
                .andExpect(status().isOk())
                .andReturn();

        var accessToken = JsonPath.read(result.getResponse().getContentAsString(), "$.accessToken");
        var refreshToken = JsonPath.read(result.getResponse().getContentAsString(), "$.refreshToken");
        assertNotNull(accessToken);
        assertNotNull(refreshToken);

        performMe(mockMvc, refreshToken.toString())
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registerUserAndConfirm() throws Exception {
        var reqBody = new CreateUserRequest("12345678aA!", "malliaris@example.eg", "first",
                "lastName", "+30 6912345679", List.of(UserRoleName.ROLE_HOST));

        performRegister(mockMvc, objectMapper.writeValueAsString(reqBody))
                .andExpect(status().isCreated());

        var restTemplate = new RestTemplate();

        var response = restTemplate.exchange(getMailhogHttpUrl(), HttpMethod.GET,
                null, String.class);
        var jsonRoot = objectMapper.readTree(response.getBody());
        var mailBody = VerifyMailUtils.verifyMailAndExtractBody(jsonRoot,
                reqBody.getEmail(), "Registration Confirmation");
        var confirmationToken = VerifyMailUtils.extractTokenFromConfirmationToken(mailBody);

        var loginRequest = new LoginRequest(reqBody.getEmail(), reqBody.getPassword());
        var result = performLogin(mockMvc, objectMapper.writeValueAsString(loginRequest))
                .andExpect(status().isOk())
                .andReturn();
        var accessToken = JsonPath.read(result.getResponse().getContentAsString(), "$.accessToken");

        performMe(mockMvc, accessToken.toString())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(reqBody.getEmail()))
                .andExpect(jsonPath("$.firstName").value(reqBody.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(reqBody.getLastName()))
                .andExpect(jsonPath("$.enabled").value(false))
                .andExpect(jsonPath("$.roles.length()").value(1))
                .andExpect(jsonPath("$.roles", Matchers.hasItem(UserRoleName.ROLE_HOST.toString())));

        performConfirm(mockMvc, confirmationToken.toString())
                .andExpect(status().isCreated());

        performMe(mockMvc, accessToken.toString())
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

        performRegister(mockMvc, objectMapper.writeValueAsString(reqBody))
                .andExpect(status().isCreated());

        var restTemplate = new RestTemplate();

        var response = restTemplate.exchange(getMailhogHttpUrl(), HttpMethod.GET,
                null, String.class);
        var jsonRoot = objectMapper.readTree(response.getBody());
        var mailBody = VerifyMailUtils.verifyMailAndExtractBody(jsonRoot,
                reqBody.getEmail(), "Registration Confirmation");
        var oldConfirmationToken = VerifyMailUtils.extractTokenFromConfirmationToken(mailBody);

        var loginRequest = new LoginRequest(reqBody.getEmail(), reqBody.getPassword());
        var result = performLogin(mockMvc, objectMapper.writeValueAsString(loginRequest))
                .andExpect(status().isOk())
                .andReturn();
        var accessToken = JsonPath.read(result.getResponse().getContentAsString(), "$.accessToken");

        performMe(mockMvc, accessToken.toString())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(reqBody.getEmail()))
                .andExpect(jsonPath("$.firstName").value(reqBody.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(reqBody.getLastName()))
                .andExpect(jsonPath("$.enabled").value(false))
                .andExpect(jsonPath("$.roles.length()").value(1))
                .andExpect(jsonPath("$.roles", Matchers.hasItem(UserRoleName.ROLE_HOST.toString())));

        performResetConfirm(mockMvc, accessToken.toString())
                .andExpect(status().isCreated());

        response = restTemplate.exchange(getMailhogHttpUrl(), HttpMethod.GET,
                null, String.class);
        jsonRoot = objectMapper.readTree(response.getBody());
        mailBody = VerifyMailUtils.verifyMailAndExtractBody(jsonRoot,
                reqBody.getEmail(), "Registration Confirmation");
        var newConfirmationToken = VerifyMailUtils.extractTokenFromConfirmationToken(mailBody);

        performConfirm(mockMvc, oldConfirmationToken.toString())
                .andExpect(status().isNotFound());

        performConfirm(mockMvc, newConfirmationToken.toString())
                .andExpect(status().isCreated());

        performMe(mockMvc, accessToken.toString())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(reqBody.getEmail()))
                .andExpect(jsonPath("$.firstName").value(reqBody.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(reqBody.getLastName()))
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.roles.length()").value(1))
                .andExpect(jsonPath("$.roles", Matchers.hasItem(UserRoleName.ROLE_HOST.toString())));

        performConfirm(mockMvc, newConfirmationToken.toString())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value(ApiExceptionMessageConstants.CONFIRMATION_TOKEN_ALREADY_USED));
    }
}
