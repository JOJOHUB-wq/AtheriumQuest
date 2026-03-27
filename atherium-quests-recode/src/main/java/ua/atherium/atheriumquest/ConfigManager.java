package ua.atherium.atheriumquest;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

public class ConfigManager {

    private final JavaPlugin plugin;
    private FileConfiguration config;
    private File configFile;
    private FileConfiguration shopConfig;
    private File shopFile;
    private FileConfiguration mainMenuConfig;
    private File mainMenuFile;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadConfigs();
    }

    public void loadConfigs() {

        configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);


        shopFile = new File(plugin.getDataFolder(), "shop.yml");
        if (!shopFile.exists()) {
            plugin.saveResource("shop.yml", false);
        }
        shopConfig = YamlConfiguration.loadConfiguration(shopFile);


        mainMenuFile = new File(plugin.getDataFolder(), "main_menu.yml");
        if (!mainMenuFile.exists()) {
            plugin.saveResource("main_menu.yml", false);
        }
        mainMenuConfig = YamlConfiguration.loadConfiguration(mainMenuFile);
    }

    public void reloadConfigs() {
        loadConfigs();
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public FileConfiguration getShopConfig() {
        return shopConfig;
    }

    public FileConfiguration getMainMenuConfig() {
        return mainMenuConfig;
    }

    public void saveShopConfig() {
        try {
            shopConfig.save(shopFile);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save shop.yml!", e);
        }
    }

    public Component parse(String text) {
        if (text == null) return Component.empty();


        text = text.replace("§", "&");
        text = text.replace("&0", "<black>");
        text = text.replace("&1", "<dark_blue>");
        text = text.replace("&2", "<dark_green>");
        text = text.replace("&3", "<dark_aqua>");
        text = text.replace("&4", "<dark_red>");
        text = text.replace("&5", "<dark_purple>");
        text = text.replace("&6", "<gold>");
        text = text.replace("&7", "<gray>");
        text = text.replace("&8", "<dark_gray>");
        text = text.replace("&9", "<blue>");
        text = text.replace("&a", "<green>");
        text = text.replace("&b", "<aqua>");
        text = text.replace("&c", "<red>");
        text = text.replace("&d", "<light_purple>");
        text = text.replace("&e", "<yellow>");
        text = text.replace("&f", "<white>");

        text = text.replace("&k", "<obfuscated>");
        text = text.replace("&l", "<bold>");
        text = text.replace("&m", "<strikethrough>");
        text = text.replace("&n", "<underlined>");
        text = text.replace("&o", "<italic>");
        text = text.replace("&r", "<reset>");

        return miniMessage.deserialize(text);
    }


    public String getMessage(String key) {
        String msg = config.getString("messages." + key);
        if (msg == null) return "Message not found: " + key;
        return msg.replace("%prefix%", config.getString("prefix", ""));
    }
}
