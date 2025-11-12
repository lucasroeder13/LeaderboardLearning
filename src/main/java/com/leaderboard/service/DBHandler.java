package com.leaderboard.service;

import com.leaderboard.model.LeaderboardModel;
import com.leaderboard.model.UserModel;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class DBHandler {
    private final DataSource dataSource;

    public DBHandler(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void init() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS user(id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT UNIQUE, password TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS leaderboards(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT)");
            log.info("Database tables initialized successfully");
        } catch (SQLException e) {
            log.error("Failed to initialize database tables", e);
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public UserModel getUserFromDBbyUsername(String username) {
        String query = "SELECT * FROM user WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, username);
            ResultSet results = stmt.executeQuery();

            if (results.next()) {
                log.debug("User found: {}", username);
                return new UserModel(
                        results.getInt("id"),
                        results.getString("username"),
                        results.getString("password")
                );
            }
            log.debug("User not found: {}", username);
            return null;
        } catch (SQLException e) {
            log.error("Error fetching user: {}", username, e);
            throw new RuntimeException("Error fetching user", e);
        }
    }

    public boolean insertNewUser(String username, String password) {
        String query = "INSERT INTO user (username, password) VALUES (?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.executeUpdate();
            log.info("New user created: {}", username);
            return true;
        } catch (SQLException e) {
            log.error("Error inserting user: {}", username, e);
            return false;
        }
    }

    public boolean createLeaderboard(String name) {
        String query = "INSERT INTO leaderboards (name) VALUES (?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, name);
            stmt.executeUpdate();
            log.info("New leaderboard created: {}", name);
            return true;
        } catch (SQLException e) {
            log.error("Error creating leaderboard: {}", name, e);
            return false;
        }
    }

    public boolean checkIfLeaderboardExists(String name) {
        try (Connection conn = getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet resultSet = meta.getTables(null, null, name, new String[]{"TABLE"});
            boolean exists = resultSet.next();
            log.debug("Leaderboard exists check for {}: {}", name, exists);
            return exists;
        } catch (SQLException e) {
            log.error("Error checking if leaderboard exists: {}", name, e);
            return false;
        }
    }

    public List<LeaderboardModel> getLeaderboards() {
        String query = "SELECT id, name FROM leaderboards";
        List<LeaderboardModel> leaderboards = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                LeaderboardModel leaderboard = new LeaderboardModel(
                    rs.getInt("id"), 
                    rs.getString("name")
                );
                leaderboards.add(leaderboard);
            }
            log.debug("Retrieved {} leaderboards", leaderboards.size());
        } catch (SQLException e) {
            log.error("Error fetching leaderboards", e);
            throw new RuntimeException("Error fetching leaderboards", e);
        }

        return leaderboards;
    }
}

