package org.gmalliaris.rental.rooms.util;

import io.jsonwebtoken.*;
import org.gmalliaris.rental.rooms.dto.JwtType;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtUtilsTest {

    @Test
    void generateTokenTest() {

        var issued = Date.from(Instant.now());
        var exp = Date.from(Instant.now().plusSeconds(20));
        var userId = UUID.randomUUID();
        var tokenGroupId = UUID.randomUUID().toString();

        var token = JwtUtils.generateToken(issued, exp,
                JwtType.ACCESS, userId, tokenGroupId);

        var validClaims = JwtUtils.extractValidClaimsFromToken(token, JwtType.ACCESS);
        assertNotNull(validClaims);
        assertTrue(validClaims.isPresent());
    }

    @Test
    void extractValidClaimsFromHeaderTest_headerIsNull() {

        var claims = JwtUtils.extractValidClaimsFromHeader(null, JwtType.ACCESS);
        assertNotNull(claims);
        assertTrue(claims.isEmpty());
    }

    @Test
    void extractValidClaimsFromHeaderTest_headerIsInvalid() {

        var header = "blah smth";
        var claims = JwtUtils.extractValidClaimsFromHeader(header, JwtType.ACCESS);
        assertNotNull(claims);
        assertTrue(claims.isEmpty());
    }

    @Test
    void extractValidClaimsFromHeaderTest_tokenIsInvalid() {

        var header = "Bearer invalid";
        var claims = JwtUtils.extractValidClaimsFromHeader(header, JwtType.ACCESS);
        assertNotNull(claims);
        assertTrue(claims.isEmpty());
    }

    @Test
    void extractValidClaimsFromTokenTest_tokenIdIsInvalid() {

        try(var jwtsUtil = mockStatic(Jwts.class);
            var commonUtil = mockStatic(CommonUtils.class)) {
            commonUtil.when(() -> CommonUtils.uuidFromString(nullable(String.class)))
                    .thenReturn(Optional.empty());

            var mockClaims = mock(Claims.class);
            var jwsClaims = mock(Jws.class);
            when(jwsClaims.getBody())
                    .thenReturn(mockClaims);
            var mockParser = mock(JwtParser.class);
            when(mockParser.parseClaimsJws(anyString()))
                    .thenReturn(jwsClaims);
            var mockParserBuilder = mock(JwtParserBuilder.class);
            when(mockParserBuilder.requireIssuer(anyString()))
                    .thenReturn(mockParserBuilder);
            when(mockParserBuilder.requireAudience(anyString()))
                    .thenReturn(mockParserBuilder);
            when(mockParserBuilder.setSigningKey(any(SecretKey.class)))
                    .thenReturn(mockParserBuilder);
            when(mockParserBuilder.build())
                    .thenReturn(mockParser);
            jwtsUtil.when(Jwts::parserBuilder)
                    .thenReturn(mockParserBuilder);

            var token = "token";
            var claims = JwtUtils.extractValidClaimsFromToken(token, JwtType.ACCESS);
            assertNotNull(claims);
            assertTrue(claims.isEmpty());
            var issAud = (String) ReflectionTestUtils.getField(JwtUtils.class, "ISS_AUD");
            verify(mockParserBuilder).requireIssuer(issAud);
            verify(mockParserBuilder).requireAudience(issAud);
            var signKey = (SecretKey) ReflectionTestUtils.getField(JwtUtils.class, "SIGN_KEY");
            verify(mockParserBuilder).setSigningKey(signKey);
            verify(mockParser).parseClaimsJws(token);
        }
    }

    @Test
    void extractValidClaimsFromTokenTest_tokenGroupIdIsInvalid() {

        try(var jwtsUtil = mockStatic(Jwts.class);
            var commonUtil = mockStatic(CommonUtils.class)) {
            var tokenId = UUID.randomUUID();
            commonUtil.when(() -> CommonUtils.uuidFromString(nullable(String.class)))
                    .thenReturn(Optional.of(tokenId), Optional.empty());

            var mockClaims = mock(Claims.class);
            when(mockClaims.getId())
                    .thenReturn(tokenId.toString());
            var jwsClaims = mock(Jws.class);
            when(jwsClaims.getBody())
                    .thenReturn(mockClaims);
            var mockParser = mock(JwtParser.class);
            when(mockParser.parseClaimsJws(anyString()))
                    .thenReturn(jwsClaims);
            var mockParserBuilder = mock(JwtParserBuilder.class);
            when(mockParserBuilder.requireIssuer(anyString()))
                    .thenReturn(mockParserBuilder);
            when(mockParserBuilder.requireAudience(anyString()))
                    .thenReturn(mockParserBuilder);
            when(mockParserBuilder.setSigningKey(any(SecretKey.class)))
                    .thenReturn(mockParserBuilder);
            when(mockParserBuilder.build())
                    .thenReturn(mockParser);
            jwtsUtil.when(Jwts::parserBuilder)
                    .thenReturn(mockParserBuilder);

            var token = "token";
            var claims = JwtUtils.extractValidClaimsFromToken(token, JwtType.ACCESS);
            assertNotNull(claims);
            assertTrue(claims.isEmpty());
            var issAud = (String) ReflectionTestUtils.getField(JwtUtils.class, "ISS_AUD");
            verify(mockParserBuilder).requireIssuer(issAud);
            verify(mockParserBuilder).requireAudience(issAud);
            var signKey = (SecretKey) ReflectionTestUtils.getField(JwtUtils.class, "SIGN_KEY");
            verify(mockParserBuilder).setSigningKey(signKey);
            verify(mockParser).parseClaimsJws(token);
        }
    }

    @Test
    void extractValidClaimsFromTokenTest_userIdIsInvalid_subjectIsNull() {

        try(var jwtsUtil = mockStatic(Jwts.class)) {
            var tokenId = UUID.randomUUID();
            var tokenGroupId = UUID.randomUUID();

            var mockClaims = mock(Claims.class);
            when(mockClaims.getId())
                    .thenReturn(tokenId.toString());
            when(mockClaims.get("tgid", String.class))
                    .thenReturn(tokenGroupId.toString());
            var jwsClaims = mock(Jws.class);
            when(jwsClaims.getBody())
                    .thenReturn(mockClaims);
            var mockParser = mock(JwtParser.class);
            when(mockParser.parseClaimsJws(anyString()))
                    .thenReturn(jwsClaims);
            var mockParserBuilder = mock(JwtParserBuilder.class);
            when(mockParserBuilder.requireIssuer(anyString()))
                    .thenReturn(mockParserBuilder);
            when(mockParserBuilder.requireAudience(anyString()))
                    .thenReturn(mockParserBuilder);
            when(mockParserBuilder.setSigningKey(any(SecretKey.class)))
                    .thenReturn(mockParserBuilder);
            when(mockParserBuilder.build())
                    .thenReturn(mockParser);
            jwtsUtil.when(Jwts::parserBuilder)
                    .thenReturn(mockParserBuilder);

            var token = "token";
            var claims = JwtUtils.extractValidClaimsFromToken(token, JwtType.ACCESS);
            assertNotNull(claims);
            assertTrue(claims.isEmpty());
            var issAud = (String) ReflectionTestUtils.getField(JwtUtils.class, "ISS_AUD");
            verify(mockParserBuilder).requireIssuer(issAud);
            verify(mockParserBuilder).requireAudience(issAud);
            var signKey = (SecretKey) ReflectionTestUtils.getField(JwtUtils.class, "SIGN_KEY");
            verify(mockParserBuilder).setSigningKey(signKey);
            verify(mockParser).parseClaimsJws(token);
        }
    }

    @Test
    void extractValidClaimsFromTokenTest_userIdIsInvalid_subjectIsInvalid() {

        try(var jwtsUtil = mockStatic(Jwts.class)) {
            var tokenId = UUID.randomUUID();
            var tokenGroupId = UUID.randomUUID();

            var mockClaims = mock(Claims.class);
            when(mockClaims.getId())
                    .thenReturn(tokenId.toString());
            when(mockClaims.getSubject())
                    .thenReturn("ref");
            when(mockClaims.get("tgid", String.class))
                    .thenReturn(tokenGroupId.toString());
            var jwsClaims = mock(Jws.class);
            when(jwsClaims.getBody())
                    .thenReturn(mockClaims);
            var mockParser = mock(JwtParser.class);
            when(mockParser.parseClaimsJws(anyString()))
                    .thenReturn(jwsClaims);
            var mockParserBuilder = mock(JwtParserBuilder.class);
            when(mockParserBuilder.requireIssuer(anyString()))
                    .thenReturn(mockParserBuilder);
            when(mockParserBuilder.requireAudience(anyString()))
                    .thenReturn(mockParserBuilder);
            when(mockParserBuilder.setSigningKey(any(SecretKey.class)))
                    .thenReturn(mockParserBuilder);
            when(mockParserBuilder.build())
                    .thenReturn(mockParser);
            jwtsUtil.when(Jwts::parserBuilder)
                    .thenReturn(mockParserBuilder);

            var token = "token";
            var claims = JwtUtils.extractValidClaimsFromToken(token, JwtType.ACCESS);
            assertNotNull(claims);
            assertTrue(claims.isEmpty());
            var issAud = (String) ReflectionTestUtils.getField(JwtUtils.class, "ISS_AUD");
            verify(mockParserBuilder).requireIssuer(issAud);
            verify(mockParserBuilder).requireAudience(issAud);
            var signKey = (SecretKey) ReflectionTestUtils.getField(JwtUtils.class, "SIGN_KEY");
            verify(mockParserBuilder).setSigningKey(signKey);
            verify(mockParser).parseClaimsJws(token);
        }
    }

    @Test
    void extractValidClaimsFromTokenTest_userIdIsInvalid_subjectIsInvalid_typeIsInvalid() {

        try(var jwtsUtil = mockStatic(Jwts.class)) {
            var tokenId = UUID.randomUUID();
            var tokenGroupId = UUID.randomUUID();

            var mockClaims = mock(Claims.class);
            when(mockClaims.getId())
                    .thenReturn(tokenId.toString());
            when(mockClaims.getSubject())
                    .thenReturn("refresh_" + UUID.randomUUID());
            when(mockClaims.get("tgid", String.class))
                    .thenReturn(tokenGroupId.toString());
            var jwsClaims = mock(Jws.class);
            when(jwsClaims.getBody())
                    .thenReturn(mockClaims);
            var mockParser = mock(JwtParser.class);
            when(mockParser.parseClaimsJws(anyString()))
                    .thenReturn(jwsClaims);
            var mockParserBuilder = mock(JwtParserBuilder.class);
            when(mockParserBuilder.requireIssuer(anyString()))
                    .thenReturn(mockParserBuilder);
            when(mockParserBuilder.requireAudience(anyString()))
                    .thenReturn(mockParserBuilder);
            when(mockParserBuilder.setSigningKey(any(SecretKey.class)))
                    .thenReturn(mockParserBuilder);
            when(mockParserBuilder.build())
                    .thenReturn(mockParser);
            jwtsUtil.when(Jwts::parserBuilder)
                    .thenReturn(mockParserBuilder);

            var token = "token";
            var claims = JwtUtils.extractValidClaimsFromToken(token, JwtType.ACCESS);
            assertNotNull(claims);
            assertTrue(claims.isEmpty());
            var issAud = (String) ReflectionTestUtils.getField(JwtUtils.class, "ISS_AUD");
            verify(mockParserBuilder).requireIssuer(issAud);
            verify(mockParserBuilder).requireAudience(issAud);
            var signKey = (SecretKey) ReflectionTestUtils.getField(JwtUtils.class, "SIGN_KEY");
            verify(mockParserBuilder).setSigningKey(signKey);
            verify(mockParser).parseClaimsJws(token);
        }
    }

    @Test
    void extractValidClaimsFromTokenTest_userIdIsInvalid_subjectIsInvalid_userIdIsInvalid() {

        try(var jwtsUtil = mockStatic(Jwts.class);
            var commonUtil = mockStatic(CommonUtils.class)) {
            var tokenId = UUID.randomUUID();
            var tokenGroupId = UUID.randomUUID();
            commonUtil.when(() -> CommonUtils.uuidFromString(nullable(String.class)))
                    .thenReturn(Optional.of(tokenId), Optional.of(tokenGroupId), Optional.empty());

            var mockClaims = mock(Claims.class);
            when(mockClaims.getId())
                    .thenReturn(tokenId.toString());
            when(mockClaims.getSubject())
                    .thenReturn("access_blah");
            when(mockClaims.get("tgid", String.class))
                    .thenReturn(tokenGroupId.toString());
            var jwsClaims = mock(Jws.class);
            when(jwsClaims.getBody())
                    .thenReturn(mockClaims);
            var mockParser = mock(JwtParser.class);
            when(mockParser.parseClaimsJws(anyString()))
                    .thenReturn(jwsClaims);
            var mockParserBuilder = mock(JwtParserBuilder.class);
            when(mockParserBuilder.requireIssuer(anyString()))
                    .thenReturn(mockParserBuilder);
            when(mockParserBuilder.requireAudience(anyString()))
                    .thenReturn(mockParserBuilder);
            when(mockParserBuilder.setSigningKey(any(SecretKey.class)))
                    .thenReturn(mockParserBuilder);
            when(mockParserBuilder.build())
                    .thenReturn(mockParser);
            jwtsUtil.when(Jwts::parserBuilder)
                    .thenReturn(mockParserBuilder);

            var token = "token";
            var claims = JwtUtils.extractValidClaimsFromToken(token, JwtType.ACCESS);
            assertNotNull(claims);
            assertTrue(claims.isEmpty());
            var issAud = (String) ReflectionTestUtils.getField(JwtUtils.class, "ISS_AUD");
            verify(mockParserBuilder).requireIssuer(issAud);
            verify(mockParserBuilder).requireAudience(issAud);
            var signKey = (SecretKey) ReflectionTestUtils.getField(JwtUtils.class, "SIGN_KEY");
            verify(mockParserBuilder).setSigningKey(signKey);
            verify(mockParser).parseClaimsJws(token);
        }
    }

    @Test
    void extractValidClaimsFromTokenTest() {

        try(var jwtsUtil = mockStatic(Jwts.class)) {
            var tokenId = UUID.randomUUID();
            var tokenGroupId = UUID.randomUUID();

            var mockClaims = mock(Claims.class);
            when(mockClaims.getId())
                    .thenReturn(tokenId.toString());
            when(mockClaims.getSubject())
                    .thenReturn("access_" + UUID.randomUUID());
            when(mockClaims.get("tgid", String.class))
                    .thenReturn(tokenGroupId.toString());
            var jwsClaims = mock(Jws.class);
            when(jwsClaims.getBody())
                    .thenReturn(mockClaims);
            var mockParser = mock(JwtParser.class);
            when(mockParser.parseClaimsJws(anyString()))
                    .thenReturn(jwsClaims);
            var mockParserBuilder = mock(JwtParserBuilder.class);
            when(mockParserBuilder.requireIssuer(anyString()))
                    .thenReturn(mockParserBuilder);
            when(mockParserBuilder.requireAudience(anyString()))
                    .thenReturn(mockParserBuilder);
            when(mockParserBuilder.setSigningKey(any(SecretKey.class)))
                    .thenReturn(mockParserBuilder);
            when(mockParserBuilder.build())
                    .thenReturn(mockParser);
            jwtsUtil.when(Jwts::parserBuilder)
                    .thenReturn(mockParserBuilder);

            var token = "token";
            var claims = JwtUtils.extractValidClaimsFromToken(token, JwtType.ACCESS);
            assertNotNull(claims);
            assertTrue(claims.isPresent());
            var issAud = (String) ReflectionTestUtils.getField(JwtUtils.class, "ISS_AUD");
            verify(mockParserBuilder).requireIssuer(issAud);
            verify(mockParserBuilder).requireAudience(issAud);
            var signKey = (SecretKey) ReflectionTestUtils.getField(JwtUtils.class, "SIGN_KEY");
            verify(mockParserBuilder).setSigningKey(signKey);
            verify(mockParser).parseClaimsJws(token);
        }
    }

    @Test
    void extractUserIdFromValidClaimsTest() {

        var tokenId = UUID.randomUUID();
        var tokenGroupId = UUID.randomUUID();
        var userId = UUID.randomUUID();

        var mockClaims = mock(Claims.class);
        when(mockClaims.getId())
                .thenReturn(tokenId.toString());
        when(mockClaims.getSubject())
                .thenReturn("access_" + userId);
        when(mockClaims.get("tgid", String.class))
                .thenReturn(tokenGroupId.toString());

        var result = JwtUtils.extractUserIdFromValidClaims(mockClaims, JwtType.ACCESS);
        assertNotNull(result);
        assertEquals(userId, result);
    }

}