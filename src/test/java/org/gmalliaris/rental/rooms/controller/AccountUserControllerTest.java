package org.gmalliaris.rental.rooms.controller;

import org.gmalliaris.rental.rooms.config.RentalRoomsConfig;
import org.gmalliaris.rental.rooms.entity.AccountUser;
import org.gmalliaris.rental.rooms.entity.UserRole;
import org.gmalliaris.rental.rooms.entity.UserRoleName;
import org.gmalliaris.rental.rooms.service.AccountUserService;
import org.gmalliaris.rental.rooms.service.SecurityService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountUserController.class)
@Import(RentalRoomsConfig.class)
@ActiveProfiles("test-security")
class AccountUserControllerTest {

    @MockBean
    private AccountUserService accountUserService;

    @MockBean
    private SecurityService securityService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void findCurrentUserTest_isForbidden() throws Exception {
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void findCurrentUserTest() throws Exception {
        var userId = UUID.randomUUID();
        when(securityService.getCurrentUserId())
                .thenReturn(userId);

        var user = new AccountUser();
        user.setId(userId);
        user.setEmail("user@example.eg");
        user.setFirstName("firstName");
        user.setLastName("lastName");
        user.setPhoneNumber("phoneNumber");
        user.setEnabled(true);
        for (UserRoleName value : UserRoleName.values()) {
            var role = new UserRole();
            role.setId(UUID.randomUUID());
            role.setName(value);
            user.addRole(role);
        }
        when(accountUserService.findAccountUserById(any(UUID.class)))
                .thenReturn(user);

        mockMvc.perform(get("/users/me"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(user.getEmail()))
                .andExpect(jsonPath("$.firstName").value(user.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(user.getLastName()))
                .andExpect(jsonPath("$.phoneNumber").value(user.getPhoneNumber()))
                .andExpect(jsonPath("$.enabled").value(user.isEnabled()))
                .andExpect(jsonPath("$.roles.length()").value(user.getRoles().size()))
                .andExpect(jsonPath("$.roles", Matchers.hasItem("ROLE_HOST")))
                .andExpect(jsonPath("$.roles", Matchers.hasItem("ROLE_GUEST")))
                .andExpect(jsonPath("$.roles", Matchers.hasItem("ROLE_ADMIN")));
        verify(accountUserService).findAccountUserById(userId);
    }
}