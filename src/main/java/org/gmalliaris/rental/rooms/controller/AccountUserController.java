package org.gmalliaris.rental.rooms.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.gmalliaris.rental.rooms.config.exception.ExceptionResponse;
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
@SecurityRequirement(name = "BearerAccessToken")
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
    @Operation(summary = "Get information of current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Requested information",
                    content = { @Content(schema = @Schema(implementation = CurrentAccountUserResponse.class)) }),
            @ApiResponse(responseCode = "401",
                    description = "Current user is unauthorized",
                    content = { @Content(schema = @Schema(implementation = ExceptionResponse.class)) }),
            @ApiResponse(responseCode = "500",
                    description = "Current user not found",
                    content = { @Content(schema = @Schema(implementation = ExceptionResponse.class)) })
    })
    public CurrentAccountUserResponse findCurrentUser(){
        var currentUserId = securityService.getCurrentUserId();
        var currentUser = accountUserService.findAccountUserById(currentUserId);
        return modelMapper.map(currentUser, CurrentAccountUserResponse.class);
    }
}
