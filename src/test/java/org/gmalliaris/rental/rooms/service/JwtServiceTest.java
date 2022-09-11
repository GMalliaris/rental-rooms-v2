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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    @Mock
    private JwtConfigurationProperties jwtConfigurationProperties;
    @Mock
    private BlacklistService blacklistService;

    @Test
    void generateAccessTokenTest() {

        var accessTokenDuration = 120;
        when(jwtConfigurationProperties.getAccessExpirationSeconds())
                .thenReturn(accessTokenDuration);

        var accountUser = new AccountUser();
        accountUser.setId(UUID.randomUUID());

        try (var jwtUtils = mockStatic(JwtUtils.class)) {
            jwtUtils.when(() -> JwtUtils.generateToken(any(Date.class),
                            any(Date.class), any(JwtType.class), any(UUID.class), anyString()))
                    .thenReturn("generatedToken");

            var token = jwtService.generateAccessToken(accountUser, UUID.randomUUID().toString());
            assertNotNull(token);
            assertEquals("generatedToken", token);

            var dateCapturer = ArgumentCaptor.forClass(Date.class);
            var typeCapturer = ArgumentCaptor.forClass(JwtType.class);
            var uuidCapturer = ArgumentCaptor.forClass(UUID.class);
            jwtUtils.verify(() -> JwtUtils.generateToken(
                    dateCapturer.capture(), dateCapturer.capture(),
                    typeCapturer.capture(), uuidCapturer.capture(), anyString()));
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
                            any(Date.class), any(JwtType.class),
                            any(UUID.class), anyString()))
                    .thenReturn("generatedToken");

            var tgid = UUID.randomUUID();
            var token = jwtService.generateRefreshToken(accountUser, tgid.toString());
            assertNotNull(token);
            assertEquals("generatedToken", token);

            var dateCapturer = ArgumentCaptor.forClass(Date.class);
            var typeCapturer = ArgumentCaptor.forClass(JwtType.class);
            var uuidCapturer = ArgumentCaptor.forClass(UUID.class);
            var tgidCapturer = ArgumentCaptor.forClass(String.class);
            jwtUtils.verify(() -> JwtUtils.generateToken(
                    dateCapturer.capture(), dateCapturer.capture(),
                    typeCapturer.capture(), uuidCapturer.capture(),
                    tgidCapturer.capture()));
            var dates = dateCapturer.getAllValues();
            var issuedAt = dates.get(0);
            var expiration = dates.get(1);
            var type = typeCapturer.getValue();
            var uuid = uuidCapturer.getValue();
            var tgidCaptured = tgidCapturer.getValue();
            assertNotNull(issuedAt);
            assertNotNull(expiration);
            var diff = ChronoUnit.MINUTES.between(issuedAt.toInstant(), expiration.toInstant());
            assertEquals(refreshTokenDuration, diff);
            assertEquals(accountUser.getId(), uuid);
            assertEquals(tgid.toString(), tgidCaptured);
            assertEquals(JwtType.REFRESH, type);
        }
    }

    @Test
    void generateRefreshTokenIfNeededTest_notNeeded() {

        var now = Instant.now();
        var expInstant = now.plusSeconds(60);

        when(jwtConfigurationProperties.getRefreshExpirationThresholdSeconds())
                .thenReturn(6);

        var result = jwtService.generateRefreshTokenIfNeeded(mock(AccountUser.class),
                Date.from(expInstant), UUID.randomUUID().toString());
        assertTrue(result.isEmpty());
        verifyNoInteractions(blacklistService);
    }

    @Test
    void generateRefreshTokenIfNeededTest_needed() {

        var now = Instant.now();
        var expInstant = now.plusSeconds(6);

        when(jwtConfigurationProperties.getRefreshExpirationThresholdSeconds())
                .thenReturn(60);

        when(jwtConfigurationProperties.getRefreshExpirationMinutes())
                .thenReturn(60);

        var user = new AccountUser();
        user.setId(UUID.randomUUID());
        var tokenGroup = UUID.randomUUID().toString();
        var result = jwtService.generateRefreshTokenIfNeeded(user,
                Date.from(expInstant), tokenGroup);
        assertFalse(result.isEmpty());
        verify(blacklistService).blacklistTokenGroup(tokenGroup);
    }

}