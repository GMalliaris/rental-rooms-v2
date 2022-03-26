package org.gmalliaris.rental.rooms.dto;

import org.gmalliaris.rental.rooms.entity.UserRoleName;

import java.util.ArrayList;
import java.util.List;

public class CurrentAccountUserResponse {
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private boolean enabled;
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
