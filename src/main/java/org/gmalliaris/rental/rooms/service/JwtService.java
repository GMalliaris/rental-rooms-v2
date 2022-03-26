package org.gmalliaris.rental.rooms.service;

import org.gmalliaris.rental.rooms.dto.JwtType;
import org.gmalliaris.rental.rooms.entity.AccountUser;
import org.gmalliaris.rental.rooms.util.JwtUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Objects;

@Service
public class JwtService {

    @Value("${jwt.access.expiration.seconds: 120}")
    private int ACCESS_DURATION_SECONDS;

    @Value("${jwt.refresh.expiration.minutes: 60}")
    private int REFRESH_DURATION_MINUTES;

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
            expiration = created.plusSeconds(ACCESS_DURATION_SECONDS);
        }
        else {
            expiration = created.plus(REFRESH_DURATION_MINUTES, ChronoUnit.MINUTES);
        }

        return JwtUtils.generateToken(Date.from(created), Date.from(expiration),
                type, user.getEmail());
    }
}
