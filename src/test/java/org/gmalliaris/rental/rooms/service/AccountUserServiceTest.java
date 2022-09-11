package org.gmalliaris.rental.rooms.service;

import io.jsonwebtoken.Claims;
import org.gmalliaris.rental.rooms.config.exception.ApiException;
import org.gmalliaris.rental.rooms.config.exception.ApiExceptionMessageConstants;
import org.gmalliaris.rental.rooms.dto.CreateUserRequest;
import org.gmalliaris.rental.rooms.dto.JwtType;
import org.gmalliaris.rental.rooms.dto.LoginRequest;
import org.gmalliaris.rental.rooms.entity.AccountUser;
import org.gmalliaris.rental.rooms.entity.ConfirmationToken;
import org.gmalliaris.rental.rooms.entity.UserRole;
import org.gmalliaris.rental.rooms.entity.UserRoleName;
import org.gmalliaris.rental.rooms.repository.AccountUserRepository;
import org.gmalliaris.rental.rooms.util.JwtUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.mail.MessagingException;
import java.sql.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountUserServiceTest {

    @InjectMocks
    private AccountUserService accountUserService;

    @Mock
    private AccountUserRepository accountUserRepository;

    @Mock
    private UserRoleService userRoleService;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private ConfirmationTokenService tokenService;

    @Mock
    private MailService mailService;

    @Test
    void createAccountUserTest_throwsBecauseAdminUser(){
        var roles = List.of(UserRoleName.ROLE_ADMIN);
        var request = new CreateUserRequest("12345678", "admin@example.eg",
                "firstName", null, "123456789", roles);

        var exception = assertThrows(ApiException.class,
                () -> accountUserService.createAccountUser(request));
        assertEquals(ApiExceptionMessageConstants.INVALID_USER_ROLES_REGISTRATION,
                exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void createAccountUserTest_throwsBecauseUsedEmail(){
        when(accountUserRepository.countByEmail(anyString()))
                .thenReturn(1L);

        var email = "admin@example.eg";
        var mockRequest = mock(CreateUserRequest.class);
        when(mockRequest.getEmail())
                .thenReturn(email);

        var exception = assertThrows(ApiException.class,
                () -> accountUserService.createAccountUser(mockRequest));
        assertEquals(ApiExceptionMessageConstants.USED_EMAIL,
                exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());

        verify(accountUserRepository).countByEmail(email);
        verify(accountUserRepository, never()).save(any(AccountUser.class));
    }

    @Test
    void createAccountUserTest_throwsBecauseUsedPhoneNumber(){
        when(accountUserRepository.countByEmail(anyString()))
                .thenReturn(0L);
        when(accountUserRepository.countByPhoneNumber(anyString()))
                .thenReturn(1L);

        var email = "admin@example.eg";
        var phoneNumber = "123456789";
        var mockRequest = mock(CreateUserRequest.class);
        when(mockRequest.getEmail())
                .thenReturn(email);
        when(mockRequest.getPhoneNumber())
                .thenReturn(phoneNumber);

        var exception = assertThrows(ApiException.class,
                () -> accountUserService.createAccountUser(mockRequest));
        assertEquals(ApiExceptionMessageConstants.USED_PHONE_NUMBER,
                exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());

        verify(accountUserRepository).countByEmail(email);
        verify(accountUserRepository).countByPhoneNumber(phoneNumber);
        verify(accountUserRepository, never()).save(any(AccountUser.class));
    }

    @Test
    void createAccountUserTest() throws MessagingException {
        when(accountUserRepository.countByEmail(anyString()))
                .thenReturn(0L);

        var encoded = "encoded";
        when(bCryptPasswordEncoder.encode(anyString()))
                .thenReturn(encoded);

        when(userRoleService.findUserRoleByName(any(UserRoleName.class)))
                .then(i -> {
                    var role = new UserRole();
                    role.setName(i.getArgument(0));
                    return role;
                });

        var roles = List.of(UserRoleName.ROLE_HOST,
                UserRoleName.ROLE_GUEST, UserRoleName.ROLE_HOST);
        var request = new CreateUserRequest("12345678", "admin@example.eg",
                "firstName", null, "123456789", roles);

        var mockToken = mock(ConfirmationToken.class);
        when(tokenService.createTokenForUser(any(AccountUser.class)))
                .thenReturn(mockToken);

        accountUserService.createAccountUser(request);

        verify(bCryptPasswordEncoder).encode(request.getPassword());
        roles.forEach(role -> verify(userRoleService)
                .findUserRoleByName(role));
        var argCaptor = ArgumentCaptor.forClass(AccountUser.class);
        verify(accountUserRepository).save(argCaptor.capture());

        var user = argCaptor.getValue();
        assertNotNull(user);
        assertEquals(request.getEmail(), user.getEmail());
        assertEquals(encoded, user.getPassword());
        assertEquals(request.getFirstName(), user.getFirstName());
        assertEquals(request.getLastName(), user.getLastName());
        assertEquals(request.getPhoneNumber(), user.getPhoneNumber());
        assertFalse(user.isEnabled());
        assertEquals(new HashSet<>(request.getRoles()).size(), user.getRoles().size());

        verify(tokenService).createTokenForUser(user);
        verify(mailService).sendRegistrationConfirmationEmail(mockToken);
    }

    @Test
    void createAccountUserTest_throwsMessageException() throws MessagingException {
        when(accountUserRepository.countByEmail(anyString()))
                .thenReturn(0L);

        var encoded = "encoded";
        when(bCryptPasswordEncoder.encode(anyString()))
                .thenReturn(encoded);

        when(userRoleService.findUserRoleByName(any(UserRoleName.class)))
                .then(i -> {
                    var role = new UserRole();
                    role.setName(i.getArgument(0));
                    return role;
                });

        var roles = List.of(UserRoleName.ROLE_HOST,
                UserRoleName.ROLE_GUEST, UserRoleName.ROLE_HOST);
        var request = new CreateUserRequest("12345678", "admin@example.eg",
                "firstName", null, "123456789", roles);

        var mockToken = mock(ConfirmationToken.class);
        when(tokenService.createTokenForUser(any(AccountUser.class)))
                .thenReturn(mockToken);
        doThrow(MessagingException.class).when(mailService)
                .sendRegistrationConfirmationEmail(any(ConfirmationToken.class));

        var exception = assertThrows(ApiException.class,
                () -> accountUserService.createAccountUser(request));
        assertEquals(String.format(ApiExceptionMessageConstants.FAILED_EMAIL,
                        "Registration Confirmation"),
                exception.getMessage());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatus());
    }

    @Test
    void loginTest_throwsBecauseUserNotFound(){
        when(accountUserRepository.findByEmail(anyString()))
                .thenReturn(Optional.empty());

        var loginRequest = new LoginRequest("test@example.eg", "12345678");

        var exception = assertThrows(ApiException.class,
                () -> accountUserService.login(loginRequest));
        assertEquals(ApiExceptionMessageConstants.INVALID_CREDENTIALS, exception.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());

        verify(accountUserRepository).findByEmail(loginRequest.getUsername());
    }

    @Test
    void loginTest_throwsBecausePasswordNotMatching(){

        var mockUser = mock(AccountUser.class);
        when(mockUser.getPassword()).thenReturn("12345678");

        when(accountUserRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(mockUser));

        when(bCryptPasswordEncoder.matches(anyString(), anyString()))
                .thenReturn(false);

        var loginRequest = new LoginRequest("test@example.eg", "12345678");

        var exception = assertThrows(ApiException.class,
                () -> accountUserService.login(loginRequest));
        assertEquals(ApiExceptionMessageConstants.INVALID_CREDENTIALS, exception.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());

        verify(accountUserRepository).findByEmail(loginRequest.getUsername());
        verify(bCryptPasswordEncoder).matches(loginRequest.getPassword(), mockUser.getPassword());
    }

    @Test
    void loginTest(){

        var mockUser = mock(AccountUser.class);
        when(mockUser.getPassword()).thenReturn("12345678");

        when(accountUserRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(mockUser));

        when(bCryptPasswordEncoder.matches(anyString(), anyString()))
                .thenReturn(true);

        when(jwtService.generateAccessToken(any(AccountUser.class), any(String.class)))
                .thenReturn("access");
        when(jwtService.generateRefreshToken(any(AccountUser.class), any(String.class)))
                .thenReturn("refresh");

        var loginRequest = new LoginRequest("test@example.eg", "12345678");

        var result = accountUserService.login(loginRequest);
        assertNotNull(result);
        assertEquals("access", result.getAccessToken());
        assertEquals("refresh", result.getRefreshToken());

        verify(accountUserRepository).findByEmail(loginRequest.getUsername());
        verify(bCryptPasswordEncoder).matches(loginRequest.getPassword(), mockUser.getPassword());
        var tgidCaptor = ArgumentCaptor.forClass(String.class);
        verify(jwtService).generateAccessToken(eq(mockUser), tgidCaptor.capture());
        verify(jwtService).generateRefreshToken(eq(mockUser), tgidCaptor.capture());
        var tgids = tgidCaptor.getAllValues();
        assertEquals(2, tgids.size());
        assertEquals(tgids.get(0), tgids.get(1));
    }

    @Test
    void findAccountUserByIdTest_throws(){

        var randomId = UUID.randomUUID();

        when(accountUserRepository.findById(any(UUID.class)))
                .thenReturn(Optional.empty());

        var exception = assertThrows(IllegalStateException.class,
                () -> accountUserService.findAccountUserById(randomId));
        var errMsg = String.format(ApiExceptionMessageConstants.ENTITY_NOT_FOUND_TEMPLATE,
                AccountUser.class, randomId);
        assertEquals(errMsg, exception.getMessage());
    }

    @Test
    void findAccountUserByIdTest(){

        var randomId = UUID.randomUUID();

        var user = mock(AccountUser.class);
        when(accountUserRepository.findById(any(UUID.class)))
                .thenReturn(Optional.of(user));

        var result = accountUserService.findAccountUserById(randomId);
        assertNotNull(result);
        assertEquals(result, user);
    }

    @Test
    void refreshAuthTokensTest_invalidClaims(){
        var header = "invalidHeader";

        var userId = UUID.randomUUID();
        var exception = assertThrows(IllegalStateException.class,
                () -> accountUserService.refreshAuthTokens(userId, header));
        assertEquals("Invalid token, token group id is missing.", exception.getMessage());
    }

    @Test
    void refreshAuthTokensTest_noRefreshTokenGenerated(){
        var header = "header";
        var userId = UUID.randomUUID();
        var user = mock(AccountUser.class);
        var tokenGroupId = UUID.randomUUID().toString();
        var exp = mock(Date.class);
        var accessToken = "access";

        try(var jwtUtils = mockStatic(JwtUtils.class)) {
            var mockClaims = mock(Claims.class);
            when(mockClaims.getExpiration())
                    .thenReturn(exp);
            jwtUtils.when(() -> JwtUtils.extractValidClaimsFromHeader(anyString(), any(JwtType.class)))
                    .thenReturn(Optional.of(mockClaims));
            jwtUtils.when(() -> JwtUtils.extractTokenGroupIdFromClaims(any(Claims.class)))
                    .thenReturn(tokenGroupId);
            when(accountUserRepository.findById(any(UUID.class)))
                    .thenReturn(Optional.of(user));
            when(jwtService.generateRefreshTokenIfNeeded(any(AccountUser.class), any(Date.class),
                    anyString()))
                    .thenReturn(Optional.empty());
            when(jwtService.generateAccessToken(any(AccountUser.class), anyString()))
                    .thenReturn(accessToken);

            var result = accountUserService.refreshAuthTokens(userId, header);
            assertNotNull(result);
            assertEquals(accessToken, result.getAccessToken());
            assertNull(result.getRefreshToken());

            jwtUtils.verify(() -> JwtUtils.extractValidClaimsFromHeader(header, JwtType.REFRESH));
            jwtUtils.verify(() -> JwtUtils.extractTokenGroupIdFromClaims(mockClaims));
            verify(accountUserRepository).findById(userId);
            verify(jwtService).generateRefreshTokenIfNeeded(user, exp, tokenGroupId);
            verify(jwtService).generateAccessToken(user, tokenGroupId);
        }
    }

    @Test
    void refreshAuthTokensTest_refreshTokenGenerated(){
        var header = "header";
        var userId = UUID.randomUUID();
        var user = mock(AccountUser.class);
        var tokenGroupId = UUID.randomUUID().toString();
        var newTokenGroupId = UUID.randomUUID().toString();
        var exp = mock(Date.class);
        var accessToken = "access";
        var refreshToken = "refreshToken";

        try(var jwtUtils = mockStatic(JwtUtils.class)) {
            var mockClaims = mock(Claims.class);
            when(mockClaims.getExpiration())
                    .thenReturn(exp);
            jwtUtils.when(() -> JwtUtils.extractValidClaimsFromHeader(anyString(), any(JwtType.class)))
                    .thenReturn(Optional.of(mockClaims));
            jwtUtils.when(() -> JwtUtils.extractTokenGroupIdFromClaims(mockClaims))
                    .thenReturn(tokenGroupId);
            when(accountUserRepository.findById(any(UUID.class)))
                    .thenReturn(Optional.of(user));
            when(jwtService.generateRefreshTokenIfNeeded(any(AccountUser.class), any(Date.class),
                    anyString()))
                    .thenReturn(Optional.of(refreshToken));

            var refreshClaims = mock(Claims.class);
            jwtUtils.when(() -> JwtUtils.extractValidClaimsFromToken(anyString(), any(JwtType.class)))
                    .thenReturn(Optional.of(refreshClaims));
            jwtUtils.when(() -> JwtUtils.extractTokenGroupIdFromClaims(refreshClaims))
                    .thenReturn(newTokenGroupId);

            when(jwtService.generateAccessToken(any(AccountUser.class), anyString()))
                    .thenReturn(accessToken);

            var result = accountUserService.refreshAuthTokens(userId, header);
            assertNotNull(result);
            assertEquals(accessToken, result.getAccessToken());
            assertEquals(refreshToken, result.getRefreshToken());

            jwtUtils.verify(() -> JwtUtils.extractValidClaimsFromHeader(header, JwtType.REFRESH));
            jwtUtils.verify(() -> JwtUtils.extractTokenGroupIdFromClaims(mockClaims));
            verify(accountUserRepository).findById(userId);
            jwtUtils.verify(() -> JwtUtils.extractValidClaimsFromToken(refreshToken, JwtType.REFRESH));
            jwtUtils.verify(() -> JwtUtils.extractTokenGroupIdFromClaims(refreshClaims));
            verify(jwtService).generateRefreshTokenIfNeeded(user, exp, tokenGroupId);
            verify(jwtService).generateAccessToken(user, newTokenGroupId);
        }
    }

    @Test
    void refreshAuthTokensTest_invalidRefreshTokenGenerated(){
        var header = "header";
        var userId = UUID.randomUUID();
        var user = mock(AccountUser.class);
        var tokenGroupId = UUID.randomUUID().toString();
        var exp = mock(Date.class);
        var refreshToken = "refreshToken";

        try(var jwtUtils = mockStatic(JwtUtils.class)) {
            var mockClaims = mock(Claims.class);
            when(mockClaims.getExpiration())
                    .thenReturn(exp);
            jwtUtils.when(() -> JwtUtils.extractValidClaimsFromHeader(anyString(), any(JwtType.class)))
                    .thenReturn(Optional.of(mockClaims));
            jwtUtils.when(() -> JwtUtils.extractTokenGroupIdFromClaims(mockClaims))
                    .thenReturn(tokenGroupId);
            when(accountUserRepository.findById(any(UUID.class)))
                    .thenReturn(Optional.of(user));
            when(jwtService.generateRefreshTokenIfNeeded(any(AccountUser.class), any(Date.class),
                    anyString()))
                    .thenReturn(Optional.of(refreshToken));

            jwtUtils.when(() -> JwtUtils.extractValidClaimsFromToken(anyString(), any(JwtType.class)))
                    .thenReturn(Optional.empty());

            var exception = assertThrows(IllegalStateException.class,
                    () -> accountUserService.refreshAuthTokens(userId, header));
            assertEquals("Invalid token, token group id is missing.", exception.getMessage());

            jwtUtils.verify(() -> JwtUtils.extractValidClaimsFromHeader(header, JwtType.REFRESH));
            jwtUtils.verify(() -> JwtUtils.extractTokenGroupIdFromClaims(mockClaims));
            verify(accountUserRepository).findById(userId);
            jwtUtils.verify(() -> JwtUtils.extractValidClaimsFromToken(refreshToken, JwtType.REFRESH));
        }
    }

    @Test
    void confirmAccountUserRegistrationTest_throwsBecauseAlreadyActivated(){
        var user = new AccountUser();
        user.setEnabled(true);

        var token = new ConfirmationToken();
        token.setAccountUser(user);

        when(tokenService.useConfirmationToken(any(UUID.class)))
                .thenReturn(token);

        var uuid = UUID.randomUUID();
        var exception = assertThrows(ApiException.class,
                () -> accountUserService.confirmAccountUserRegistration(uuid));
        assertEquals(ApiExceptionMessageConstants.USER_ALREADY_CONFIRMED,
                exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());

        verify(tokenService).useConfirmationToken(uuid);
    }

    @Test
    void confirmAccountUserRegistrationTest(){
        var user = new AccountUser();
        user.setEnabled(false);

        var token = new ConfirmationToken();
        token.setAccountUser(user);

        when(tokenService.useConfirmationToken(any(UUID.class)))
                .thenReturn(token);

        var uuid = UUID.randomUUID();
        accountUserService.confirmAccountUserRegistration(uuid);

        verify(tokenService).useConfirmationToken(uuid);
        verify(accountUserRepository).save(user);
    }

    @Test
    void resetConfirmationProcessTest_throwsBecauseIsEnabled() {

        var user = mock(AccountUser.class);
        when(user.isEnabled()).thenReturn(true);

        when(accountUserRepository.findById(any(UUID.class)))
                .thenReturn(Optional.of(user));

        var uuid = UUID.randomUUID();
        var exception = assertThrows(ApiException.class,
                () -> accountUserService.resetConfirmationProcess(uuid));
        assertEquals(ApiExceptionMessageConstants.USER_ALREADY_CONFIRMED,
                exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());

        verify(accountUserRepository).findById(uuid);
        verifyNoInteractions(tokenService);
        verifyNoInteractions(mailService);
    }

    @Test
    void resetConfirmationProcessTest() throws MessagingException {

        var user = mock(AccountUser.class);
        var token = mock(ConfirmationToken.class);

        when(accountUserRepository.findById(any(UUID.class)))
                .thenReturn(Optional.of(user));
        when(tokenService.replaceConfirmationTokenForUser(any(AccountUser.class)))
                .thenReturn(token);

        var uuid = UUID.randomUUID();
        accountUserService.resetConfirmationProcess(uuid);

        verify(accountUserRepository).findById(uuid);
        verify(tokenService).replaceConfirmationTokenForUser(user);
        verify(mailService).sendRegistrationConfirmationEmail(token);
    }

    @Test
    void logoutUserTest() {

        try (var jwtUtils = mockStatic(JwtUtils.class)) {
            var mockClaims = mock(Claims.class);
            var tgid = UUID.randomUUID().toString();
            jwtUtils.when(() -> JwtUtils.extractValidClaimsFromHeader(anyString(), any(JwtType.class)))
                    .thenReturn(Optional.of(mockClaims));
            jwtUtils.when(() -> JwtUtils.extractTokenGroupIdFromClaims(any(Claims.class)))
                    .thenReturn(tgid);

            var header = "header";
            accountUserService.logoutUser(header);
            jwtUtils.verify(() -> JwtUtils.extractValidClaimsFromHeader(header, JwtType.ACCESS));
            jwtUtils.verify(() -> JwtUtils.extractTokenGroupIdFromClaims(mockClaims));
            verify(jwtService).blacklistTokenGroup(tgid);
        }
    }
}