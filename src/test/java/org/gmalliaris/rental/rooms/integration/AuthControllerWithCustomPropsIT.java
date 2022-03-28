package org.gmalliaris.rental.rooms.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.gmalliaris.rental.rooms.dto.LoginRequest;
import org.gmalliaris.rental.rooms.entity.UserRoleName;
import org.gmalliaris.rental.rooms.repository.AccountUserRepository;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@TestPropertySource(properties = {"default.admin.email=customAdmin@example.eg",
    "default.admin.password=1234",
    "jwt.refresh.expiration.minutes=2",
    "jwt.access.expiration.seconds=180"})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class AuthControllerWithCustomPropsIT {

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

        var loginRequest = new LoginRequest("customAdmin@example.eg",
                "1234");

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
                .andExpect(jsonPath("$.email").value("customAdmin@example.eg"))
                .andExpect(jsonPath("$.firstName").value("Admin"))
                .andExpect(jsonPath("$.lastName").value("istrator"))
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.roles.length()").value(1))
                .andExpect(jsonPath("$.roles", Matchers.hasItem(UserRoleName.ROLE_ADMIN.toString())));

        mockMvc.perform(get("/auth/refresh").
                        header("Authorization", String.format("Bearer %s", refreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }
}
