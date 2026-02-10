package ua.atherium.atheriumquest.database;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;

public class YamlStorage implements Storage {

    private final JavaPlugin plugin;
    private final File dataFolder;

    public YamlStorage(JavaPlugin plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "data");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }

    private File getPlayerFile(UUID uuid) {
        return new File(dataFolder, uuid.toString() + ".yml");
    }

    private YamlConfiguration getPlayerConfig(UUID uuid) {
        return YamlConfiguration.loadConfiguration(getPlayerFile(uuid));
    }

    private void savePlayerConfig(UUID uuid, YamlConfiguration config) {
        try {
            config.save(getPlayerFile(uuid));
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save data for " + uuid, e);
        }
    }

    @Override
    public void init() {

    }

    @Override
    public void close() {

    }

    @Override
    public int getLevel(UUID uuid, String npcType) {
        return getPlayerConfig(uuid).getInt(npcType + ".current_level", 1);
    }

    @Override
    public void setLevel(UUID uuid, String npcType, int level) {
        YamlConfiguration config = getPlayerConfig(uuid);
        config.set(npcType + ".current_level", level);
        savePlayerConfig(uuid, config);
    }

    @Override
    public int getQuestProgress(UUID uuid, String npcType, int level, String questId) {
        return getPlayerConfig(uuid).getInt(npcType + ".quests.level_" + level + "." + questId + ".progress", 0);
    }

    @Override
    public void setQuestProgress(UUID uuid, String npcType, int level, String questId, int progress) {
        YamlConfiguration config = getPlayerConfig(uuid);
        config.set(npcType + ".quests.level_" + level + "." + questId + ".progress", progress);
        savePlayerConfig(uuid, config);
    }

    @Override
    public boolean isQuestCompleted(UUID uuid, String npcType, int level, String questId) {
        return getPlayerConfig(uuid).getBoolean(npcType + ".quests.level_" + level + "." + questId + ".completed", false);
    }

    @Override
    public void setQuestCompleted(UUID uuid, String npcType, int level, String questId, boolean completed) {
        YamlConfiguration config = getPlayerConfig(uuid);
        config.set(npcType + ".quests.level_" + level + "." + questId + ".completed", completed);
        if (completed) {
            config.set(npcType + ".quests.level_" + level + "." + questId + ".completed_at", System.currentTimeMillis());
        }
        savePlayerConfig(uuid, config);
    }

    @Override
    public int getShopPurchaseCount(UUID uuid, String itemId) {
        return getPlayerConfig(uuid).getInt("shop." + itemId + ".purchased", 0);
    }

    @Override
    public long getShopPurchaseTime(UUID uuid, String itemId) {
        return getPlayerConfig(uuid).getLong("shop." + itemId + ".last_refresh", 0);
    }

    @Override
    public void setShopPurchase(UUID uuid, String itemId, int count, long lastRefresh) {
        YamlConfiguration config = getPlayerConfig(uuid);
        config.set("shop." + itemId + ".purchased", count);
        config.set("shop." + itemId + ".last_refresh", lastRefresh);
        savePlayerConfig(uuid, config);
    }

    @Override
    public int getCompletedLevels(UUID uuid, String npcType) {
        return getPlayerConfig(uuid).getInt(npcType + ".completed_levels", 0);
    }

    @Override
    public void setCompletedLevels(UUID uuid, String npcType, int levels) {
        YamlConfiguration config = getPlayerConfig(uuid);
        config.set(npcType + ".completed_levels", levels);
        savePlayerConfig(uuid, config);
    }

    @Override
    public int getTotalQuestsCompleted(UUID uuid) {


        YamlConfiguration config = getPlayerConfig(uuid);
        int count = 0;
        for (String key : config.getKeys(true)) {
            if (key.endsWith(".completed") && config.getBoolean(key)) {
                count++;
            }
        }
        return count;
    }

    @Override
    public int getTotalLevelsCompleted(UUID uuid) {
        YamlConfiguration config = getPlayerConfig(uuid);
        int count = 0;
        for (String key : config.getKeys(false)) {
            if (!key.equals("shop") && !key.equals("uuid") && !key.equals("name") && !key.equals("last_seen")) {
                count += config.getInt(key + ".completed_levels", 0);
            }
        }
        return count;
    }
}
