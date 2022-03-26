package org.gmalliaris.rental.rooms.service;

import org.gmalliaris.rental.rooms.config.exception.ApiException;
import org.gmalliaris.rental.rooms.config.exception.ApiExceptionMessageConstants;
import org.gmalliaris.rental.rooms.dto.CreateUserRequest;
import org.gmalliaris.rental.rooms.dto.LoginRequest;
import org.gmalliaris.rental.rooms.dto.LoginResponse;
import org.gmalliaris.rental.rooms.entity.AccountUser;
import org.gmalliaris.rental.rooms.repository.AccountUserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
public class AccountUserService {

    private final AccountUserRepository accountUserRepository;
    private final UserRoleService userRoleService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JwtService jwtService;

    public AccountUserService(AccountUserRepository accountUserRepository, UserRoleService userRoleService, BCryptPasswordEncoder bCryptPasswordEncoder, JwtService jwtService) {
        this.accountUserRepository = accountUserRepository;
        this.userRoleService = userRoleService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.jwtService = jwtService;
    }

    private AccountUser findByEmail(String email){
        return accountUserRepository.findByEmail(email)
                .orElseThrow(() -> {
                    throw new ApiException(HttpStatus.BAD_REQUEST, ApiExceptionMessageConstants.INVALID_CREDENTIALS);
                });
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void createAccountUser(CreateUserRequest request){

        if (accountUserRepository.countByEmail(request.getEmail()) > 0){
            throw new ApiException(HttpStatus.CONFLICT, ApiExceptionMessageConstants.USED_EMAIL);
        }

        if ( request.getPhoneNumber() != null
            && accountUserRepository.countByPhoneNumber(request.getPhoneNumber()) > 0){
            throw new ApiException(HttpStatus.CONFLICT, ApiExceptionMessageConstants.USED_PHONE_NUMBER);
        }

        var user = new AccountUser();
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        var encodedPassword = bCryptPasswordEncoder.encode(request.getPassword());
        user.setPassword(encodedPassword);
        user.setPhoneNumber(request.getPhoneNumber());
        user.setEnabled(false);
        request.getRoles()
            .stream()
            .distinct()
            .forEach( roleName -> {
                var userRole = userRoleService.findUserRoleByName(roleName);
                user.addRole(userRole);
            });

        accountUserRepository.save(user);
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest loginRequest){
        var user = findByEmail(loginRequest.getUsername());
        var encryptedPwd = user.getPassword();
        if (!bCryptPasswordEncoder.matches(loginRequest.getPassword(), encryptedPwd)){
            throw new ApiException(HttpStatus.BAD_REQUEST, ApiExceptionMessageConstants.INVALID_CREDENTIALS);
        }

        var accessToken = jwtService.generateAccessToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        return new LoginResponse(accessToken, refreshToken);
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY)
    public AccountUser findAccountUserById(UUID userId){

        Objects.requireNonNull(userId);
        return accountUserRepository.findById(userId)
                .orElseThrow(() -> {
                    var errMsg = String.format(ApiExceptionMessageConstants.ENTITY_NOT_FOUND_TEMPLATE,
                            AccountUser.class, userId);
                    throw new ApiException(HttpStatus.NOT_FOUND, errMsg);
                });
    }
}
