package org.gmalliaris.rental.rooms.service;

import org.gmalliaris.rental.rooms.dto.JwtType;
import org.gmalliaris.rental.rooms.entity.AccountUser;
import org.gmalliaris.rental.rooms.util.JwtUtils;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.temporal.ChronoUnit;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;

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
}