package org.gmalliaris.rental.rooms.service;

import org.gmalliaris.rental.rooms.config.JwtConfigurationProperties;
import org.gmalliaris.rental.rooms.dto.JwtType;
import org.gmalliaris.rental.rooms.entity.AccountUser;
import org.gmalliaris.rental.rooms.util.JwtUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    @Mock
    private JwtConfigurationProperties jwtConfigurationProperties;

    @Test
    void generateAccessTokenTest() {

        var accessTokenDuration = 120;
        when(jwtConfigurationProperties.getAccessExpirationSeconds())
                .thenReturn(accessTokenDuration);

        var accountUser = new AccountUser();
        accountUser.setId(UUID.randomUUID());

        try (var jwtUtils = mockStatic(JwtUtils.class)) {
            jwtUtils.when(() -> JwtUtils.generateToken(any(Date.class),
                            any(Date.class), any(JwtType.class), any(UUID.class)))
                    .thenReturn("generatedToken");

            var token = jwtService.generateAccessToken(accountUser);
            assertNotNull(token);
            assertEquals("generatedToken", token);

            var dateCapturer = ArgumentCaptor.forClass(Date.class);
            var typeCapturer = ArgumentCaptor.forClass(JwtType.class);
            var uuidCapturer = ArgumentCaptor.forClass(UUID.class);
            jwtUtils.verify(() -> JwtUtils.generateToken(
                    dateCapturer.capture(), dateCapturer.capture(),
                    typeCapturer.capture(), uuidCapturer.capture()));
            var dates = dateCapturer.getAllValues();
            var issuedAt = dates.get(0);
            var expiration = dates.get(1);
            var type = typeCapturer.getValue();
            var uuid = uuidCapturer.getValue();
            assertNotNull(issuedAt);
            assertNotNull(expiration);
            var diff = ChronoUnit.SECONDS.between(issuedAt.toInstant(), expiration.toInstant());
            assertEquals(accessTokenDuration, diff);
            assertEquals(accountUser.getId(), uuid);
            assertEquals(JwtType.ACCESS, type);
        }
    }

    @Test
    void generateRefreshTokenTest() {

        var refreshTokenDuration = 60;
        when(jwtConfigurationProperties.getRefreshExpirationMinutes())
                .thenReturn(refreshTokenDuration);

        var accountUser = new AccountUser();
        accountUser.setId(UUID.randomUUID());

        try (var jwtUtils = mockStatic(JwtUtils.class)) {
            jwtUtils.when(() -> JwtUtils.generateToken(any(Date.class),
                            any(Date.class), any(JwtType.class), any(UUID.class)))
                    .thenReturn("generatedToken");

            var token = jwtService.generateRefreshToken(accountUser);
            assertNotNull(token);
            assertEquals("generatedToken", token);

            var dateCapturer = ArgumentCaptor.forClass(Date.class);
            var typeCapturer = ArgumentCaptor.forClass(JwtType.class);
            var uuidCapturer = ArgumentCaptor.forClass(UUID.class);
            jwtUtils.verify(() -> JwtUtils.generateToken(
                    dateCapturer.capture(), dateCapturer.capture(),
                    typeCapturer.capture(), uuidCapturer.capture()));
            var dates = dateCapturer.getAllValues();
            var issuedAt = dates.get(0);
            var expiration = dates.get(1);
            var type = typeCapturer.getValue();
            var uuid = uuidCapturer.getValue();
            assertNotNull(issuedAt);
            assertNotNull(expiration);
            var diff = ChronoUnit.MINUTES.between(issuedAt.toInstant(), expiration.toInstant());
            assertEquals(refreshTokenDuration, diff);
            assertEquals(accountUser.getId(), uuid);
            assertEquals(JwtType.REFRESH, type);
        }
    }

    @Test
    void generateNewRefreshTokenTest_throws(){

        var authHeader = "header";
        var exception = assertThrows(IllegalStateException.class,
                () -> jwtService.generateNewRefreshToken(mock(AccountUser.class), authHeader));
        var errMsg = "Invalid token, expiration is missing.";
        assertEquals(errMsg, exception.getMessage());
    }

    @Test
    void generateNewRefreshTokenTest_needsRefresh(){

        var authHeader = "header";
        when(jwtConfigurationProperties.getRefreshExpirationMinutes())
                .thenReturn(Integer.MAX_VALUE);

        try(var jwtUtils = mockStatic(JwtUtils.class)){
            jwtUtils.when(() -> JwtUtils.extractExpirationFromHeader(anyString()))
                    .thenAnswer( a -> {
                       var exp = Date.from(Instant.now());
                       return Optional.of(exp);
                    });
            jwtUtils.when(() -> JwtUtils.generateToken(any(Date.class), any(Date.class),
                    any(JwtType.class), any(UUID.class)))
                    .thenReturn("refreshToken");

            var user = mock(AccountUser.class);
            var userId = UUID.randomUUID();
            when(user.getId()).thenReturn(userId);
            var result = jwtService.generateNewRefreshToken(user, authHeader);
            assertNotNull(result);
            assertEquals("refreshToken", result);
        }
    }

    @Test
    void generateNewRefreshTokenTest_notNeedRefresh(){

        var authHeader = "header";
        when(jwtConfigurationProperties.getRefreshExpirationMinutes())
                .thenReturn(Integer.MIN_VALUE);

        try(var jwtUtils = mockStatic(JwtUtils.class)){
            jwtUtils.when(() -> JwtUtils.extractExpirationFromHeader(anyString()))
                    .thenAnswer( a -> {
                        var exp = Date.from(Instant.now());
                        return Optional.of(exp);
                    });

            var result = jwtService.generateNewRefreshToken(mock(AccountUser.class), authHeader);
            assertNull(result);
            jwtUtils.verify(() -> JwtUtils.generateToken(any(Date.class), any(Date.class),
                            any(JwtType.class), any(UUID.class)), never());
        }
    }
}