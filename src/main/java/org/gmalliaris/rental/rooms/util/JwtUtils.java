package org.gmalliaris.rental.rooms.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.gmalliaris.rental.rooms.dto.JwtType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class JwtUtils {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    private static final String ISS = "rental-rooms-api";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final SecretKey SIGN_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    private JwtUtils(){
        // hide implicit constructor
    }

    public static String generateToken(Date issuedAt, Date expiration,
                                       JwtType type, UUID userId){

        return Jwts.builder()
                .setClaims(Map.of("tgid", UUID.randomUUID().toString()))
                .setIssuer(ISS)
                .setAudience(ISS)
                .setSubject(String.format("%s_%s", type.getValue(), userId))
                .setIssuedAt(issuedAt)
                .setExpiration(expiration)
                .setId(UUID.randomUUID().toString())
                .signWith(SIGN_KEY)
                .compact();
    }

    public static Jws<Claims> extractClaims(String token)
            throws JwtException {

        return Jwts.parserBuilder()
                .requireIssuer(ISS)
                .requireAudience(ISS)
                .setSigningKey(SIGN_KEY)
                .build()
                .parseClaimsJws(token);
    }

    public static Optional<UUID> extractUserIdFromToken(String token, JwtType type)
            throws JwtException{

        var claims = extractClaims(token).getBody();
        if (claims == null){
            return Optional.empty();
        }

        var subject = claims.getSubject();
        if (subject == null) {
            return Optional.empty();
        }

        var subjectIdComponents = subject.split("_");
        if (subjectIdComponents.length != 2
            || !type.getValue().equals(subjectIdComponents[0])) {
            return Optional.empty();
        }
        return CommonUtils.uuidFromString(subjectIdComponents[1]);
    }

    public static Optional<UUID> extractUserIdFromHeader(String authorizationHeader, JwtType type){

        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)){
            return Optional.empty();
        }

        var token = authorizationHeader.substring(BEARER_PREFIX.length());
        try {
            return extractUserIdFromToken(token, type);
        }
        catch (JwtException exception) {
            logger.debug("Failed to parse jwt: exception of class {}", exception.getClass());
            logger.debug(exception.getMessage());
            return Optional.empty();
        }
    }

    public static Optional<Date> extractExpirationFromToken(String token, JwtType type)
            throws JwtException{

        var claims = extractClaims(token).getBody();
        if (claims == null){
            return Optional.empty();
        }
        return Optional.ofNullable(claims.getExpiration());
    }

    public static Optional<Date> extractExpirationFromHeader(String authorizationHeader, JwtType type){

        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)){
            return Optional.empty();
        }

        var token = authorizationHeader.substring(BEARER_PREFIX.length());
        try {
            return extractExpirationFromToken(token, type);
        }
        catch (JwtException exception) {
            logger.debug("Failed to parse jwt: exception of class {}", exception.getClass());
            logger.debug(exception.getMessage());
            return Optional.empty();
        }
    }
}
