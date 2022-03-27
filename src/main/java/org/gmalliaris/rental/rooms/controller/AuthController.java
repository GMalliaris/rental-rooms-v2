package org.gmalliaris.rental.rooms.controller;

import org.gmalliaris.rental.rooms.dto.AccountUserAuthResponse;
import org.gmalliaris.rental.rooms.dto.CreateUserRequest;
import org.gmalliaris.rental.rooms.dto.LoginRequest;
import org.gmalliaris.rental.rooms.service.AccountUserService;
import org.gmalliaris.rental.rooms.service.SecurityService;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

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
    public void registerAccountUser(@RequestBody @Valid CreateUserRequest createUserRequest){
        accountUserService.createAccountUser(createUserRequest);
    }

    @PostMapping("/login")
    @Transactional(readOnly = true)
    public AccountUserAuthResponse registerAccountUser(@RequestBody @Valid LoginRequest loginRequest){
        return accountUserService.login(loginRequest);
    }

    @GetMapping("/refresh")
    @Transactional(readOnly = true)
    public AccountUserAuthResponse refreshAuthTokens(@RequestHeader("Authorization") String authorizationHeader){
        var currentUserId = securityService.getCurrentUserId();
        return accountUserService.refreshAuthTokens(currentUserId, authorizationHeader);
    }

}
