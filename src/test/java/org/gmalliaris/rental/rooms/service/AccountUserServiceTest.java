package org.gmalliaris.rental.rooms.service;

import org.gmalliaris.rental.rooms.config.exception.ApiException;
import org.gmalliaris.rental.rooms.config.exception.ApiExceptionMessageConstants;
import org.gmalliaris.rental.rooms.dto.CreateUserRequest;
import org.gmalliaris.rental.rooms.dto.LoginRequest;
import org.gmalliaris.rental.rooms.entity.AccountUser;
import org.gmalliaris.rental.rooms.entity.UserRole;
import org.gmalliaris.rental.rooms.entity.UserRoleName;
import org.gmalliaris.rental.rooms.repository.AccountUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;

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

        verify(accountUserRepository).countByEmail(email);
        verify(accountUserRepository).countByPhoneNumber(phoneNumber);
        verify(accountUserRepository, never()).save(any(AccountUser.class));
    }

    @Test
    void createAccountUserTest(){
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
                UserRoleName.ROLE_GUEST, UserRoleName.ROLE_GUEST);
        var request = new CreateUserRequest("12345678", "admin@example.eg",
                "firstName", null, "123456789", roles);

        accountUserService.createAccountUser(request);

        verify(bCryptPasswordEncoder).encode(request.getPassword());
        verify(userRoleService)
                .findUserRoleByName(UserRoleName.ROLE_HOST);
        verify(userRoleService)
                .findUserRoleByName(UserRoleName.ROLE_GUEST);
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
        assertEquals(2, user.getRoles().size());
    }

    @Test
    void loginTest_throwsBecauseUserNotFound(){
        when(accountUserRepository.findByEmail(anyString()))
                .thenReturn(Optional.empty());

        var loginRequest = new LoginRequest("test@example.eg", "12345678");

        var exception = assertThrows(ApiException.class,
                () -> accountUserService.login(loginRequest));
        assertEquals(ApiExceptionMessageConstants.INVALID_CREDENTIALS, exception.getMessage());
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

        when(jwtService.generateAccessToken(any(AccountUser.class)))
                .thenReturn("access");
        when(jwtService.generateRefreshToken(any(AccountUser.class)))
                .thenReturn("refresh");

        var loginRequest = new LoginRequest("test@example.eg", "12345678");

        var result = accountUserService.login(loginRequest);
        assertNotNull(result);
        assertEquals("access", result.getAccessToken());
        assertEquals("refresh", result.getRefreshToken());

        verify(accountUserRepository).findByEmail(loginRequest.getUsername());
        verify(bCryptPasswordEncoder).matches(loginRequest.getPassword(), mockUser.getPassword());
    }
}