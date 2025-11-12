package com.leaderboard.service;

import com.leaderboard.exception.UserAlreadyExistsException;
import com.leaderboard.model.UserModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.SQLException;

@Slf4j
@Service
public class AuthHandler {
    private static final PasswordEncoder encoder = new BCryptPasswordEncoder();
    private final DBHandler db;
    private final JWTHandler jwtHandler;

    public AuthHandler(DBHandler db, JWTHandler jwtHandler) {
        this.db = db;
        this.jwtHandler = jwtHandler;
    }

    private String encodePW(String password) {
        return encoder.encode(password);
    }

    private boolean checkPW(String password, String encoded) {
        return encoder.matches(password, encoded);
    }

    public String login(String username, String password) {
        // Input validation
        if (username == null || username.trim().isEmpty() ||
                password == null || password.isEmpty()) {
            log.warn("Login attempt with empty username or password");
            return null;
        }

        try {
            UserModel user = db.getUserFromDBbyUsername(username);

            // Always check password even if user is null (constant-time comparison)
            String encodedPassword = user != null ? user.getPassword() : "$2a$10$dummy.hash.to.prevent.timing.attack";
            boolean passwordMatches = checkPW(password, encodedPassword);

            if (user != null && passwordMatches) {
                log.info("Successful login for user: {}", username);
                return jwtHandler.getJWTToken(user.getId(), user.getUsername());
            }
            log.warn("Failed login attempt for user: {}", username);
            return null;
        } catch (Exception e) {
            log.error("Login failed for user: {}", username, e);
            return null;
        }
    }

    public boolean register(String username, String password) throws SQLException {
        // Check if Password or Username is null
        if (password == null || username == null) {
            log.warn("Registration attempt with null username or password");
            return false;
        }

        // Check if the password is long enough
        if (password.length() < 8) {
            log.warn("Registration attempt with short password for user: {}", username);
            return false;
        }

        // Check if a User already exists
        UserModel existingUser = db.getUserFromDBbyUsername(username);
        if (existingUser != null) {
            log.warn("Registration attempt for existing user: {}", username);
            throw new UserAlreadyExistsException("Username already exists: " + username);
        }

        // Encode password and insert
        String encodedPassword = encodePW(password);
        boolean success = db.insertNewUser(username, encodedPassword);
        
        if (success) {
            log.info("Successfully registered new user: {}", username);
        } else {
            log.error("Failed to register user: {}", username);
        }
        
        return success;
    }
}
