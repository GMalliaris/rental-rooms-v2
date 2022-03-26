package org.gmalliaris.rental.rooms.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.gmalliaris.rental.rooms.dto.JwtType;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.gmalliaris.rental.rooms.dto.JwtType.ACCESS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class JwtUtilsTest {

    @Test
    void extractClaimsTest_malformedToken(){

        var invalidToken = "invalid";
        var exception = assertThrows(JwtException.class,
                () -> JwtUtils.extractClaims(invalidToken, ACCESS));
        assertEquals(MalformedJwtException.class, exception.getClass());
    }

    @Test
    void extractClaimsTest_invalidSignature(){

        var iss = (String) ReflectionTestUtils.getField(JwtUtils.class, "ISS");
        var key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        var now = Instant.now();
        var exp = now.plusSeconds(600);

        var token = Jwts.builder()
                .setIssuer(iss)
                .setSubject(ACCESS.getValue())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .signWith(key)
                .compact();

        var exception = assertThrows(JwtException.class,
                () -> JwtUtils.extractClaims(token, ACCESS));
        assertEquals(SignatureException.class, exception.getClass());
    }

    @Test
    void extractClaimsTest_invalidIssuer(){

        var key = (SecretKey) ReflectionTestUtils.getField(JwtUtils.class, "SIGN_KEY");
        var now = Instant.now();
        var exp = now.plusSeconds(600);

        var token = Jwts.builder()
                .setIssuer("invalid")
                .setSubject(ACCESS.getValue())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .signWith(key)
                .compact();

        var exception = assertThrows(JwtException.class,
                () -> JwtUtils.extractClaims(token, ACCESS));
        assertEquals(IncorrectClaimException.class, exception.getClass());
    }

    @Test
    void extractClaimsTest_invalidSubject(){

        var key = (SecretKey) ReflectionTestUtils.getField(JwtUtils.class, "SIGN_KEY");
        var iss = (String) ReflectionTestUtils.getField(JwtUtils.class, "ISS");
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
                () -> JwtUtils.extractClaims(token, ACCESS));
        assertEquals(IncorrectClaimException.class, exception.getClass());
    }

    @Test
    void extractClaimsTest_expired(){

        var key = (SecretKey) ReflectionTestUtils.getField(JwtUtils.class, "SIGN_KEY");
        var iss = (String) ReflectionTestUtils.getField(JwtUtils.class, "ISS");
        var now = Instant.now();
        var exp = now.minusSeconds(600);

        var token = Jwts.builder()
                .setIssuer(iss)
                .setSubject(ACCESS.getValue())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .signWith(key)
                .compact();

        var exception = assertThrows(JwtException.class,
                () -> JwtUtils.extractClaims(token, ACCESS));
        assertEquals(ExpiredJwtException.class, exception.getClass());
    }

    @Test
    void extractClaimsTest(){

        var key = (SecretKey) ReflectionTestUtils.getField(JwtUtils.class, "SIGN_KEY");
        var iss = (String) ReflectionTestUtils.getField(JwtUtils.class, "ISS");
        var now = Instant.now();
        var exp = now.plusSeconds(600);
        var uuid = UUID.randomUUID();

        var token = Jwts.builder()
                .setIssuer(iss)
                .setSubject(ACCESS.getValue())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .setId(uuid.toString())
                .signWith(key)
                .compact();

        var result = JwtUtils.extractClaims(token, ACCESS);
        assertNotNull(result);
        var body = result.getBody();
        assertNotNull(result);
        assertEquals(iss, body.getIssuer());
        assertEquals(ACCESS.getValue(), body.getSubject());
        assertEquals(Date.from(now).toString(), body.getIssuedAt().toString());
        assertEquals(Date.from(exp).toString(), body.getExpiration().toString());
        assertEquals(uuid.toString(), body.getId());
    }

    @Test
    void generateTokenTest(){
        var now = Instant.now();
        var expiration = now.plusSeconds(120);
        var type = ACCESS;
        var email = "test@example.eg";

        var token = JwtUtils.generateToken(Date.from(now),
                Date.from(expiration), type, email);
        assertNotNull(token);

        var iss = ReflectionTestUtils.getField(JwtUtils.class, "ISS");
        var claims = JwtUtils.extractClaims(token, ACCESS);
        var body = claims.getBody();
        assertNotNull(body);
        assertEquals(iss, body.getIssuer());
        assertEquals(type.getValue(), body.getSubject());
        assertEquals(Date.from(now).toString(),
                body.getIssuedAt().toString());
        assertEquals(Date.from(expiration).toString(),
                body.getExpiration().toString());
        assertEquals(email, body.getId());
    }

    @Test
    void extractUserEmailFromTokenTest_nullBody(){

        var token = "random-token";
        var type = JwtType.REFRESH;
        try (var jwtUtils = mockStatic(JwtUtils.class)){
            var mockJws = mock(Jws.class);
            jwtUtils.when(() -> JwtUtils.extractClaims(anyString(), any(JwtType.class)))
                    .thenReturn(mockJws);
            jwtUtils.when(() -> JwtUtils.extractUserEmailFromToken(anyString(), any(JwtType.class)))
                    .thenCallRealMethod();

            var result = JwtUtils.extractUserEmailFromToken(token, type);
            assertNotNull(result);
            assertTrue(result.isEmpty());
            jwtUtils.verify(() -> JwtUtils.extractClaims(token, type));
        }
    }

    @Test
    void extractUserEmailFromTokenTest_nullClaimEmail(){

        var token = "random-token";
        var type = JwtType.REFRESH;
        try (var jwtUtils = mockStatic(JwtUtils.class)){
            var mockJws = mock(Jws.class);
            when(mockJws.getBody())
                    .thenReturn(mock(Claims.class));
            jwtUtils.when(() -> JwtUtils.extractClaims(anyString(), any(JwtType.class)))
                    .thenReturn(mockJws);
            jwtUtils.when(() -> JwtUtils.extractUserEmailFromToken(anyString(), any(JwtType.class)))
                    .thenCallRealMethod();

            var result = JwtUtils.extractUserEmailFromToken(token, type);
            assertNotNull(result);
            assertTrue(result.isEmpty());
            jwtUtils.verify(() -> JwtUtils.extractClaims(token, type));
        }
    }

    @Test
    void extractUserEmailFromTokenTest(){

        var token = "random-token";
        var type = JwtType.REFRESH;
        var email = "random@example.eg";
        try (var jwtUtils = mockStatic(JwtUtils.class)){
            var mockClaims = mock(Claims.class);
            when(mockClaims.getId())
                    .thenReturn(email);

            var mockJws = mock(Jws.class);
            when(mockJws.getBody())
                    .thenReturn(mockClaims);
            jwtUtils.when(() -> JwtUtils.extractClaims(anyString(), any(JwtType.class)))
                    .thenReturn(mockJws);
            jwtUtils.when(() -> JwtUtils.extractUserEmailFromToken(anyString(), any(JwtType.class)))
                    .thenCallRealMethod();

            var result = JwtUtils.extractUserEmailFromToken(token, type);
            assertNotNull(result);
            assertTrue(result.isPresent());
            assertEquals(email, result.get());
            jwtUtils.verify(() -> JwtUtils.extractClaims(token, type));
        }
    }

    @Test
    void extractUserEmailFromHeaderTest_nullHeader(){
        var result = JwtUtils.extractUserEmailFromHeader(null, ACCESS);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void extractUserEmailFromHeaderTest_invalidHeader(){
        var invalidHeader = "Bearer- test";
        var result = JwtUtils.extractUserEmailFromHeader(invalidHeader, ACCESS);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void extractUserEmailFromHeaderTest_throwsJwtException(){
        var invalidHeader = "Bearer test";

        try (var jwtUtils = mockStatic(JwtUtils.class)){

            jwtUtils.when(() -> JwtUtils.extractUserEmailFromToken(anyString(), any(JwtType.class)))
                    .thenThrow(JwtException.class);
            jwtUtils.when(() -> JwtUtils.extractUserEmailFromHeader(anyString(), any(JwtType.class)))
                    .thenCallRealMethod();

            var result = JwtUtils.extractUserEmailFromHeader(invalidHeader, ACCESS);
            assertNotNull(result);
            assertTrue(result.isEmpty());
            jwtUtils.verify(() -> JwtUtils.extractUserEmailFromToken("test", ACCESS));
        }
    }

    @Test
    void extractUserEmailFromHeaderTest(){
        var invalidHeader = "Bearer test";
        var email = "test@example.eg";

        try (var jwtUtils = mockStatic(JwtUtils.class)){

            jwtUtils.when(() -> JwtUtils.extractUserEmailFromToken(anyString(), any(JwtType.class)))
                    .thenReturn(Optional.of(email));
            jwtUtils.when(() -> JwtUtils.extractUserEmailFromHeader(anyString(), any(JwtType.class)))
                    .thenCallRealMethod();

            var result = JwtUtils.extractUserEmailFromHeader(invalidHeader, ACCESS);
            assertNotNull(result);
            assertTrue(result.isPresent());
            assertEquals(email, result.get());
            jwtUtils.verify(() -> JwtUtils.extractUserEmailFromToken("test", ACCESS));
        }
    }
}