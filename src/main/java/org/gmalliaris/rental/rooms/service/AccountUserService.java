package org.gmalliaris.rental.rooms.service;

import io.jsonwebtoken.Claims;
import org.gmalliaris.rental.rooms.config.exception.ApiException;
import org.gmalliaris.rental.rooms.config.exception.ApiExceptionMessageConstants;
import org.gmalliaris.rental.rooms.dto.AccountUserAuthResponse;
import org.gmalliaris.rental.rooms.dto.CreateUserRequest;
import org.gmalliaris.rental.rooms.dto.JwtType;
import org.gmalliaris.rental.rooms.dto.LoginRequest;
import org.gmalliaris.rental.rooms.entity.AccountUser;
import org.gmalliaris.rental.rooms.entity.ConfirmationToken;
import org.gmalliaris.rental.rooms.entity.UserRoleName;
import org.gmalliaris.rental.rooms.repository.AccountUserRepository;
import org.gmalliaris.rental.rooms.util.JwtUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.MessagingException;
import java.util.Objects;
import java.util.UUID;

@Service
public class AccountUserService {

    private final AccountUserRepository accountUserRepository;
    private final UserRoleService userRoleService;
    private final ConfirmationTokenService tokenService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JwtService jwtService;
    private final MailService mailService;

    public AccountUserService(AccountUserRepository accountUserRepository, UserRoleService userRoleService,
                              ConfirmationTokenService tokenService, BCryptPasswordEncoder bCryptPasswordEncoder,
                              JwtService jwtService, MailService mailService) {
        this.accountUserRepository = accountUserRepository;
        this.userRoleService = userRoleService;
        this.tokenService = tokenService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.jwtService = jwtService;
        this.mailService = mailService;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void createAccountUser(CreateUserRequest request){

        if (request.getRoles().contains(UserRoleName.ROLE_ADMIN)){
            throw new ApiException(HttpStatus.BAD_REQUEST, ApiExceptionMessageConstants.INVALID_USER_ROLES_REGISTRATION);
        }

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

        var confirmationToken = tokenService.createTokenForUser(user);
        sendRegistrationConfirmationEmail(confirmationToken);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void confirmAccountUserRegistration(UUID confirmationToken){

        var activatedToken = tokenService.useConfirmationToken(confirmationToken);

        var user = activatedToken.getAccountUser();
        if (user.isEnabled()){
            throw new ApiException(HttpStatus.CONFLICT, ApiExceptionMessageConstants.USER_ALREADY_CONFIRMED);
        }
        user.setEnabled(true);
        accountUserRepository.save(user);
    }

    @Transactional(readOnly = true)
    public AccountUserAuthResponse login(LoginRequest loginRequest){
        var user = accountUserRepository.findByEmail(loginRequest.getUsername())
                .orElseThrow(() -> {
                    throw new ApiException(HttpStatus.UNAUTHORIZED, ApiExceptionMessageConstants.INVALID_CREDENTIALS);
                });
        var encryptedPwd = user.getPassword();
        if (!bCryptPasswordEncoder.matches(loginRequest.getPassword(), encryptedPwd)){
            throw new ApiException(HttpStatus.UNAUTHORIZED, ApiExceptionMessageConstants.INVALID_CREDENTIALS);
        }

        var randomTokenGroupId = UUID.randomUUID();
        var accessToken = jwtService.generateAccessToken(user, randomTokenGroupId.toString());
        var refreshToken = jwtService.generateRefreshToken(user, randomTokenGroupId.toString());
        return new AccountUserAuthResponse(accessToken, refreshToken);
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY)
    public AccountUser findAccountUserById(UUID userId){

        Objects.requireNonNull(userId);
        return accountUserRepository.findById(userId)
                .orElseThrow(() -> {
                    var errMsg = String.format(ApiExceptionMessageConstants.ENTITY_NOT_FOUND_TEMPLATE,
                            AccountUser.class, userId);
                    throw new IllegalStateException(errMsg);
                });
    }

    @Transactional(readOnly = true)
    public AccountUserAuthResponse refreshAuthTokens(UUID userId, String authHeader){

        var validClaims = extractValidClaimsFromHeader(authHeader, JwtType.REFRESH);
        var tokenGroupId = JwtUtils.extractTokenGroupIdFromClaims(validClaims);
        var refreshExpiration = validClaims.getExpiration();
        var user = findAccountUserById(userId);

        var newRefreshTokenOptional = jwtService.generateRefreshTokenIfNeeded(user, refreshExpiration, tokenGroupId);

        if (newRefreshTokenOptional.isEmpty()) { // no refresh token generated
            var accessToken = jwtService.generateAccessToken(user, tokenGroupId);
            return new AccountUserAuthResponse(accessToken, null);
        }

        var newRefreshToken = newRefreshTokenOptional.get();
        var newTokenGroupId = JwtUtils.extractValidClaimsFromToken(newRefreshToken, JwtType.REFRESH)
                .map(JwtUtils::extractTokenGroupIdFromClaims)
                .orElseThrow(() -> {
                    var errMsg = "Invalid token, token group id is missing.";
                    throw new IllegalStateException(errMsg);
                });
        var accessToken = jwtService.generateAccessToken(user, newTokenGroupId);
        return new AccountUserAuthResponse(accessToken, newRefreshToken);
    }

    @Transactional
    public void resetConfirmationProcess(UUID currentUserId){

        var currentUser = findAccountUserById(currentUserId);
        if (currentUser.isEnabled()){
            throw new ApiException(HttpStatus.CONFLICT, ApiExceptionMessageConstants.USER_ALREADY_CONFIRMED);
        }
        var newToken = tokenService.replaceConfirmationTokenForUser(currentUser);

        sendRegistrationConfirmationEmail(newToken);
    }

    private void sendRegistrationConfirmationEmail(ConfirmationToken token){
        try {
            mailService.sendRegistrationConfirmationEmail(token);
        } catch (MessagingException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                    String.format(ApiExceptionMessageConstants.FAILED_EMAIL, "Registration Confirmation"));
        }
    }

    public void logoutUser(String authHeader) {

        var validClaims = extractValidClaimsFromHeader(authHeader, JwtType.ACCESS);
        var tokenGroupId = JwtUtils.extractTokenGroupIdFromClaims(validClaims);
        jwtService.blacklistTokenGroup(tokenGroupId);
    }

    private static Claims extractValidClaimsFromHeader(String authHeader, JwtType type) {

        return JwtUtils.extractValidClaimsFromHeader(authHeader, type)
                .orElseThrow(() -> {
                    var errMsg = "Invalid token, token group id is missing.";
                    throw new IllegalStateException(errMsg);
                });
    }
}
