package ua.atherium.atheriumquest;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigManager {

    private final JavaPlugin plugin;
    private FileConfiguration config;
    private File configFile;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Pattern hexPattern = Pattern.compile("&#([A-Fa-f0-9]{6})");

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public void reloadConfig() {
        loadConfig();
    }

    public Component parse(String text) {
        if (text == null) return Component.empty();

        Matcher matcher = hexPattern.matcher(text);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(buffer, "<#" + matcher.group(1) + ">");
        }
        matcher.appendTail(buffer);

        return miniMessage.deserialize(buffer.toString());
    }
}
