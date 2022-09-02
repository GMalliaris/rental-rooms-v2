package org.gmalliaris.rental.rooms.controller;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.gmalliaris.rental.rooms.config.exception.ExceptionResponse;
import org.gmalliaris.rental.rooms.dto.AccountUserAuthResponse;
import org.gmalliaris.rental.rooms.dto.CreateUserRequest;
import org.gmalliaris.rental.rooms.dto.LoginRequest;
import org.gmalliaris.rental.rooms.service.AccountUserService;
import org.gmalliaris.rental.rooms.service.SecurityService;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AccountUserService accountUserService;
    private final SecurityService securityService;

    public AuthController(AccountUserService accountUserService, SecurityService securityService) {
        this.accountUserService = accountUserService;
        this.securityService = securityService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    @Operation(summary = "Register new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",
                    description = "New user registered",
                    content = { @Content(schema = @Schema) }),
            @ApiResponse(responseCode = "400",
                    description = "Invalid user information",
                    content = { @Content(schema = @Schema(implementation = ExceptionResponse.class)) }),
            @ApiResponse(responseCode = "409",
                    description = "Invalid user email/phone number",
                    content = { @Content(schema = @Schema(implementation = ExceptionResponse.class)) })
    })
    public void registerAccountUser(@RequestBody @Valid CreateUserRequest createUserRequest){
        accountUserService.createAccountUser(createUserRequest);
    }

    @PostMapping("/login")
    @Transactional(readOnly = true)
    @Operation(summary = "Login as user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "User logged in",
                    content = { @Content(schema = @Schema(implementation = AccountUserAuthResponse.class)) }),
            @ApiResponse(responseCode = "401",
                    description = "Invalid user credentials",
                    content = { @Content(schema = @Schema(implementation = ExceptionResponse.class)) })
    })
    public AccountUserAuthResponse loginAccountUser(@RequestBody @Valid LoginRequest loginRequest){
        return accountUserService.login(loginRequest);
    }

    @GetMapping("/refresh")
    @Transactional(readOnly = true)
    @Operation(summary = "Refresh auth token(s)", security = { @SecurityRequirement(name = "BearerRefreshToken") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Token(s) refreshed",
                    content = { @Content(schema = @Schema(implementation = AccountUserAuthResponse.class)) }),
            @ApiResponse(responseCode = "401",
                    description = "Invalid user credentials",
                    content = { @Content(schema = @Schema(implementation = ExceptionResponse.class)) }),
            @ApiResponse(responseCode = "500",
                    description = "Current user not found",
                    content = { @Content(schema = @Schema(implementation = ExceptionResponse.class)) })
    })
    public AccountUserAuthResponse refreshAuthTokens(@Schema(hidden = true) @RequestHeader("Authorization") String authorizationHeader){
        var currentUserId = securityService.getCurrentUserId();
        return accountUserService.refreshAuthTokens(currentUserId, authorizationHeader);
    }

    @PostMapping("/confirm-reset")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    @Operation(summary = "Resend user registration confirmation email", security = { @SecurityRequirement(name = "BearerAccessToken") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",
                    description = "Registration confirmation email resent",
                    content = { @Content(schema = @Schema) }),
            @ApiResponse(responseCode = "401",
                    description = "Invalid user credentials",
                    content = { @Content(schema = @Schema(implementation = ExceptionResponse.class)) }),
            @ApiResponse(responseCode = "404",
                    description = "Confirmation token not found",
                    content = { @Content(schema = @Schema(implementation = ExceptionResponse.class)) }),
            @ApiResponse(responseCode = "409",
                    description = "Invalid user action",
                    content = { @Content(schema = @Schema(implementation = ExceptionResponse.class)) }),
            @ApiResponse(responseCode = "500",
                    description = "Current user not found",
                    content = { @Content(schema = @Schema(implementation = ExceptionResponse.class)) })
    })
    public void resetConfirmationProcess(){
        var currentUserId = securityService.getCurrentUserId();
        accountUserService.resetConfirmationProcess(currentUserId);
    }


    @PostMapping("/confirm/{confirmationToken}")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    @Operation(summary = "Confirm user registration")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "User registration confirmed",
                    content = { @Content(schema = @Schema) }),
            @ApiResponse(responseCode = "404",
                    description = "Confirmation token not found",
                    content = { @Content(schema = @Schema(implementation = ExceptionResponse.class)) }),
            @ApiResponse(responseCode = "409",
                    description = "Invalid user action",
                    content = { @Content(schema = @Schema(implementation = ExceptionResponse.class)) })
    })
    public void confirmAccountUserRegistration(@PathVariable("confirmationToken") UUID confirmationToken){
        accountUserService.confirmAccountUserRegistration(confirmationToken);
    }

}
