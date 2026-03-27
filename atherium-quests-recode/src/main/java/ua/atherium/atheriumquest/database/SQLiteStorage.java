package ua.atherium.atheriumquest.database;

import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.sql.*;
import java.util.UUID;
import java.util.logging.Level;

public class SQLiteStorage implements Storage {

    private final JavaPlugin plugin;
    private Connection connection;
    private final String dbFile;

    public SQLiteStorage(JavaPlugin plugin, String dbFile) {
        this.plugin = plugin;
        this.dbFile = dbFile;
    }

    @Override
    public void init() {
        try {
            Class.forName("org.sqlite.JDBC");
            File file = new File(plugin.getDataFolder(), dbFile);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            connection = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
            createTables();
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Could not init SQLite database", e);
        }
    }

    private void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS atq_players (" +
                    "uuid VARCHAR(36) PRIMARY KEY, " +
                    "name VARCHAR(16), " +
                    "last_seen BIGINT)");

            stmt.execute("CREATE TABLE IF NOT EXISTS atq_npc_levels (" +
                    "uuid VARCHAR(36), " +
                    "npc VARCHAR(32), " +
                    "current_level INT DEFAULT 1, " +
                    "completed_levels INT DEFAULT 0, " +
                    "PRIMARY KEY (uuid, npc))");

            stmt.execute("CREATE TABLE IF NOT EXISTS atq_quest_progress (" +
                    "uuid VARCHAR(36), " +
                    "npc VARCHAR(32), " +
                    "level INT, " +
                    "quest_id VARCHAR(64), " +
                    "progress INT DEFAULT 0, " +
                    "completed BOOLEAN DEFAULT 0, " +
                    "PRIMARY KEY (uuid, npc, level, quest_id))");

            stmt.execute("CREATE TABLE IF NOT EXISTS atq_shop_purchases (" +
                    "uuid VARCHAR(36), " +
                    "item_id VARCHAR(64), " +
                    "purchased_count INT DEFAULT 0, " +
                    "last_refresh BIGINT DEFAULT 0, " +
                    "PRIMARY KEY (uuid, item_id))");
        }
    }

    @Override
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getLevel(UUID uuid, String npc) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT current_level FROM atq_npc_levels WHERE uuid = ? AND npc = ?")) {
            ps.setString(1, uuid.toString());
            ps.setString(2, npc);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("current_level");
        } catch (SQLException e) { e.printStackTrace(); }
        return 1;
    }

    @Override
    public void setLevel(UUID uuid, String npc, int level) {
        try (PreparedStatement ps = connection.prepareStatement("INSERT OR REPLACE INTO atq_npc_levels (uuid, npc, current_level, completed_levels) VALUES (?, ?, ?, (SELECT completed_levels FROM atq_npc_levels WHERE uuid=? AND npc=?))")) {





            updateLevel(uuid, npc, level);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void updateLevel(UUID uuid, String npc, int level) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("INSERT INTO atq_npc_levels (uuid, npc, current_level, completed_levels) VALUES (?, ?, ?, 0) ON CONFLICT(uuid, npc) DO UPDATE SET current_level=?")) {
            ps.setString(1, uuid.toString());
            ps.setString(2, npc);
            ps.setInt(3, level);
            ps.setInt(4, level);
            ps.executeUpdate();
        }
    }

    @Override
    public int getQuestProgress(UUID uuid, String npc, int level, String questId) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT progress FROM atq_quest_progress WHERE uuid=? AND npc=? AND level=? AND quest_id=?")) {
            ps.setString(1, uuid.toString());
            ps.setString(2, npc);
            ps.setInt(3, level);
            ps.setString(4, questId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("progress");
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    @Override
    public void setQuestProgress(UUID uuid, String npc, int level, String questId, int progress) {
         try (PreparedStatement ps = connection.prepareStatement("INSERT INTO atq_quest_progress (uuid, npc, level, quest_id, progress, completed) VALUES (?, ?, ?, ?, ?, 0) ON CONFLICT(uuid, npc, level, quest_id) DO UPDATE SET progress=?")) {
            ps.setString(1, uuid.toString());
            ps.setString(2, npc);
            ps.setInt(3, level);
            ps.setString(4, questId);
            ps.setInt(5, progress);
            ps.setInt(6, progress);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public boolean isQuestCompleted(UUID uuid, String npc, int level, String questId) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT completed FROM atq_quest_progress WHERE uuid=? AND npc=? AND level=? AND quest_id=?")) {
            ps.setString(1, uuid.toString());
            ps.setString(2, npc);
            ps.setInt(3, level);
            ps.setString(4, questId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getBoolean("completed");
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    @Override
    public void setQuestCompleted(UUID uuid, String npc, int level, String questId, boolean completed) {
         try (PreparedStatement ps = connection.prepareStatement("INSERT INTO atq_quest_progress (uuid, npc, level, quest_id, progress, completed) VALUES (?, ?, ?, ?, 0, ?) ON CONFLICT(uuid, npc, level, quest_id) DO UPDATE SET completed=?")) {
            ps.setString(1, uuid.toString());
            ps.setString(2, npc);
            ps.setInt(3, level);
            ps.setString(4, questId);
            ps.setBoolean(5, completed);
            ps.setBoolean(6, completed);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public int getShopPurchaseCount(UUID uuid, String itemId) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT purchased_count FROM atq_shop_purchases WHERE uuid=? AND item_id=?")) {
            ps.setString(1, uuid.toString());
            ps.setString(2, itemId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("purchased_count");
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    @Override
    public long getShopPurchaseTime(UUID uuid, String itemId) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT last_refresh FROM atq_shop_purchases WHERE uuid=? AND item_id=?")) {
            ps.setString(1, uuid.toString());
            ps.setString(2, itemId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getLong("last_refresh");
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    @Override
    public void setShopPurchase(UUID uuid, String itemId, int count, long lastRefresh) {
        try (PreparedStatement ps = connection.prepareStatement("INSERT INTO atq_shop_purchases (uuid, item_id, purchased_count, last_refresh) VALUES (?, ?, ?, ?) ON CONFLICT(uuid, item_id) DO UPDATE SET purchased_count=?, last_refresh=?")) {
            ps.setString(1, uuid.toString());
            ps.setString(2, itemId);
            ps.setInt(3, count);
            ps.setLong(4, lastRefresh);
            ps.setInt(5, count);
            ps.setLong(6, lastRefresh);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public int getCompletedLevels(UUID uuid, String npc) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT completed_levels FROM atq_npc_levels WHERE uuid=? AND npc=?")) {
            ps.setString(1, uuid.toString());
            ps.setString(2, npc);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("completed_levels");
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    @Override
    public void setCompletedLevels(UUID uuid, String npc, int levels) {
         try (PreparedStatement ps = connection.prepareStatement("INSERT INTO atq_npc_levels (uuid, npc, current_level, completed_levels) VALUES (?, ?, 1, ?) ON CONFLICT(uuid, npc) DO UPDATE SET completed_levels=?")) {
            ps.setString(1, uuid.toString());
            ps.setString(2, npc);
            ps.setInt(3, levels);
            ps.setInt(4, levels);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public int getTotalQuestsCompleted(UUID uuid) {
         try (PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM atq_quest_progress WHERE uuid=? AND completed=1")) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    @Override
    public int getTotalLevelsCompleted(UUID uuid) {
         try (PreparedStatement ps = connection.prepareStatement("SELECT SUM(completed_levels) FROM atq_npc_levels WHERE uuid=?")) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }
}
