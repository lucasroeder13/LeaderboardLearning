package com.leaderboard.controller;

import com.leaderboard.model.LeaderboardModel;
import com.leaderboard.model.PlayerScoreEntry;
import com.leaderboard.service.DBHandler;
import com.leaderboard.service.JWTHandler;
import com.leaderboard.service.RedisDBHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/leaderboard")
@Tag(name = "Leaderboard", description = "Leaderboard management and score tracking")
@SecurityRequirement(name = "bearerAuth")
public class LeaderboardScoreController {

    private final DBHandler dbHandler;
    private final RedisDBHandler redisDBHandler;
    private final JWTHandler jwtHandler;

    public LeaderboardScoreController(DBHandler dbHandler, RedisDBHandler redisDBHandler, JWTHandler jwtHandler) {
        this.dbHandler = dbHandler;
        this.redisDBHandler = redisDBHandler;
        this.jwtHandler = jwtHandler;
    }

    @Data
    public static class ScoreSubmission {
        @Min(value = 0, message = "Score must be non-negative")
        private double score;
    }

    @Data
    public static class CreateLeaderboardRequest {
        @jakarta.validation.constraints.NotBlank(message = "Leaderboard name is required")
        @jakarta.validation.constraints.Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
        private String name;
    }

    @Operation(summary = "Get all leaderboards", description = "Returns a list of all available leaderboards")
    @GetMapping
    public ResponseEntity<List<LeaderboardModel>> getAllLeaderboards() {
        log.info("Fetching all leaderboards");
        List<LeaderboardModel> leaderboards = dbHandler.getLeaderboards();
        return ResponseEntity.ok(leaderboards);
    }

    @Operation(summary = "Create a new leaderboard", description = "Creates a new leaderboard with the given name")
    @PostMapping
    public ResponseEntity<Map<String, String>> createLeaderboard(
            @Valid @RequestBody CreateLeaderboardRequest request) {
        log.info("Creating new leaderboard: {}", request.getName());
        
        boolean success = dbHandler.createLeaderboard(request.getName());
        if (!success) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to create leaderboard"));
        }
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("success", "Leaderboard created", "name", request.getName()));
    }

    @Operation(summary = "Submit a score", description = "Submit a score for the authenticated user to a specific leaderboard")
    @PostMapping("/{leaderboardName}/score")
    public ResponseEntity<Map<String, Object>> submitScore(
            @Parameter(description = "Name of the leaderboard") @PathVariable String leaderboardName,
            @Valid @RequestBody ScoreSubmission scoreSubmission,
            Authentication authentication) {
        
        String username = authentication.getName();
        log.info("User {} submitting score {} to leaderboard {}", username, scoreSubmission.getScore(), leaderboardName);
        
        boolean success = redisDBHandler.addOrUpdateScore(leaderboardName, username, scoreSubmission.getScore());
        if (!success) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to submit score"));
        }
        
        Long rank = redisDBHandler.getPlayerRank(leaderboardName, username);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("score", scoreSubmission.getScore());
        response.put("rank", rank);
        response.put("username", username);
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get top scores", description = "Get the top N scores from a leaderboard")
    @GetMapping("/{leaderboardName}/top")
    public ResponseEntity<List<PlayerScoreEntry>> getTopScores(
            @Parameter(description = "Name of the leaderboard") @PathVariable String leaderboardName,
            @Parameter(description = "Number of top scores to return") 
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit) {
        
        log.info("Fetching top {} scores from leaderboard {}", limit, leaderboardName);
        List<PlayerScoreEntry> topScores = redisDBHandler.getTopScores(leaderboardName, limit);
        return ResponseEntity.ok(topScores);
    }

    @Operation(summary = "Get player's score and rank", description = "Get the authenticated user's score and rank in a leaderboard")
    @GetMapping("/{leaderboardName}/me")
    public ResponseEntity<Map<String, Object>> getMyScore(
            @Parameter(description = "Name of the leaderboard") @PathVariable String leaderboardName,
            Authentication authentication) {
        
        String username = authentication.getName();
        log.info("User {} fetching their score from leaderboard {}", username, leaderboardName);
        
        Double score = redisDBHandler.getPlayerScore(leaderboardName, username);
        Long rank = redisDBHandler.getPlayerRank(leaderboardName, username);
        Long totalPlayers = redisDBHandler.getLeaderboardSize(leaderboardName);
        
        if (score == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No score found for user in this leaderboard"));
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("username", username);
        response.put("score", score);
        response.put("rank", rank);
        response.put("totalPlayers", totalPlayers);
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get leaderboard with pagination", description = "Get scores from a leaderboard with pagination")
    @GetMapping("/{leaderboardName}/scores")
    public ResponseEntity<Map<String, Object>> getLeaderboardScores(
            @Parameter(description = "Name of the leaderboard") @PathVariable String leaderboardName,
            @Parameter(description = "Start index (0-based)") @RequestParam(defaultValue = "0") @Min(0) int start,
            @Parameter(description = "Number of scores to return") @RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit) {
        
        log.info("Fetching scores from leaderboard {} (start: {}, limit: {})", leaderboardName, start, limit);
        
        int end = start + limit - 1;
        List<PlayerScoreEntry> scores = redisDBHandler.getScoresInRange(leaderboardName, start, end);
        Long totalPlayers = redisDBHandler.getLeaderboardSize(leaderboardName);
        
        Map<String, Object> response = new HashMap<>();
        response.put("leaderboardName", leaderboardName);
        response.put("scores", scores);
        response.put("start", start);
        response.put("limit", limit);
        response.put("totalPlayers", totalPlayers);
        
        return ResponseEntity.ok(response);
    }
}
