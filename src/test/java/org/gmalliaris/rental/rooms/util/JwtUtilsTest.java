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
import java.util.Map;
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
                () -> JwtUtils.extractClaims(invalidToken));
        assertEquals(MalformedJwtException.class, exception.getClass());
    }

    @Test
    void extractClaimsTest_invalidSignature(){

        var iss = (String) ReflectionTestUtils.getField(JwtUtils.class, "ISS");
        var key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        var now = Instant.now();
        var exp = now.plusSeconds(600);
        var userId = UUID.randomUUID();

        var token = Jwts.builder()
                .setClaims(Map.of("tgid", UUID.randomUUID().toString()))
                .setIssuer(iss)
                .setAudience(iss)
                .setSubject(userId.toString())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .setId(String.format("%s_%s", ACCESS.getValue(), UUID.randomUUID()))
                .signWith(key)
                .compact();

        var exception = assertThrows(JwtException.class,
                () -> JwtUtils.extractClaims(token));
        assertEquals(SignatureException.class, exception.getClass());
    }

    @Test
    void extractClaimsTest_invalidIssuer(){

        var iss = (String) ReflectionTestUtils.getField(JwtUtils.class, "ISS");
        var key = (SecretKey) ReflectionTestUtils.getField(JwtUtils.class, "SIGN_KEY");
        var now = Instant.now();
        var exp = now.plusSeconds(600);
        var userId = UUID.randomUUID();

        var token = Jwts.builder()
                .setClaims(Map.of("tgid", UUID.randomUUID().toString()))
                .setIssuer("invalid")
                .setAudience(iss)
                .setSubject(userId.toString())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .setId(String.format("%s_%s", ACCESS.getValue(), UUID.randomUUID()))
                .signWith(key)
                .compact();

        var exception = assertThrows(JwtException.class,
                () -> JwtUtils.extractClaims(token));
        assertEquals(IncorrectClaimException.class, exception.getClass());
    }

    @Test
    void extractClaimsTest_invalidAudience(){

        var iss = (String) ReflectionTestUtils.getField(JwtUtils.class, "ISS");
        var key = (SecretKey) ReflectionTestUtils.getField(JwtUtils.class, "SIGN_KEY");
        var now = Instant.now();
        var exp = now.plusSeconds(600);
        var userId = UUID.randomUUID();

        var token = Jwts.builder()
                .setClaims(Map.of("tgid", UUID.randomUUID().toString()))
                .setIssuer(iss)
                .setAudience("invalid")
                .setSubject(userId.toString())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .setId(String.format("%s_%s", ACCESS.getValue(), UUID.randomUUID()))
                .signWith(key)
                .compact();

        var exception = assertThrows(JwtException.class,
                () -> JwtUtils.extractClaims(token));
        assertEquals(IncorrectClaimException.class, exception.getClass());
    }

    @Test
    void extractClaimsTest_expired(){

        var key = (SecretKey) ReflectionTestUtils.getField(JwtUtils.class, "SIGN_KEY");
        var iss = (String) ReflectionTestUtils.getField(JwtUtils.class, "ISS");
        var now = Instant.now();
        var exp = now.minusSeconds(600);
        var userId = UUID.randomUUID();

        var token = Jwts.builder()
                .setClaims(Map.of("tgid", UUID.randomUUID().toString()))
                .setIssuer(iss)
                .setAudience("invalid")
                .setSubject(userId.toString())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .setId(String.format("%s_%s", ACCESS.getValue(), UUID.randomUUID()))
                .signWith(key)
                .compact();

        var exception = assertThrows(JwtException.class,
                () -> JwtUtils.extractClaims(token));
        assertEquals(ExpiredJwtException.class, exception.getClass());
    }

    @Test
    void generateTokenAndExtractClaimsTest(){
        var now = Instant.now();
        var expiration = now.plusSeconds(120);
        var type = ACCESS;
        var userId = UUID.randomUUID();

        var token = JwtUtils.generateToken(Date.from(now), Date.from(expiration),
                type, userId);
        assertNotNull(token);

        var claims = JwtUtils.extractClaims(token);
        assertNotNull(claims);
        var body = claims.getBody();
        assertNotNull(body);
        assertEquals(String.format("%s_%s", type.getValue(), userId), body.getSubject());
        var subject = body.getSubject();
        var subjectComponents = subject.split("_");
        assertEquals(2, subjectComponents.length);
        assertEquals(type.getValue(), subjectComponents[0]);
        assertTrue(CommonUtils.uuidFromString(subjectComponents[1]).isPresent());
        var tokenGroupId = body.get("tgid", String.class);
        assertTrue(CommonUtils.uuidFromString(tokenGroupId).isPresent());
    }

    @Test
    void extractUserIdFromTokenTest_nullBody(){

        var token = "random-token";
        var type = JwtType.REFRESH;
        try (var jwtUtils = mockStatic(JwtUtils.class)){
            var mockJws = mock(Jws.class);
            jwtUtils.when(() -> JwtUtils.extractClaims(anyString()))
                    .thenReturn(mockJws);
            jwtUtils.when(() -> JwtUtils.extractUserIdFromToken(anyString(), any(JwtType.class)))
                    .thenCallRealMethod();

            var result = JwtUtils.extractUserIdFromToken(token, type);
            assertNotNull(result);
            assertTrue(result.isEmpty());
            jwtUtils.verify(() -> JwtUtils.extractClaims(token));
        }
    }

    @Test
    void extractUserIdFromTokenTest_nullClaimEmail(){

        var token = "random-token";
        var type = JwtType.REFRESH;
        try (var jwtUtils = mockStatic(JwtUtils.class)){
            var mockJws = mock(Jws.class);
            when(mockJws.getBody())
                    .thenReturn(mock(Claims.class));
            jwtUtils.when(() -> JwtUtils.extractClaims(anyString()))
                    .thenReturn(mockJws);
            jwtUtils.when(() -> JwtUtils.extractUserIdFromToken(anyString(), any(JwtType.class)))
                    .thenCallRealMethod();

            var result = JwtUtils.extractUserIdFromToken(token, type);
            assertNotNull(result);
            assertTrue(result.isEmpty());
            jwtUtils.verify(() -> JwtUtils.extractClaims(token));
        }
    }

    @Test
    void extractUserIdFromTokenTest_subjectMalformed(){

        var token = "random-token";
        var type = JwtType.REFRESH;
        var subject = "blah";
        try (var jwtUtils = mockStatic(JwtUtils.class)){
            var mockClaims = mock(Claims.class);
            when(mockClaims.getSubject())
                    .thenReturn(subject);

            var mockJws = mock(Jws.class);
            when(mockJws.getBody())
                    .thenReturn(mockClaims);
            jwtUtils.when(() -> JwtUtils.extractClaims(anyString()))
                    .thenReturn(mockJws);
            jwtUtils.when(() -> JwtUtils.extractUserIdFromToken(anyString(), any(JwtType.class)))
                    .thenCallRealMethod();

            var result = JwtUtils.extractUserIdFromToken(token, type);
            assertNotNull(result);
            assertTrue(result.isEmpty());
            jwtUtils.verify(() -> JwtUtils.extractClaims(token));
        }
    }

    @Test
    void extractUserIdFromTokenTest_invalidTokenIdPrefix(){

        var token = "random-token";
        var type = JwtType.REFRESH;
        var userId = UUID.randomUUID();
        var subject = String.format("%s_%s", ACCESS.getValue(), userId);
        try (var jwtUtils = mockStatic(JwtUtils.class)){
            var mockClaims = mock(Claims.class);
            when(mockClaims.getSubject())
                    .thenReturn(subject);

            var mockJws = mock(Jws.class);
            when(mockJws.getBody())
                    .thenReturn(mockClaims);
            jwtUtils.when(() -> JwtUtils.extractClaims(anyString()))
                    .thenReturn(mockJws);
            jwtUtils.when(() -> JwtUtils.extractUserIdFromToken(anyString(), any(JwtType.class)))
                    .thenCallRealMethod();

            var result = JwtUtils.extractUserIdFromToken(token, type);
            assertNotNull(result);
            assertTrue(result.isEmpty());
            jwtUtils.verify(() -> JwtUtils.extractClaims(token));
        }
    }

    @Test
    void extractUserIdFromTokenTest_invalidTokenId(){

        var token = "random-token";
        var type = JwtType.REFRESH;
        var subject = "blah";
        try (var jwtUtils = mockStatic(JwtUtils.class)){
            var mockClaims = mock(Claims.class);
            when(mockClaims.getSubject())
                    .thenReturn(subject);

            var mockJws = mock(Jws.class);
            when(mockJws.getBody())
                    .thenReturn(mockClaims);
            jwtUtils.when(() -> JwtUtils.extractClaims(anyString()))
                    .thenReturn(mockJws);
            jwtUtils.when(() -> JwtUtils.extractUserIdFromToken(anyString(), any(JwtType.class)))
                    .thenCallRealMethod();

            var result = JwtUtils.extractUserIdFromToken(token, type);
            assertNotNull(result);
            assertTrue(result.isEmpty());
            jwtUtils.verify(() -> JwtUtils.extractClaims(token));
        }
    }

    @Test
    void extractUserIdFromTokenTest(){

        var token = "random-token";
        var type = JwtType.REFRESH;
        var userId = UUID.randomUUID();
        try (var jwtUtils = mockStatic(JwtUtils.class)){
            var mockClaims = mock(Claims.class);
            when(mockClaims.getSubject())
                    .thenReturn(String.format("%s_%s", type.getValue(), userId));

            var mockJws = mock(Jws.class);
            when(mockJws.getBody())
                    .thenReturn(mockClaims);
            jwtUtils.when(() -> JwtUtils.extractClaims(anyString()))
                    .thenReturn(mockJws);
            jwtUtils.when(() -> JwtUtils.extractUserIdFromToken(anyString(), any(JwtType.class)))
                    .thenCallRealMethod();

            var result = JwtUtils.extractUserIdFromToken(token, type);
            assertNotNull(result);
            assertTrue(result.isPresent());
            assertEquals(userId, result.get());
            jwtUtils.verify(() -> JwtUtils.extractClaims(token));
        }
    }

    @Test
    void extractUserIdFromHeaderTest_nullHeader(){
        var result = JwtUtils.extractUserIdFromHeader(null, ACCESS);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void extractUserIdFromHeaderTest_invalidHeader(){
        var invalidHeader = "Bearer- test";
        var result = JwtUtils.extractUserIdFromHeader(invalidHeader, ACCESS);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void extractUserIdFromHeaderTest_throwsJwtException(){
        var invalidHeader = "Bearer test";

        try (var jwtUtils = mockStatic(JwtUtils.class)){

            jwtUtils.when(() -> JwtUtils.extractUserIdFromToken(anyString(), any(JwtType.class)))
                    .thenThrow(JwtException.class);
            jwtUtils.when(() -> JwtUtils.extractUserIdFromHeader(anyString(), any(JwtType.class)))
                    .thenCallRealMethod();

            var result = JwtUtils.extractUserIdFromHeader(invalidHeader, ACCESS);
            assertNotNull(result);
            assertTrue(result.isEmpty());
            jwtUtils.verify(() -> JwtUtils.extractUserIdFromToken("test", ACCESS));
        }
    }

    @Test
    void extractUserIdFromHeaderTest(){
        var invalidHeader = "Bearer test";
        var userId = UUID.randomUUID();

        try (var jwtUtils = mockStatic(JwtUtils.class)){

            jwtUtils.when(() -> JwtUtils.extractUserIdFromToken(anyString(), any(JwtType.class)))
                    .thenReturn(Optional.of(userId));
            jwtUtils.when(() -> JwtUtils.extractUserIdFromHeader(anyString(), any(JwtType.class)))
                    .thenCallRealMethod();

            var result = JwtUtils.extractUserIdFromHeader(invalidHeader, ACCESS);
            assertNotNull(result);
            assertTrue(result.isPresent());
            assertEquals(userId, result.get());
            jwtUtils.verify(() -> JwtUtils.extractUserIdFromToken("test", ACCESS));
        }
    }

    @Test
    void extractExpirationFromTokenTest_nullBody(){

        var token = "random-token";
        var type = JwtType.REFRESH;
        try (var jwtUtils = mockStatic(JwtUtils.class)){
            var mockJws = mock(Jws.class);
            jwtUtils.when(() -> JwtUtils.extractClaims(anyString()))
                    .thenReturn(mockJws);
            jwtUtils.when(() -> JwtUtils.extractExpirationFromToken(anyString(), any(JwtType.class)))
                    .thenCallRealMethod();

            var result = JwtUtils.extractExpirationFromToken(token, type);
            assertNotNull(result);
            assertTrue(result.isEmpty());
            jwtUtils.verify(() -> JwtUtils.extractClaims(token));
        }
    }

    @Test
    void extractExpirationFromTokenTest_nullClaimEmail(){

        var token = "random-token";
        var type = JwtType.REFRESH;
        try (var jwtUtils = mockStatic(JwtUtils.class)){
            var mockJws = mock(Jws.class);
            when(mockJws.getBody())
                    .thenReturn(mock(Claims.class));
            jwtUtils.when(() -> JwtUtils.extractClaims(anyString()))
                    .thenReturn(mockJws);
            jwtUtils.when(() -> JwtUtils.extractExpirationFromToken(anyString(), any(JwtType.class)))
                    .thenCallRealMethod();

            var result = JwtUtils.extractExpirationFromToken(token, type);
            assertNotNull(result);
            assertTrue(result.isEmpty());
            jwtUtils.verify(() -> JwtUtils.extractClaims(token));
        }
    }

    @Test
    void extractExpirationFromTokenTest(){

        var token = "random-token";
        var type = JwtType.REFRESH;
        var now = Date.from(Instant.now());
        try (var jwtUtils = mockStatic(JwtUtils.class)){
            var mockClaims = mock(Claims.class);
            when(mockClaims.getExpiration())
                    .thenReturn(now);

            var mockJws = mock(Jws.class);
            when(mockJws.getBody())
                    .thenReturn(mockClaims);
            jwtUtils.when(() -> JwtUtils.extractClaims(anyString()))
                    .thenReturn(mockJws);
            jwtUtils.when(() -> JwtUtils.extractExpirationFromToken(anyString(), any(JwtType.class)))
                    .thenCallRealMethod();

            var result = JwtUtils.extractExpirationFromToken(token, type);
            assertNotNull(result);
            assertTrue(result.isPresent());
            assertEquals(now, result.get());
            jwtUtils.verify(() -> JwtUtils.extractClaims(token));
        }
    }

    @Test
    void extractExpirationFromHeaderTest_nullHeader(){
        var result = JwtUtils.extractExpirationFromHeader(null, ACCESS);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void extractExpirationFromHeaderTest_invalidHeader(){
        var invalidHeader = "Bearer- test";
        var result = JwtUtils.extractExpirationFromHeader(invalidHeader, ACCESS);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void extractExpirationFromHeaderTest_throwsJwtException(){
        var invalidHeader = "Bearer test";

        try (var jwtUtils = mockStatic(JwtUtils.class)){

            jwtUtils.when(() -> JwtUtils.extractExpirationFromToken(anyString(), any(JwtType.class)))
                    .thenThrow(JwtException.class);
            jwtUtils.when(() -> JwtUtils.extractExpirationFromHeader(anyString(), any(JwtType.class)))
                    .thenCallRealMethod();

            var result = JwtUtils.extractExpirationFromHeader(invalidHeader, ACCESS);
            assertNotNull(result);
            assertTrue(result.isEmpty());
            jwtUtils.verify(() -> JwtUtils.extractExpirationFromToken("test", ACCESS));
        }
    }

    @Test
    void extractExpirationFromHeaderTest(){
        var invalidHeader = "Bearer test";
        var now = Date.from(Instant.now());

        try (var jwtUtils = mockStatic(JwtUtils.class)){

            jwtUtils.when(() -> JwtUtils.extractExpirationFromToken(anyString(), any(JwtType.class)))
                    .thenReturn(Optional.of(now));
            jwtUtils.when(() -> JwtUtils.extractExpirationFromHeader(anyString(), any(JwtType.class)))
                    .thenCallRealMethod();

            var result = JwtUtils.extractExpirationFromHeader(invalidHeader, ACCESS);
            assertNotNull(result);
            assertTrue(result.isPresent());
            assertEquals(now, result.get());
            jwtUtils.verify(() -> JwtUtils.extractExpirationFromToken("test", ACCESS));
        }
    }
}