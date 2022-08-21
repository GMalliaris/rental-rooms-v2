package org.gmalliaris.rental.rooms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.gmalliaris.rental.rooms.entity.UserRoleName;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Schema
public class CurrentAccountUserResponse {
    @NotBlank
    private String email;
    @NotBlank
    private String firstName;
    private String lastName;
    private String phoneNumber;
    @NotBlank
    private boolean enabled;
    @NotEmpty
    @Size(min = 1, max = 2)
    private List<UserRoleName> roles = new ArrayList<>();

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<UserRoleName> getRoles() {
        return roles;
    }

    public void setRoles(List<UserRoleName> roles) {
        this.roles = roles;
    }
}
