package org.gmalliaris.rental.rooms.controller;

import org.gmalliaris.rental.rooms.dto.CurrentAccountUserResponse;
import org.gmalliaris.rental.rooms.service.AccountUserService;
import org.gmalliaris.rental.rooms.service.SecurityService;
import org.modelmapper.ModelMapper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class AccountUserController {

    private final AccountUserService accountUserService;
    private final SecurityService securityService;
    private final ModelMapper modelMapper;

    public AccountUserController(AccountUserService accountUserService, SecurityService securityService, ModelMapper modelMapper) {
        this.accountUserService = accountUserService;
        this.securityService = securityService;
        this.modelMapper = modelMapper;
    }

    @GetMapping("/me")
    @Transactional(readOnly = true)
    public CurrentAccountUserResponse findCurrentUser(){
        var currentUserId = securityService.getCurrentUserId();
        var currentUser = accountUserService.findAccountUserById(currentUserId);
        return modelMapper.map(currentUser, CurrentAccountUserResponse.class);
    }
}
