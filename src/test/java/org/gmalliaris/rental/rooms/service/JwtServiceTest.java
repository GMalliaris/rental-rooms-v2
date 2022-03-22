package org.gmalliaris.rental.rooms.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.gmalliaris.rental.rooms.dto.JwtType;
import org.gmalliaris.rental.rooms.entity.AccountUser;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService();

    @Test
    void generateAccessTokenTest(){

        ReflectionTestUtils.setField(jwtService, "ACCESS_DURATION_SECONDS", 120);
        var accountUser = new AccountUser();
        accountUser.setId(UUID.randomUUID());

        var token = jwtService.generateAccessToken(accountUser);
        assertNotNull(token);
    }

    @Test
    void generateRefreshTokenTest(){

        ReflectionTestUtils.setField(jwtService, "REFRESH_DURATION_MINUTES", 120);
        var accountUser = new AccountUser();
        accountUser.setId(UUID.randomUUID());

        var token = jwtService.generateRefreshToken(accountUser);
        assertNotNull(token);
    }

    @Test
    void parseTokenTest_malformedToken(){

        var invalidToken = "invalid";
        var exception = assertThrows(JwtException.class,
                () -> jwtService.parseToken(invalidToken, JwtType.ACCESS));
        assertEquals(MalformedJwtException.class, exception.getClass());
    }

    @Test
    void parseTokenTest_invalidSignature(){

        var iss = (String) ReflectionTestUtils.getField(jwtService, "ISS");
        var key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        var now = Instant.now();
        var exp = now.plusSeconds(600);

        var token = Jwts.builder()
                .setIssuer(iss)
                .setSubject(JwtType.ACCESS.getValue())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .signWith(key)
                .compact();

        var exception = assertThrows(JwtException.class,
                () -> jwtService.parseToken(token, JwtType.ACCESS));
        assertEquals(SignatureException.class, exception.getClass());
    }

    @Test
    void parseTokenTest_invalidIssuer(){

        var key = (SecretKey) ReflectionTestUtils.getField(jwtService, "SIGN_KEY");
        var now = Instant.now();
        var exp = now.plusSeconds(600);

        var token = Jwts.builder()
                .setIssuer("invalid")
                .setSubject(JwtType.ACCESS.getValue())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .signWith(key)
                .compact();

        var exception = assertThrows(JwtException.class,
                () -> jwtService.parseToken(token, JwtType.ACCESS));
        assertEquals(IncorrectClaimException.class, exception.getClass());
    }

    @Test
    void parseTokenTest_invalidSubject(){

        var key = (SecretKey) ReflectionTestUtils.getField(jwtService, "SIGN_KEY");
        var iss = (String) ReflectionTestUtils.getField(jwtService, "ISS");
        var now = Instant.now();
        var exp = now.plusSeconds(600);

        var token = Jwts.builder()
                .setIssuer(iss)
                .setSubject(JwtType.REFRESH.getValue())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .signWith(key)
                .compact();

        var exception = assertThrows(JwtException.class,
                () -> jwtService.parseToken(token, JwtType.ACCESS));
        assertEquals(IncorrectClaimException.class, exception.getClass());
    }

    @Test
    void parseTokenTest_expired(){

        var key = (SecretKey) ReflectionTestUtils.getField(jwtService, "SIGN_KEY");
        var iss = (String) ReflectionTestUtils.getField(jwtService, "ISS");
        var now = Instant.now();
        var exp = now.minusSeconds(600);

        var token = Jwts.builder()
                .setIssuer(iss)
                .setSubject(JwtType.ACCESS.getValue())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .signWith(key)
                .compact();

        var exception = assertThrows(JwtException.class,
                () -> jwtService.parseToken(token, JwtType.ACCESS));
        assertEquals(ExpiredJwtException.class, exception.getClass());
    }

    @Test
    void parseTokenTest(){

        var key = (SecretKey) ReflectionTestUtils.getField(jwtService, "SIGN_KEY");
        var iss = (String) ReflectionTestUtils.getField(jwtService, "ISS");
        var now = Instant.now();
        var exp = now.plusSeconds(600);
        var uuid = UUID.randomUUID();

        var token = Jwts.builder()
                .setIssuer(iss)
                .setSubject(JwtType.ACCESS.getValue())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .setId(uuid.toString())
                .signWith(key)
                .compact();

        var result = jwtService.parseToken(token, JwtType.ACCESS);
        assertNotNull(result);
        assertEquals(iss, result.getIssuer());
        assertEquals(JwtType.ACCESS.getValue(), result.getSubject());
        assertEquals(Date.from(now).toString(), result.getIssuedAt().toString());
        assertEquals(Date.from(exp).toString(), result.getExpiration().toString());
        assertEquals(uuid.toString(), result.getId());
    }

}