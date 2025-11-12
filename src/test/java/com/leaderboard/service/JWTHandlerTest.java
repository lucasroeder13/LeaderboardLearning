package com.leaderboard.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class JWTHandlerTest {

    private JWTHandler jwtHandler;
    private final String testSecret = "test-secret-key-that-is-at-least-256-bits-long-for-hs256-algorithm";
    private final long testExpiration = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        jwtHandler = new JWTHandler(testSecret, testExpiration);
    }

    @Test
    void testGetJWTToken_Success() {
        // Act
        String token = jwtHandler.getJWTToken(1, "testuser");

        // Assert
        assertNotNull(token);
        assertTrue(token.length() > 0);
        assertTrue(token.startsWith("eyJ")); // JWT tokens start with this
    }

    @Test
    void testCheckToken_ValidToken() {
        // Arrange
        String token = jwtHandler.getJWTToken(1, "testuser");

        // Act
        boolean isValid = jwtHandler.checkToken(token);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void testCheckToken_InvalidToken() {
        // Act
        boolean isValid = jwtHandler.checkToken("invalid.token.here");

        // Assert
        assertFalse(isValid);
    }

    @Test
    void testGetTokenUserID_Success() {
        // Arrange
        String token = jwtHandler.getJWTToken(42, "testuser");

        // Act
        String userId = jwtHandler.getTokenUserID(token);

        // Assert
        assertEquals("42", userId);
    }

    @Test
    void testGetTokenRole_Success() {
        // Arrange
        String token = jwtHandler.getJWTToken(1, "admin");

        // Act
        String role = jwtHandler.getTokenRole(token);

        // Assert
        assertEquals("admin", role);
    }

    @Test
    void testTokenContainsCorrectClaims() {
        // Arrange
        String token = jwtHandler.getJWTToken(123, "testuser");
        SecretKey key = Keys.hmacShaKeyFor(testSecret.getBytes(StandardCharsets.UTF_8));

        // Act
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        // Assert
        assertEquals(123, claims.get("userID", Integer.class));
        assertEquals("testuser", claims.get("role", String.class));
        assertEquals("123", claims.getSubject());
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }

    @Test
    void testTokenExpiration() throws InterruptedException {
        // Create handler with very short expiration
        JWTHandler shortExpiryHandler = new JWTHandler(testSecret, 1); // 1ms
        
        // Arrange
        String token = shortExpiryHandler.getJWTToken(1, "testuser");
        
        // Wait for token to expire
        Thread.sleep(10);

        // Act
        boolean isValid = shortExpiryHandler.checkToken(token);

        // Assert
        assertFalse(isValid);
    }
}
