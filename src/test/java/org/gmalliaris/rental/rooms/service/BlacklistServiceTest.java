package org.gmalliaris.rental.rooms.service;

import io.jsonwebtoken.Claims;
import org.gmalliaris.rental.rooms.config.JwtConfigurationProperties;
import org.gmalliaris.rental.rooms.util.JwtUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class BlacklistServiceTest {
    private static final String TOKEN_GROUP_ENTRY_DUMMY_VALUE = "group";

    @InjectMocks
    private BlacklistService blacklistService;

    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @Mock
    private JwtConfigurationProperties jwtConfigurationProperties;

    @Test
    void blacklistTokenWithClaimsTest() {
        var tokenGroupId = UUID.randomUUID().toString();
        var timeout = 60;
        var mockOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue())
                .thenReturn(mockOps);

        when(jwtConfigurationProperties.getRefreshExpirationMinutes())
                .thenReturn(timeout);

        blacklistService.blacklistTokenGroup(tokenGroupId);
        verify(redisTemplate).opsForValue();
        verify(mockOps).set(tokenGroupId, TOKEN_GROUP_ENTRY_DUMMY_VALUE, timeout, TimeUnit.MINUTES);
    }

    @Test
    void tokenWithClaimsIsBlackListedTest_isBlacklisted() {
        var mockClaims = mock(Claims.class);
        var tokenGroupId = UUID.randomUUID().toString();
        try (var jwtUtils = mockStatic(JwtUtils.class)) {
            jwtUtils.when(() -> JwtUtils.extractTokenGroupIdFromClaims(any(Claims.class)))
                    .thenReturn(tokenGroupId);

            var mockOps = mock(ValueOperations.class);
            when(mockOps.get(tokenGroupId))
                    .thenReturn(TOKEN_GROUP_ENTRY_DUMMY_VALUE);
            when(redisTemplate.opsForValue())
                    .thenReturn(mockOps);

            var result = blacklistService.tokenWithClaimsIsBlackListed(mockClaims);
            assertTrue(result);
            verify(mockOps).get(tokenGroupId);
            verify(redisTemplate).opsForValue();

        }
    }

    @Test
    void tokenWithClaimsIsBlackListedTest_isNotBlacklisted() {
        var mockClaims = mock(Claims.class);
        var tokenGroupId = UUID.randomUUID().toString();
        try (var jwtUtils = mockStatic(JwtUtils.class)) {
            jwtUtils.when(() -> JwtUtils.extractTokenGroupIdFromClaims(any(Claims.class)))
                    .thenReturn(tokenGroupId);

            var mockOps = mock(ValueOperations.class);
            when(mockOps.get(tokenGroupId))
                    .thenReturn("NOT_" + TOKEN_GROUP_ENTRY_DUMMY_VALUE);
            when(redisTemplate.opsForValue())
                    .thenReturn(mockOps);

            var result = blacklistService.tokenWithClaimsIsBlackListed(mockClaims);
            assertFalse(result);
            verify(mockOps).get(tokenGroupId);
            verify(redisTemplate).opsForValue();

        }
    }
}