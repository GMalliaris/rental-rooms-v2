package org.gmalliaris.rental.rooms.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.gmalliaris.rental.rooms.MailHogTestContainer;
import org.gmalliaris.rental.rooms.PostgresTestContainer;
import org.gmalliaris.rental.rooms.RedisTestContainer;
import org.gmalliaris.rental.rooms.dto.LoginRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@TestPropertySource(properties = "cors.allowedOrigins=http://localhost:3000,www.example.com")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CorsIT  implements PostgresTestContainer, MailHogTestContainer, RedisTestContainer {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void corsTest() throws Exception {


        var loginRequest = new LoginRequest("admin@example.eg",
                "12345678");

//        no cors
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());
//        cors allowed
        mockMvc.perform(post("/auth/login")
                        .header("Origin", "http://localhost:3000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());
//        cors allowed
        mockMvc.perform(post("/auth/login")
                        .header("Origin", "www.example.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());
//        cors not allowed
        mockMvc.perform(post("/auth/login")
                        .header("Origin", "http://localhost:3001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isForbidden());
    }
}
