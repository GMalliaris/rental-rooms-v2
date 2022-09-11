package org.gmalliaris.rental.rooms.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.apache.commons.lang3.StringUtils;
import org.gmalliaris.rental.rooms.dto.JwtType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.util.*;

public final class JwtUtils {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    private static final String ISS_AUD = "rental-rooms-api";
    private static final String TOKEN_GROUP_ID_CUSTOM_CLAIM = "tgid";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final SecretKey SIGN_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    private JwtUtils(){
        // hide implicit constructor
    }

    public static String generateToken(Date issuedAt, Date expiration,
                                       JwtType type, UUID userId, String tokenGroupId){

        return Jwts.builder()
                .setClaims(Map.of(TOKEN_GROUP_ID_CUSTOM_CLAIM, tokenGroupId))
                .setIssuer(ISS_AUD)
                .setAudience(ISS_AUD)
                .setSubject(String.format("%s_%s", type.getValue(), userId))
                .setIssuedAt(issuedAt)
                .setExpiration(expiration)
                .setId(UUID.randomUUID().toString())
                .signWith(SIGN_KEY)
                .compact();
    }

    private static Jws<Claims> extractClaims(String token)
            throws JwtException {

        return Jwts.parserBuilder()
                .requireIssuer(ISS_AUD)
                .requireAudience(ISS_AUD)
                .setSigningKey(SIGN_KEY)
                .build()
                .parseClaimsJws(token);
    }

    public static Optional<Claims> extractValidClaimsFromHeader(String header, JwtType type) {

        if (header == null || !header.startsWith(BEARER_PREFIX)){
            return Optional.empty();
        }

        var token = header.substring(BEARER_PREFIX.length());
        return extractValidClaimsFromToken(token, type);
    }

    public static Optional<Claims> extractValidClaimsFromToken(String token, JwtType type) {

        Claims claims;
        try {
            claims = extractClaims(token).getBody();

            var tokenId = CommonUtils.uuidFromString(claims.getId());
            if (tokenId.isEmpty()) {
                throw new MalformedJwtException("Invalid Token ID");
            }

            var tokenGroupId = CommonUtils.uuidFromString(extractTokenGroupIdFromClaims(claims));
            if (tokenGroupId.isEmpty()) {
                throw new MalformedJwtException("Invalid Token group ID");
            }

            var userId = extractUserIdFromClaims(claims, type);
            if (userId.isEmpty()) {
                throw new MalformedJwtException("Invalid Application User ID");
            }
        }
        catch (JwtException exception) {
            logger.debug("Failed to parse jwt: exception of class {}", exception.getClass());
            logger.debug(exception.getMessage());
            return Optional.empty();
        }

        return Optional.of(claims);
    }

    private static Optional<UUID> extractUserIdFromClaims(Claims claims, JwtType type){

        Objects.requireNonNull(claims);
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

    public static UUID extractUserIdFromValidClaims(Claims claims, JwtType type){

        return extractUserIdFromClaims(claims, type)
                .orElse(null);
    }

    public static String extractTokenGroupIdFromClaims(Claims claims) {
        return claims.get(TOKEN_GROUP_ID_CUSTOM_CLAIM, String.class);
    }
}
