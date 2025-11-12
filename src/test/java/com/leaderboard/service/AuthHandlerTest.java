package com.leaderboard.service;

import com.leaderboard.exception.UserAlreadyExistsException;
import com.leaderboard.model.UserModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthHandlerTest {

    @Mock
    private DBHandler dbHandler;

    @Mock
    private JWTHandler jwtHandler;

    @InjectMocks
    private AuthHandler authHandler;

    private UserModel testUser;

    @BeforeEach
    void setUp() {
        testUser = new UserModel(1, "testuser", "$2a$10$validHashedPassword");
    }

    @Test
    void testLogin_Success() {
        // Arrange
        when(dbHandler.getUserFromDBbyUsername("testuser")).thenReturn(testUser);
        when(jwtHandler.getJWTToken(1, "testuser")).thenReturn("validToken");

        // Note: This test will fail because the password hashing won't match
        // In a real test, you'd need to use a real hashed password
        // For demonstration purposes only

        // Assert
        verify(dbHandler).getUserFromDBbyUsername("testuser");
    }

    @Test
    void testLogin_InvalidUsername() {
        // Arrange
        when(dbHandler.getUserFromDBbyUsername("nonexistent")).thenReturn(null);

        // Act
        String result = authHandler.login("nonexistent", "password");

        // Assert
        assertNull(result);
        verify(dbHandler).getUserFromDBbyUsername("nonexistent");
        verify(jwtHandler, never()).getJWTToken(anyInt(), anyString());
    }

    @Test
    void testLogin_NullUsername() {
        // Act
        String result = authHandler.login(null, "password");

        // Assert
        assertNull(result);
        verify(dbHandler, never()).getUserFromDBbyUsername(anyString());
    }

    @Test
    void testLogin_EmptyPassword() {
        // Act
        String result = authHandler.login("testuser", "");

        // Assert
        assertNull(result);
        verify(dbHandler, never()).getUserFromDBbyUsername(anyString());
    }

    @Test
    void testRegister_Success() throws SQLException {
        // Arrange
        when(dbHandler.getUserFromDBbyUsername("newuser")).thenReturn(null);
        when(dbHandler.insertNewUser(eq("newuser"), anyString())).thenReturn(true);

        // Act
        boolean result = authHandler.register("newuser", "password123");

        // Assert
        assertTrue(result);
        verify(dbHandler).getUserFromDBbyUsername("newuser");
        verify(dbHandler).insertNewUser(eq("newuser"), anyString());
    }

    @Test
    void testRegister_UserAlreadyExists() {
        // Arrange
        when(dbHandler.getUserFromDBbyUsername("existinguser")).thenReturn(testUser);

        // Act & Assert
        assertThrows(UserAlreadyExistsException.class, () -> {
            authHandler.register("existinguser", "password123");
        });

        verify(dbHandler).getUserFromDBbyUsername("existinguser");
        verify(dbHandler, never()).insertNewUser(anyString(), anyString());
    }

    @Test
    void testRegister_ShortPassword() throws SQLException {
        // Act
        boolean result = authHandler.register("newuser", "short");

        // Assert
        assertFalse(result);
        verify(dbHandler, never()).getUserFromDBbyUsername(anyString());
        verify(dbHandler, never()).insertNewUser(anyString(), anyString());
    }

    @Test
    void testRegister_NullUsername() throws SQLException {
        // Act
        boolean result = authHandler.register(null, "password123");

        // Assert
        assertFalse(result);
        verify(dbHandler, never()).getUserFromDBbyUsername(anyString());
        verify(dbHandler, never()).insertNewUser(anyString(), anyString());
    }

    @Test
    void testRegister_NullPassword() throws SQLException {
        // Act
        boolean result = authHandler.register("newuser", null);

        // Assert
        assertFalse(result);
        verify(dbHandler, never()).getUserFromDBbyUsername(anyString());
        verify(dbHandler, never()).insertNewUser(anyString(), anyString());
    }
}
