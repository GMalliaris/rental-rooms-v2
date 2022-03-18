package org.gmalliaris.rental.rooms.dto;

import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

public class LoginRequest {

    @NotBlank(message = "Username is required and must not be blank")
    @Email(message = "Username must be a valid email address")
    private final String username;

    @NotBlank(message = "Password is required and must not be blank")
    private final String password;

    @JsonCreator
    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
