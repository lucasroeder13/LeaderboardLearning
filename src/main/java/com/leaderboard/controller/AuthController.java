package com.leaderboard.controller;

import com.leaderboard.model.UserAuthModel;
import com.leaderboard.service.AuthHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "User authentication and registration endpoints")
public class AuthController {

    private final AuthHandler authHandler;

    public AuthController(AuthHandler authHandler) {
        this.authHandler = authHandler;
    }

    @Operation(
        summary = "User login",
        description = "Authenticate a user and receive a JWT token"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = "{\"token\": \"eyJhbGc...\"}"))),
        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
        @ApiResponse(responseCode = "400", description = "Validation error")
    })
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody UserAuthModel userAuthModel) {
        log.info("Login attempt for user: {}", userAuthModel.getUsername());
        
        String username = userAuthModel.getUsername();
        String password = userAuthModel.getPassword();

        // Get JWT token if username and password are valid
        String loginResp = authHandler.login(username, password);
        
        // Check if a function returned a JWT token
        if (loginResp == null) {
            log.warn("Failed login attempt for user: {}", username);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error","Invalid Username or Password"));
        }

        log.info("Successful login for user: {}", username);
        return ResponseEntity.ok(Map.of("token", loginResp));
    }

    @Operation(
        summary = "User registration",
        description = "Register a new user account"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Registration successful"),
        @ApiResponse(responseCode = "400", description = "Validation error or registration failed"),
        @ApiResponse(responseCode = "409", description = "Username already exists")
    })
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody UserAuthModel registerModel) throws SQLException {
        log.info("Registration attempt for user: {}", registerModel.getUsername());
        
        String username = registerModel.getUsername();
        String password = registerModel.getPassword();

        boolean success = authHandler.register(username, password);
        if (!success) {
            log.warn("Failed registration attempt for user: {}", username);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to Register - Username may already exist"));
        }

        log.info("Successful registration for user: {}", username);
        return ResponseEntity.ok(Map.of("success", "Registration Successful"));
    }

}
