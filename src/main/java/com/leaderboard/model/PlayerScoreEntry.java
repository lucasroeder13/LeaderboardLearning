package com.leaderboard.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerScoreEntry {
    private int id;
    
    @NotBlank(message = "Username is required")
    private String username;
    
    @Positive(message = "Score must be positive")
    private float score;
}
