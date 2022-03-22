package org.gmalliaris.rental.rooms.dto;

import org.gmalliaris.rental.rooms.entity.UserRoleName;
import org.gmalliaris.rental.rooms.util.PhoneNumber;
import org.gmalliaris.rental.rooms.util.StrongPassword;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

public class CreateUserRequest {

    @NotBlank(message = "Password is required and must not be blank")
    @StrongPassword(message = "Password must contain at least one upper-case letter, one lower-case letter, one digit, one special character and must be at least 10 characters long")
    private final String password;

    @NotNull(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    private final String email;

    @NotBlank(message = "First name is required and must not be blank")
    private final String firstName;

    private final String lastName;

    @PhoneNumber(message = "Phone number must be a valid phone number with country prefix")
    private final String phoneNumber;

    @NotNull(message = "Roles list is required")
    @Size(min = 1, max = 2, message = "Roles list must contain either one or two roles")
    private final List<@NotNull UserRoleName> roles;

    public CreateUserRequest(String password, String email, String firstName, String lastName, String phoneNumber, List<UserRoleName> roles) {
        this.password = password;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.roles = roles;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public List<UserRoleName> getRoles() {
        return roles;
    }
}
