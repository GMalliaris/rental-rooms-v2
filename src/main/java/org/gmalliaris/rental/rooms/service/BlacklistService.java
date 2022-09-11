package org.gmalliaris.rental.rooms.service;

import io.jsonwebtoken.Claims;
import org.gmalliaris.rental.rooms.config.JwtConfigurationProperties;
import org.gmalliaris.rental.rooms.util.JwtUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
public class BlacklistService {

    private static final String TOKEN_GROUP_ENTRY_DUMMY_VALUE = "group";
    private final RedisTemplate<String, String> redisTemplate;
    private final JwtConfigurationProperties jwtConfigurationProperties;

    public BlacklistService(RedisTemplate<String, String> redisTemplate, JwtConfigurationProperties jwtConfigurationProperties) {
        this.redisTemplate = redisTemplate;
        this.jwtConfigurationProperties = jwtConfigurationProperties;
    }

    public void blacklistTokenGroup(String tokenGroupId) {
        Objects.requireNonNull(tokenGroupId);
        redisTemplate.opsForValue().set(tokenGroupId, TOKEN_GROUP_ENTRY_DUMMY_VALUE,
                jwtConfigurationProperties.getRefreshExpirationMinutes(), TimeUnit.MINUTES);
    }

    public boolean tokenWithClaimsIsBlackListed(Claims claims) {
        var tokenGroupId = JwtUtils.extractTokenGroupIdFromClaims(claims);
        Objects.requireNonNull(tokenGroupId);
        return TOKEN_GROUP_ENTRY_DUMMY_VALUE.equals(redisTemplate.opsForValue().get(tokenGroupId));
    }
}
