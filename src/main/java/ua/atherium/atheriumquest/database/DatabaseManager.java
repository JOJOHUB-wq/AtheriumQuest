package ua.atherium.atheriumquest.database;

import org.bukkit.plugin.java.JavaPlugin;
import ua.atherium.atheriumquest.ConfigManager;

public class DatabaseManager {

    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private Storage storage;

    public DatabaseManager(JavaPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    public void init() {
        String type = configManager.getConfig().getString("database.type", "sqlite");
        if (type.equalsIgnoreCase("sqlite")) {
            try {
                storage = new SQLiteStorage(plugin);
                storage.init();
                plugin.getLogger().info("SQLite database connected.");
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to connect to SQLite! Fallback to YAML.");
                e.printStackTrace();
                storage = new YamlStorage(plugin);
                storage.init();
            }
        } else {
            storage = new YamlStorage(plugin);
            storage.init();
            plugin.getLogger().info("Using YAML storage.");
        }
    }

    public void close() {
        if (storage != null) {
            storage.close();
        }
    }

    public Storage getStorage() {
        return storage;
    }
}
