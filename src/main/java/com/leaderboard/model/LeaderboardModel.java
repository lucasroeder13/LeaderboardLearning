package com.leaderboard.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardModel {
    private int id;
    
    @NotBlank(message = "Leaderboard name is required")
    @Size(min = 3, max = 100, message = "Leaderboard name must be between 3 and 100 characters")
    private String name;
}
