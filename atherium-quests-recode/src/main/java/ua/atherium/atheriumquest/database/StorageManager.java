package ua.atherium.atheriumquest.database;

import org.bukkit.plugin.java.JavaPlugin;
import ua.atherium.atheriumquest.ConfigManager;

public class StorageManager {

    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private Storage storage;

    public StorageManager(JavaPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        init();
    }

    private void init() {
        String type = configManager.getConfig().getString("storage.type", "YAML");
        if (type.equalsIgnoreCase("SQLITE")) {
            String file = configManager.getConfig().getString("storage.sqlite.file", "data.db");
            storage = new SQLiteStorage(plugin, file);
        } else if (type.equalsIgnoreCase("MYSQL")) {








            plugin.getLogger().warning("MySQL not fully implemented in this version. Using SQLite.");
            storage = new SQLiteStorage(plugin, "data.db");
        } else {
            storage = new YamlStorage(plugin);
        }
        storage.init();
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
