package org.gmalliaris.rental.rooms.controller;

import org.gmalliaris.rental.rooms.dto.CreateUserRequest;
import org.gmalliaris.rental.rooms.dto.LoginRequest;
import org.gmalliaris.rental.rooms.dto.AccountUserAuthResponse;
import org.gmalliaris.rental.rooms.service.AccountUserService;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AccountUserService accountUserService;

    public AuthController(AccountUserService accountUserService) {
        this.accountUserService = accountUserService;
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

}
