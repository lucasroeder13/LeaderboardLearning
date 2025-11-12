package com.leaderboard.service;

import com.leaderboard.model.PlayerScoreEntry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class RedisDBHandler {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ZSetOperations<String, Object> zSetOps;

    public RedisDBHandler(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.zSetOps = redisTemplate.opsForZSet();
    }

    /**
     * Add or update a player's score in a leaderboard
     * Uses Redis Sorted Sets for efficient ranking
     */
    public boolean addOrUpdateScore(String leaderboardName, String username, double score) {
        try {
            String key = "leaderboard:" + leaderboardName;
            zSetOps.add(key, username, score);
            log.info("Added/updated score for user {} in leaderboard {}: {}", username, leaderboardName, score);
            return true;
        } catch (Exception e) {
            log.error("Error adding score for user {} in leaderboard {}", username, leaderboardName, e);
            return false;
        }
    }

    /**
     * Get top N players from a leaderboard (highest scores first)
     */
    public List<PlayerScoreEntry> getTopScores(String leaderboardName, int limit) {
        try {
            String key = "leaderboard:" + leaderboardName;
            // Get top scores in descending order
            Set<ZSetOperations.TypedTuple<Object>> typedTuples = 
                zSetOps.reverseRangeWithScores(key, 0, limit - 1);
            
            List<PlayerScoreEntry> results = new ArrayList<>();
            if (typedTuples != null) {
                int rank = 1;
                for (ZSetOperations.TypedTuple<Object> tuple : typedTuples) {
                    String username = (String) tuple.getValue();
                    Double score = tuple.getScore();
                    if (username != null && score != null) {
                        results.add(new PlayerScoreEntry(rank++, username, score.floatValue()));
                    }
                }
            }
            log.debug("Retrieved top {} scores from leaderboard {}", limit, leaderboardName);
            return results;
        } catch (Exception e) {
            log.error("Error getting top scores from leaderboard {}", leaderboardName, e);
            return new ArrayList<>();
        }
    }

    /**
     * Get a player's rank in a leaderboard (1-based, where 1 is the highest score)
     */
    public Long getPlayerRank(String leaderboardName, String username) {
        try {
            String key = "leaderboard:" + leaderboardName;
            Long rank = zSetOps.reverseRank(key, username);
            if (rank != null) {
                return rank + 1; // Convert 0-based to 1-based ranking
            }
            log.debug("Player {} not found in leaderboard {}", username, leaderboardName);
            return null;
        } catch (Exception e) {
            log.error("Error getting rank for user {} in leaderboard {}", username, leaderboardName, e);
            return null;
        }
    }

    /**
     * Get a player's score in a leaderboard
     */
    public Double getPlayerScore(String leaderboardName, String username) {
        try {
            String key = "leaderboard:" + leaderboardName;
            Double score = zSetOps.score(key, username);
            log.debug("Retrieved score for user {} in leaderboard {}: {}", username, leaderboardName, score);
            return score;
        } catch (Exception e) {
            log.error("Error getting score for user {} in leaderboard {}", username, leaderboardName, e);
            return null;
        }
    }

    /**
     * Get total number of players in a leaderboard
     */
    public Long getLeaderboardSize(String leaderboardName) {
        try {
            String key = "leaderboard:" + leaderboardName;
            Long size = zSetOps.size(key);
            log.debug("Leaderboard {} has {} players", leaderboardName, size);
            return size;
        } catch (Exception e) {
            log.error("Error getting size of leaderboard {}", leaderboardName, e);
            return 0L;
        }
    }

    /**
     * Remove a player from a leaderboard
     */
    public boolean removePlayer(String leaderboardName, String username) {
        try {
            String key = "leaderboard:" + leaderboardName;
            Long removed = zSetOps.remove(key, username);
            boolean success = removed != null && removed > 0;
            if (success) {
                log.info("Removed user {} from leaderboard {}", username, leaderboardName);
            }
            return success;
        } catch (Exception e) {
            log.error("Error removing user {} from leaderboard {}", username, leaderboardName, e);
            return false;
        }
    }

    /**
     * Get scores within a range (for pagination)
     */
    public List<PlayerScoreEntry> getScoresInRange(String leaderboardName, int start, int end) {
        try {
            String key = "leaderboard:" + leaderboardName;
            Set<ZSetOperations.TypedTuple<Object>> typedTuples = 
                zSetOps.reverseRangeWithScores(key, start, end);
            
            List<PlayerScoreEntry> results = new ArrayList<>();
            if (typedTuples != null) {
                int rank = start + 1;
                for (ZSetOperations.TypedTuple<Object> tuple : typedTuples) {
                    String username = (String) tuple.getValue();
                    Double score = tuple.getScore();
                    if (username != null && score != null) {
                        results.add(new PlayerScoreEntry(rank++, username, score.floatValue()));
                    }
                }
            }
            log.debug("Retrieved scores {} to {} from leaderboard {}", start, end, leaderboardName);
            return results;
        } catch (Exception e) {
            log.error("Error getting score range from leaderboard {}", leaderboardName, e);
            return new ArrayList<>();
        }
    }
}
