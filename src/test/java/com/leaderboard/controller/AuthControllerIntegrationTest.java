package com.leaderboard.controller;

import com.leaderboard.model.UserAuthModel;
import com.leaderboard.service.AuthHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthHandler authHandler;

    @Test
    void testLogin_Success() throws Exception {
        // Arrange
        UserAuthModel loginRequest = new UserAuthModel("testuser", "password123");
        when(authHandler.login("testuser", "password123")).thenReturn("validToken");

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("validToken"));
    }

    @Test
    void testLogin_InvalidCredentials() throws Exception {
        // Arrange
        UserAuthModel loginRequest = new UserAuthModel("testuser", "wrongpassword");
        when(authHandler.login("testuser", "wrongpassword")).thenReturn(null);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void testLogin_ValidationError_ShortPassword() throws Exception {
        // Arrange
        UserAuthModel loginRequest = new UserAuthModel("testuser", "short");

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLogin_ValidationError_ShortUsername() throws Exception {
        // Arrange
        UserAuthModel loginRequest = new UserAuthModel("ab", "password123");

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRegister_Success() throws Exception {
        // Arrange
        UserAuthModel registerRequest = new UserAuthModel("newuser", "password123");
        when(authHandler.register("newuser", "password123")).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value("Registration Successful"));
    }

    @Test
    void testRegister_UserAlreadyExists() throws Exception {
        // Arrange
        UserAuthModel registerRequest = new UserAuthModel("existinguser", "password123");
        when(authHandler.register(anyString(), anyString())).thenReturn(false);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void testRegister_ValidationError_BlankUsername() throws Exception {
        // Arrange
        UserAuthModel registerRequest = new UserAuthModel("", "password123");

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }
}
