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

@Service
public class JwtService {

    private final JwtConfigurationProperties jwtConfigurationProperties;

    public JwtService(JwtConfigurationProperties jwtConfigurationProperties) {
        this.jwtConfigurationProperties = jwtConfigurationProperties;
    }

    public String generateAccessToken(AccountUser user){
        return generateToken(user, JwtType.ACCESS);
    }

    public String generateRefreshToken(AccountUser user){
        return generateToken(user, JwtType.REFRESH);
    }

    private String generateToken(AccountUser user, JwtType type){

        Objects.requireNonNull(user);
        Objects.requireNonNull(user.getEmail());
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
                type, user.getEmail());
    }

    public String generateNewRefreshToken(AccountUser user, String authHeader){

        var expiration = JwtUtils.extractExpirationFromHeader(authHeader, JwtType.REFRESH)
                .orElseThrow(() -> {
                    var errMsg = "Invalid token, expiration is missing.";
                    throw new IllegalStateException(errMsg);
                });

        var now = Instant.now();

        var delay = 60;
        var diff = ChronoUnit.SECONDS.between(now, expiration.toInstant());
        if (diff + delay < jwtConfigurationProperties.getRefreshExpirationMinutes()){
            return generateToken(user, JwtType.REFRESH);
        }
        return null;
    }
}
