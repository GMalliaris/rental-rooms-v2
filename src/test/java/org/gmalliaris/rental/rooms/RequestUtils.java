package org.gmalliaris.rental.rooms;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public final class RequestUtils {

    private RequestUtils(){
        // Hide implicit constructor
    }

    public static final class Auth {

        private Auth(){
            // Hide implicit constructor
        }

        public static ResultActions performRegister(MockMvc mockMvc, String jsonBody)
                throws Exception {

            return mockMvc.perform(post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody));
        }

        public static ResultActions performLogin(MockMvc mockMvc, String jsonBody)
                throws Exception {

            return mockMvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody));
        }

        public static ResultActions performRefreshTokens(MockMvc mockMvc, String refreshToken)
                throws Exception {

            return mockMvc.perform(get("/auth/refresh").
                    header("Authorization", String.format("Bearer %s", refreshToken)));
        }

        public static ResultActions performResetConfirm(MockMvc mockMvc, String accessToken)
                throws Exception {

            return mockMvc.perform(post("/auth/confirm-reset").
                    header("Authorization", String.format("Bearer %s", accessToken)));
        }

        public static ResultActions performConfirm(MockMvc mockMvc, String confirmationToken)
                throws Exception {

            var confirmUri = String.format("/auth/confirm/%s", confirmationToken);
            return mockMvc.perform(post(confirmUri));
        }

        public static ResultActions performLogout(MockMvc mockMvc, String accessToken)
                throws Exception {

            return mockMvc.perform(post("/auth/logout")
                    .header("Authorization", String.format("Bearer %s", accessToken)));
        }
    }

    public static final class Users{

        private Users(){
            // Hide implicit constructor
        }

        public static ResultActions performMe(MockMvc mockMvc, String accessToken)
                throws Exception {

            return mockMvc.perform(get("/users/me")
                    .header("Authorization", String.format("Bearer %s", accessToken)));
        }

    }
}
