package org.gmalliaris.rental.rooms.service;

import org.gmalliaris.rental.rooms.config.JwtConfigurationProperties;
import org.gmalliaris.rental.rooms.dto.JwtType;
import org.gmalliaris.rental.rooms.entity.AccountUser;
import org.gmalliaris.rental.rooms.util.JwtUtils;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class JwtService {

    private final JwtConfigurationProperties jwtConfigurationProperties;
    private final BlacklistService blacklistService;

    public JwtService(JwtConfigurationProperties jwtConfigurationProperties, BlacklistService blacklistService) {
        this.jwtConfigurationProperties = jwtConfigurationProperties;
        this.blacklistService = blacklistService;
    }

    public String generateAccessToken(AccountUser user, String tokenGroupId){
        return generateToken(user, JwtType.ACCESS, tokenGroupId);
    }

//    public String generateAccessToken(AccountUser user){
//        return generateAccessToken(user, UUID.randomUUID().toString());
//    }
//
//    public String generateRefreshToken(AccountUser user){
//        return generateRefreshToken(user, UUID.randomUUID().toString());
//    }

    public String generateRefreshToken(AccountUser user, String tokenGroupId){
        return generateToken(user, JwtType.REFRESH, tokenGroupId);
    }

    private String generateToken(AccountUser user, JwtType type, String tokenGroupId){

        Objects.requireNonNull(user);
        Objects.requireNonNull(user.getId());
        Objects.requireNonNull(type);

        var created = Instant.now();
        Instant expiration;
        if (type == JwtType.ACCESS){
            expiration = created.plusSeconds(jwtConfigurationProperties.getAccessExpirationSeconds());
        }
        else {
            expiration = created.plus(jwtConfigurationProperties.getRefreshExpirationMinutes(), ChronoUnit.MINUTES);
        }

        return JwtUtils.generateToken(Date.from(created), Date.from(expiration),
                type, user.getId(), tokenGroupId);
    }

    public Optional<String> generateRefreshTokenIfNeeded(AccountUser user, Date expiration, String tokenGroupId ){

        var nowInstant = Instant.now();
        var expirationInstant = expiration.toInstant();

        var expiresInSeconds = ChronoUnit.SECONDS.between(nowInstant, expirationInstant);
        var refreshExpirationThresholdSeconds = jwtConfigurationProperties.getRefreshExpirationThresholdSeconds();

        if (expiresInSeconds > refreshExpirationThresholdSeconds) {
            return Optional.empty();
        }

        blacklistService.blacklistTokenGroup(tokenGroupId);
        return Optional.of(generateRefreshToken(user, UUID.randomUUID().toString()));
    }
}
