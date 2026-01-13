package ua.atherium.atheriumquest.database;

import java.io.File;
import java.io.IOException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.UUID;

public class YamlStorage implements Storage {

    private final JavaPlugin plugin;
    private File file;
    private YamlConfiguration config;

    public YamlStorage(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public int getQuestIndex(UUID uuid, String npcType) {
        return config.getInt(uuid.toString() + "." + npcType + ".questIndex", 0);
    }

    @Override
    public void setQuestIndex(UUID uuid, String npcType, int index) {
        config.set(uuid.toString() + "." + npcType + ".questIndex", index);
        save();
    }

    @Override
    public void init() {
        file = new File(plugin.getDataFolder(), "balances.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create balances.yml!");
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    @Override
    public void close() {
        save();
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save balances.yml!");
        }
    }

    @Override
    public int getLevel(UUID uuid, String npcType) {
        return config.getInt(uuid.toString() + "." + npcType + ".level", 1);
    }

    @Override
    public void setLevel(UUID uuid, String npcType, int level) {
        config.set(uuid.toString() + "." + npcType + ".level", level);
        save();
    }

    @Override
    public java.util.Map<String, Integer> getProgress(UUID uuid) {
        java.util.Map<String, Integer> map = new java.util.HashMap<>();
        org.bukkit.configuration.ConfigurationSection sec = config.getConfigurationSection(uuid.toString() + ".progress");
        if (sec != null) {
            for (String key : sec.getKeys(false)) {
                map.put(key, sec.getInt(key));
            }
        }
        return map;
    }

    @Override
    public void saveProgress(UUID uuid, java.util.Map<String, Integer> progress) {
        if (progress.isEmpty()) return;
        org.bukkit.configuration.ConfigurationSection sec = config.createSection(uuid.toString() + ".progress");
        for (java.util.Map.Entry<String, Integer> entry : progress.entrySet()) {
            sec.set(entry.getKey(), entry.getValue());
        }
        save();
    }

    @Override
    public void setQuestStart(UUID uuid, long time) {
        config.set(uuid.toString() + ".start_time", time);
        save();
    }

    @Override
    public void setQuestEnd(UUID uuid, long time) {
        config.set(uuid.toString() + ".end_time", time);
        save();
    }

    @Override
    public java.util.Map<String, Long> getTopFarmers(int limit) {
        java.util.Map<String, Long> results = new java.util.HashMap<>();
        for (String uuidStr : config.getKeys(false)) {
            long start = config.getLong(uuidStr + ".start_time", 0);
            long end = config.getLong(uuidStr + ".end_time", 0);
            if (start > 0 && end > 0 && end > start) {
                results.put(uuidStr, end - start);
            }
        }
        return results;
    }

    @Override
    public double getBalance(UUID uuid) {
        return config.getDouble(uuid.toString() + ".balance", 0.0);
    }

    @Override
    public void setBalance(UUID uuid, double amount) {
        config.set(uuid.toString() + ".balance", amount);
        save();
    }
}
