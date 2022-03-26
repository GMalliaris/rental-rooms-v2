package org.gmalliaris.rental.rooms.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.gmalliaris.rental.rooms.dto.JwtType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Optional;

public final class JwtUtils {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    private static final String ISS = "rental-rooms-api";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final SecretKey SIGN_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    private JwtUtils(){
        // hide implicit constructor
    }

    public static String generateToken(Date issuedAt, Date expiration,
                                       JwtType type, String email){

        return Jwts.builder()
                .setIssuer(ISS)
                .setSubject(type.getValue())
                .setIssuedAt(issuedAt)
                .setExpiration(expiration)
                .setId(email)
                .signWith(SIGN_KEY)
                .compact();
    }

    public static Jws<Claims> extractClaims(String token, JwtType type)
            throws JwtException {

        return Jwts.parserBuilder()
                .requireIssuer(ISS)
                .requireSubject(type.getValue())
                .setSigningKey(SIGN_KEY)
                .build()
                .parseClaimsJws(token);
    }

    public static Optional<String> extractUserEmailFromToken(String token, JwtType type)
            throws JwtException{

        var claims = extractClaims(token, type).getBody();
        if (claims == null){
            return Optional.empty();
        }
        return Optional.ofNullable(claims.getId());
    }

    public static Optional<String> extractUserEmailFromHeader(String authorizationHeader, JwtType type){

        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)){
            return Optional.empty();
        }

        var token = authorizationHeader.substring(BEARER_PREFIX.length());
        try {
            return extractUserEmailFromToken(token, type);
        }
        catch (JwtException exception) {
            logger.debug("Failed to parse jwt: exception of class {}", exception.getClass());
            logger.debug(exception.getMessage());
            return Optional.empty();
        }
    }
}
