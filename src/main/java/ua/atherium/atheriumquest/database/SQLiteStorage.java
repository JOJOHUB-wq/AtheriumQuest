package ua.atherium.atheriumquest.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import org.bukkit.plugin.java.JavaPlugin;

public class SQLiteStorage implements Storage {

    private final JavaPlugin plugin;
    private Connection connection;

    public SQLiteStorage(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void init() {
        File dataFolder = new File(plugin.getDataFolder(), "database.db");
        if (!dataFolder.getParentFile().exists()) {
            dataFolder.getParentFile().mkdirs();
        }

        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dataFolder.getAbsolutePath());
            try (Statement statement = connection.createStatement()) {
                statement.execute("CREATE TABLE IF NOT EXISTS player_levels (" +
                        "uuid VARCHAR(36) NOT NULL, " +
                        "npc_type VARCHAR(32) NOT NULL, " +
                        "level INTEGER DEFAULT 1, " +
                        "quest_index INTEGER DEFAULT 0, " +
                        "PRIMARY KEY (uuid, npc_type))");
                statement.execute("CREATE TABLE IF NOT EXISTS quest_progress (" +
                        "uuid VARCHAR(36) NOT NULL, " +
                        "progress_key VARCHAR(64) NOT NULL, " +
                        "amount INTEGER DEFAULT 0, " +
                        "PRIMARY KEY (uuid, progress_key))");
                statement.execute("CREATE TABLE IF NOT EXISTS quest_timer (" +
                        "uuid VARCHAR(36) PRIMARY KEY, " +
                        "start_time LONG DEFAULT 0, " +
                        "end_time LONG DEFAULT 0)");
                statement.execute("CREATE TABLE IF NOT EXISTS balances (" +
                        "uuid VARCHAR(36) PRIMARY KEY, " +
                        "balance DOUBLE DEFAULT 0.0)");
            }
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("Failed to initialize SQLite database", e);
        }
    }

    @Override
    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                plugin.getLogger().severe("Error closing SQLite connection: " + e.getMessage());
            }
        }
    }

    @Override
    public int getLevel(UUID uuid, String npcType) {
        String sql = "SELECT level FROM player_levels WHERE uuid = ? AND npc_type = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, npcType);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("level");
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error getting level: " + e.getMessage());
        }
        return 1;
    }

    @Override
    public void setLevel(UUID uuid, String npcType, int level) {
        String sql = "INSERT INTO player_levels (uuid, npc_type, level) VALUES (?, ?, ?) ON CONFLICT(uuid, npc_type) DO UPDATE SET level=excluded.level";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, npcType);
            ps.setInt(3, level);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Error setting level: " + e.getMessage());
        }
    }

    @Override
    public int getQuestIndex(UUID uuid, String npcType) {
        String sql = "SELECT quest_index FROM player_levels WHERE uuid = ? AND npc_type = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, npcType);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("quest_index");
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error getting quest index: " + e.getMessage());
        }
        return 0;
    }

    @Override
    public void setQuestIndex(UUID uuid, String npcType, int index) {
        String sql = "INSERT INTO player_levels (uuid, npc_type, quest_index) VALUES (?, ?, ?) ON CONFLICT(uuid, npc_type) DO UPDATE SET quest_index=excluded.quest_index";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, npcType);
            ps.setInt(3, index);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Error setting quest index: " + e.getMessage());
        }
    }

    @Override
    public java.util.Map<String, Integer> getProgress(UUID uuid) {
        java.util.Map<String, Integer> map = new java.util.HashMap<>();
        String sql = "SELECT progress_key, amount FROM quest_progress WHERE uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getString("progress_key"), rs.getInt("amount"));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error getting progress: " + e.getMessage());
        }
        return map;
    }

    @Override
    public void saveProgress(UUID uuid, java.util.Map<String, Integer> progress) {
        String sql = "INSERT OR REPLACE INTO quest_progress (uuid, progress_key, amount) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            connection.setAutoCommit(false);
            for (java.util.Map.Entry<String, Integer> entry : progress.entrySet()) {
                ps.setString(1, uuid.toString());
                ps.setString(2, entry.getKey());
                ps.setInt(3, entry.getValue());
                ps.addBatch();
            }
            ps.executeBatch();
            connection.commit();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            plugin.getLogger().severe("Error saving progress: " + e.getMessage());
        }
    }

    @Override
    public void setQuestStart(UUID uuid, long time) {
        String sql = "INSERT OR IGNORE INTO quest_timer (uuid, start_time) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setLong(2, time);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Error setting start time: " + e.getMessage());
        }
    }

    @Override
    public void setQuestEnd(UUID uuid, long time) {
        String sql = "UPDATE quest_timer SET end_time = ? WHERE uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, time);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Error setting end time: " + e.getMessage());
        }
    }

    @Override
    public java.util.Map<String, Long> getTopFarmers(int limit) {
        java.util.Map<String, Long> results = new java.util.LinkedHashMap<>();
        String sql = "SELECT uuid, (end_time - start_time) as duration FROM quest_timer WHERE end_time > 0 AND start_time > 0 ORDER BY duration ASC LIMIT ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.put(rs.getString("uuid"), rs.getLong("duration"));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error getting top farmers: " + e.getMessage());
        }
        return results;
    }

    @Override
    public double getBalance(UUID uuid) {
        String sql = "SELECT balance FROM balances WHERE uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble("balance");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error getting balance: " + e.getMessage());
        }
        return 0.0;
    }

    @Override
    public void setBalance(UUID uuid, double amount) {
        String sql = "INSERT OR REPLACE INTO balances (uuid, balance) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setDouble(2, amount);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Error setting balance: " + e.getMessage());
        }
    }
}
