package org.gmalliaris.rental.rooms.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.gmalliaris.rental.rooms.dto.JwtType;
import org.gmalliaris.rental.rooms.entity.AccountUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Objects;

@Service
public class JwtService {

    private static final String ISS = "rental-rooms-api";

    private static final SecretKey SIGN_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

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
        Objects.requireNonNull(user.getId());
        Objects.requireNonNull(type);

        var created = Instant.now();
        Instant expiration;
        if (type == JwtType.ACCESS){
            expiration = created.plusSeconds(ACCESS_DURATION_SECONDS);
        }
        else {
            expiration = created.plus(REFRESH_DURATION_MINUTES, ChronoUnit.MINUTES);
        }

        return Jwts.builder()
                .setIssuer(ISS)
                .setSubject(type.getValue())
                .setIssuedAt(Date.from(created))
                .setExpiration(Date.from(expiration))
                .setId(user.getId().toString())
                .signWith(SIGN_KEY)
                .compact();
    }

    public Claims parseToken(String token, JwtType type) throws JwtException {
        var result = Jwts.parserBuilder()
                .requireIssuer(ISS)
                .requireSubject(type.getValue())
                .setSigningKey(SIGN_KEY)
                .build()
                .parseClaimsJws(token);
        return result.getBody();
    }
}
