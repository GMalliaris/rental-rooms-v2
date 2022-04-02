package org.gmalliaris.rental.rooms.service;

import org.gmalliaris.rental.rooms.config.exception.ApiException;
import org.gmalliaris.rental.rooms.config.exception.ApiExceptionMessageConstants;
import org.gmalliaris.rental.rooms.entity.AccountUser;
import org.gmalliaris.rental.rooms.entity.ConfirmationToken;
import org.gmalliaris.rental.rooms.entity.ConfirmationTokenStatus;
import org.gmalliaris.rental.rooms.repository.ConfirmationTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfirmationTokenServiceTest {

    @InjectMocks
    private ConfirmationTokenService tokenService;

    @Mock
    private ConfirmationTokenRepository tokenRepository;

    @Test
    void createTokenForUserTest(){
        when(tokenRepository.save(any(ConfirmationToken.class)))
                .then(i -> i.getArgument(0));

        var user = new AccountUser();
        user.setId(UUID.randomUUID());

        var now = LocalDate.now();
        try(var dateUtils = mockStatic(LocalDate.class)){
            dateUtils.when(LocalDate::now)
                    .thenReturn(now);
            var tokenDuration = 14;
            ReflectionTestUtils.setField(tokenService,
                    "durationInDays", tokenDuration);

            var result = tokenService.createTokenForUser(user);
            assertNotNull(result);
            assertEquals(ConfirmationTokenStatus.PENDING, result.getStatus());
            assertEquals(now.plusDays(tokenDuration), result.getExpirationDate());
            assertEquals(user, result.getAccountUser());
        }
    }

    @Test
    void useConfirmationTokenTest_throwsBecausNotFound(){
        var uuid = UUID.randomUUID();

        when(tokenRepository.findById(any(UUID.class)))
                .thenReturn(Optional.empty());

        var exception = assertThrows(ApiException.class,
                () -> tokenService.useConfirmationToken(uuid));

        var expectedErrMsg = String.format(ApiExceptionMessageConstants.ENTITY_NOT_FOUND_TEMPLATE,
                "ConfirmationToken", uuid);
        assertEquals(expectedErrMsg, exception.getMessage());
        verify(tokenRepository).findById(uuid);
    }

    @Test
    void useConfirmationTokenTest_throwsTokenExpired(){
        var uuid = UUID.randomUUID();

        var token = new ConfirmationToken();
        token.setStatus(ConfirmationTokenStatus.EXPIRED);
        when(tokenRepository.findById(any(UUID.class)))
                .thenReturn(Optional.of(token));

        var exception = assertThrows(ApiException.class,
                () -> tokenService.useConfirmationToken(uuid));

        assertEquals(ApiExceptionMessageConstants.CONFIRMATION_TOKEN_EXPIRED, exception.getMessage());
        verify(tokenRepository).findById(uuid);
    }

    @Test
    void useConfirmationTokenTest_throwsTokenActivated(){
        var uuid = UUID.randomUUID();

        var token = new ConfirmationToken();
        token.setStatus(ConfirmationTokenStatus.ACTIVATED);
        when(tokenRepository.findById(any(UUID.class)))
                .thenReturn(Optional.of(token));

        var exception = assertThrows(ApiException.class,
                () -> tokenService.useConfirmationToken(uuid));

        assertEquals(ApiExceptionMessageConstants.CONFIRMATION_TOKEN_ALREADY_USER, exception.getMessage());
        verify(tokenRepository).findById(uuid);
    }

    @Test
    void useConfirmationTokenTest(){
        var uuid = UUID.randomUUID();

        var token = new ConfirmationToken();
        token.setStatus(ConfirmationTokenStatus.PENDING);
        when(tokenRepository.findById(any(UUID.class)))
                .thenReturn(Optional.of(token));
        when(tokenRepository.save(any(ConfirmationToken.class)))
                .then(i -> i.getArgument(0));

        var result = tokenService.useConfirmationToken(uuid);
        assertEquals(token, result);
        assertEquals(ConfirmationTokenStatus.ACTIVATED ,token.getStatus());

        verify(tokenRepository).findById(uuid);
        verify(tokenRepository).save(token);
    }
}