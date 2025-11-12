package com.leaderboard.service;

import com.leaderboard.model.PlayerScoreEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisDBHandlerTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ZSetOperations<String, Object> zSetOperations;

    private RedisDBHandler redisDBHandler;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        redisDBHandler = new RedisDBHandler(redisTemplate);
    }

    @Test
    void testAddOrUpdateScore_Success() {
        // Arrange
        when(zSetOperations.add(anyString(), anyString(), anyDouble())).thenReturn(true);

        // Act
        boolean result = redisDBHandler.addOrUpdateScore("test-leaderboard", "player1", 100.0);

        // Assert
        assertTrue(result);
        verify(zSetOperations).add("leaderboard:test-leaderboard", "player1", 100.0);
    }

    @Test
    void testGetTopScores_Success() {
        // Arrange
        Set<ZSetOperations.TypedTuple<Object>> mockTuples = new HashSet<>();
        mockTuples.add(createTuple("player1", 100.0));
        mockTuples.add(createTuple("player2", 90.0));
        
        when(zSetOperations.reverseRangeWithScores(anyString(), anyLong(), anyLong()))
                .thenReturn(mockTuples);

        // Act
        List<PlayerScoreEntry> results = redisDBHandler.getTopScores("test-leaderboard", 10);

        // Assert
        assertEquals(2, results.size());
        verify(zSetOperations).reverseRangeWithScores("leaderboard:test-leaderboard", 0, 9);
    }

    @Test
    void testGetPlayerRank_Success() {
        // Arrange
        when(zSetOperations.reverseRank(anyString(), anyString())).thenReturn(0L);

        // Act
        Long rank = redisDBHandler.getPlayerRank("test-leaderboard", "player1");

        // Assert
        assertEquals(1L, rank); // 0-based converted to 1-based
        verify(zSetOperations).reverseRank("leaderboard:test-leaderboard", "player1");
    }

    @Test
    void testGetPlayerRank_NotFound() {
        // Arrange
        when(zSetOperations.reverseRank(anyString(), anyString())).thenReturn(null);

        // Act
        Long rank = redisDBHandler.getPlayerRank("test-leaderboard", "nonexistent");

        // Assert
        assertNull(rank);
    }

    @Test
    void testGetPlayerScore_Success() {
        // Arrange
        when(zSetOperations.score(anyString(), anyString())).thenReturn(85.5);

        // Act
        Double score = redisDBHandler.getPlayerScore("test-leaderboard", "player1");

        // Assert
        assertEquals(85.5, score);
        verify(zSetOperations).score("leaderboard:test-leaderboard", "player1");
    }

    @Test
    void testGetLeaderboardSize() {
        // Arrange
        when(zSetOperations.size(anyString())).thenReturn(42L);

        // Act
        Long size = redisDBHandler.getLeaderboardSize("test-leaderboard");

        // Assert
        assertEquals(42L, size);
        verify(zSetOperations).size("leaderboard:test-leaderboard");
    }

    @Test
    void testRemovePlayer_Success() {
        // Arrange
        when(zSetOperations.remove(anyString(), any())).thenReturn(1L);

        // Act
        boolean result = redisDBHandler.removePlayer("test-leaderboard", "player1");

        // Assert
        assertTrue(result);
        verify(zSetOperations).remove("leaderboard:test-leaderboard", "player1");
    }

    @Test
    void testRemovePlayer_NotFound() {
        // Arrange
        when(zSetOperations.remove(anyString(), any())).thenReturn(0L);

        // Act
        boolean result = redisDBHandler.removePlayer("test-leaderboard", "nonexistent");

        // Assert
        assertFalse(result);
    }

    private ZSetOperations.TypedTuple<Object> createTuple(String value, Double score) {
        return new ZSetOperations.TypedTuple<Object>() {
            @Override
            public Object getValue() {
                return value;
            }

            @Override
            public Double getScore() {
                return score;
            }

            @Override
            public int compareTo(ZSetOperations.TypedTuple<Object> o) {
                return Double.compare(this.getScore(), o.getScore());
            }
        };
    }
}
