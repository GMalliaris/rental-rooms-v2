package org.gmalliaris.rental.rooms.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;

@Schema
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountUserAuthResponse {

    @NotBlank
    private final String accessToken;
    @NotBlank
    private final String refreshToken;

    public AccountUserAuthResponse(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}
