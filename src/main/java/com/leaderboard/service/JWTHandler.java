package com.leaderboard.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static io.jsonwebtoken.Jwts.*;

@Slf4j
@Service
public class JWTHandler {

    private final SecretKey SECRET_KEY;
    private final long expirationTime;

    public JWTHandler(@Value("${jwt.secret}") String secret,
                     @Value("${jwt.expiration}") long expiration) {
        this.SECRET_KEY = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationTime = expiration;
    }

    public String getJWTToken(int userID, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("userID", userID);
        long now = System.currentTimeMillis();

        try {
            String token = Jwts.builder()
                    .claims(claims)
                    .subject(String.valueOf(userID))
                    .issuedAt(new Date(now))
                    .expiration(new Date(now + expirationTime))
                    .signWith(SECRET_KEY, Jwts.SIG.HS256)
                    .compact();
            return token;
        }
        catch (Exception e) {
            log.error("Error creating JWT Token", e);
            return null;
        }
    }


    public boolean checkToken(String token) {
        try {
            Claims claims = parser()
                    .verifyWith(SECRET_KEY)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            log.error("Invalid token", e);
            return false;
        }
    }


    public String getTokenRole(String token) {
        Claims claims = parser()
                .verifyWith(SECRET_KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.get("role", String.class);
    }

    public String getTokenUserID(String token) {
        Claims claims = parser()
                .verifyWith(SECRET_KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        // Fixed: was using "ID" but we store it as "userID" in claims
        return String.valueOf(claims.get("userID", Integer.class));
    }




}