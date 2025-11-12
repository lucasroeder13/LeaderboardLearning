package com.leaderboard.controller;

import com.leaderboard.service.DBHandler;
import com.leaderboard.service.JWTHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/user")
public class LeaderboardController {

    private final DBHandler dbHandler;
    private final JWTHandler jwtHandler;

    public LeaderboardController(DBHandler dbHandler, JWTHandler jwtHandler) {
        this.dbHandler = dbHandler;
        this.jwtHandler = jwtHandler;
    }

    // Get own data and leaderboard placements
    @GetMapping("/self")
    public ResponseEntity<String> getSelf(@RequestHeader("Authorization") String token){
        boolean tokenValid = jwtHandler.checkToken(token);
        if (!tokenValid){
            log.warn("Invalid token attempted to access /self endpoint");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Token");
        }
        
        String userID = jwtHandler.getTokenUserID(token);
        log.info("User {} accessing self endpoint", userID);
        
        var leaderboards = dbHandler.getLeaderboards();

        return ResponseEntity.ok().body(leaderboards.toString());
    }
}
