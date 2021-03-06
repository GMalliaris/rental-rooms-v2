package org.gmalliaris.rental.rooms.service;

import org.gmalliaris.rental.rooms.dto.JwtType;
import org.gmalliaris.rental.rooms.entity.AccountUser;
import org.gmalliaris.rental.rooms.util.JwtUtils;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService();

    @Test
    void generateAccessTokenTest() {

        var accessTokenDuration = 120;
        ReflectionTestUtils.setField(jwtService, "ACCESS_DURATION_SECONDS", accessTokenDuration);
        var accountUser = new AccountUser();
        accountUser.setEmail("random@example.eg");

        try (var jwtUtils = mockStatic(JwtUtils.class)) {
            jwtUtils.when(() -> JwtUtils.generateToken(any(Date.class),
                            any(Date.class), any(JwtType.class), anyString()))
                    .thenReturn("generatedToken");

            var token = jwtService.generateAccessToken(accountUser);
            assertNotNull(token);
            assertEquals("generatedToken", token);

            var dateCapturer = ArgumentCaptor.forClass(Date.class);
            var typeCapturer = ArgumentCaptor.forClass(JwtType.class);
            var stringCapturer = ArgumentCaptor.forClass(String.class);
            jwtUtils.verify(() -> JwtUtils.generateToken(
                    dateCapturer.capture(), dateCapturer.capture(),
                    typeCapturer.capture(), stringCapturer.capture()));
            var dates = dateCapturer.getAllValues();
            var issuedAt = dates.get(0);
            var expiration = dates.get(1);
            var type = typeCapturer.getValue();
            var email = stringCapturer.getValue();
            assertNotNull(issuedAt);
            assertNotNull(expiration);
            var diff = ChronoUnit.SECONDS.between(issuedAt.toInstant(), expiration.toInstant());
            assertEquals(accessTokenDuration, diff);
            assertEquals("random@example.eg", email);
            assertEquals(JwtType.ACCESS, type);
        }
    }

    @Test
    void generateRefreshTokenTest() {

        var refreshTokenDuration = 60;
        ReflectionTestUtils.setField(jwtService, "REFRESH_DURATION_MINUTES", refreshTokenDuration);
        var accountUser = new AccountUser();
        accountUser.setEmail("random@example.eg");

        try (var jwtUtils = mockStatic(JwtUtils.class)) {
            jwtUtils.when(() -> JwtUtils.generateToken(any(Date.class),
                            any(Date.class), any(JwtType.class), anyString()))
                    .thenReturn("generatedToken");

            var token = jwtService.generateRefreshToken(accountUser);
            assertNotNull(token);
            assertEquals("generatedToken", token);

            var dateCapturer = ArgumentCaptor.forClass(Date.class);
            var typeCapturer = ArgumentCaptor.forClass(JwtType.class);
            var stringCapturer = ArgumentCaptor.forClass(String.class);
            jwtUtils.verify(() -> JwtUtils.generateToken(
                    dateCapturer.capture(), dateCapturer.capture(),
                    typeCapturer.capture(), stringCapturer.capture()));
            var dates = dateCapturer.getAllValues();
            var issuedAt = dates.get(0);
            var expiration = dates.get(1);
            var type = typeCapturer.getValue();
            var email = stringCapturer.getValue();
            assertNotNull(issuedAt);
            assertNotNull(expiration);
            var diff = ChronoUnit.MINUTES.between(issuedAt.toInstant(), expiration.toInstant());
            assertEquals(refreshTokenDuration, diff);
            assertEquals("random@example.eg", email);
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
        ReflectionTestUtils.setField(jwtService, "ACCESS_DURATION_SECONDS", Integer.MAX_VALUE);

        try(var jwtUtils = mockStatic(JwtUtils.class)){
            jwtUtils.when(() -> JwtUtils.extractExpirationFromHeader(anyString(), any(JwtType.class)))
                    .thenAnswer( a -> {
                       var exp = Date.from(Instant.now());
                       return Optional.of(exp);
                    });
            jwtUtils.when(() -> JwtUtils.generateToken(any(Date.class), any(Date.class),
                    any(JwtType.class), anyString()))
                    .thenReturn("refreshToken");

            var user = mock(AccountUser.class);
            when(user.getEmail()).thenReturn("email@example.eg");
            var result = jwtService.generateNewRefreshToken(user, authHeader);
            assertNotNull(result);
            assertEquals("refreshToken", result);
        }
    }

    @Test
    void generateNewRefreshTokenTest_notNeedRefresh(){

        var authHeader = "header";
        ReflectionTestUtils.setField(jwtService, "ACCESS_DURATION_SECONDS", Integer.MIN_VALUE);

        try(var jwtUtils = mockStatic(JwtUtils.class)){
            jwtUtils.when(() -> JwtUtils.extractExpirationFromHeader(anyString(), any(JwtType.class)))
                    .thenAnswer( a -> {
                        var exp = Date.from(Instant.now());
                        return Optional.of(exp);
                    });

            var result = jwtService.generateNewRefreshToken(mock(AccountUser.class), authHeader);
            assertNull(result);
            jwtUtils.verify(() -> JwtUtils.generateToken(any(Date.class), any(Date.class),
                            any(JwtType.class), anyString()), never());
        }
    }
}