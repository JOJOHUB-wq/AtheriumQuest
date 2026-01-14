package ua.atherium.atheriumquest.gui;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import ua.atherium.atheriumquest.AtheriumQuest;
import ua.atherium.atheriumquest.quest.NpcType;

public class MenuManager {

    private final AtheriumQuest plugin;
    private final Map<NpcType, Map<Integer, MenuConfig>> menuConfigs = new HashMap<>();

    public MenuManager(AtheriumQuest plugin) {
        this.plugin = plugin;
        loadMenus();
    }

    public void loadMenus() {
        menuConfigs.clear();
        loadNpcMenus(NpcType.FARMER, "farmer");
        loadNpcMenus(NpcType.ALCHEMIST, "alchemist");
        loadNpcMenus(NpcType.WEAPONS, "weapons");
    }

    private void loadNpcMenus(NpcType type, String folderName) {
        File folder = new File(plugin.getDataFolder(), folderName + "/menus");
        if (!folder.exists()) {
            plugin.saveResource(folderName + "/menus/level_1.yml", false);
            plugin.saveResource(folderName + "/menus/level_2.yml", false);
        }

        Map<Integer, MenuConfig> levels = new HashMap<>();

        if (folder.exists() && folder.isDirectory()) {
            for (File file : folder.listFiles()) {
                if (file.getName().startsWith("level_") && file.getName().endsWith(".yml")) {
                    try {
                        String numStr = file.getName().replace("level_", "").replace(".yml", "");
                        int level = Integer.parseInt(numStr);

                        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

                        String title = config.getString("menu_title", "Menu");
                        int rows = config.getInt("size", 27) / 9;

                        Map<String, QuestMenuItem> items = new HashMap<>();
                        for (String key : config.getKeys(false)) {
                            if (key.equals("menu_title") || key.equals("size")) continue;

                            ConfigurationSection is = config.getConfigurationSection(key);
                            if (is != null) {
                                items.put(key, new QuestMenuItem(
                                    key,
                                    is.getInt("slot"),
                                    Material.valueOf(is.getString("material", "STONE")),
                                    is.getString("name"),
                                    is.getStringList("lore")
                                ));
                            }
                        }

                        levels.put(level, new MenuConfig(title, rows, items));

                    } catch (Exception e) {
                        plugin.getLogger().severe("Error loading menu " + file.getName());
                        e.printStackTrace();
                    }
                }
            }
        }
        menuConfigs.put(type, levels);
    }

    public MenuConfig getMenuConfig(NpcType type, int level) {
        return menuConfigs.getOrDefault(type, new HashMap<>()).get(level);
    }

    public record MenuConfig(String title, int rows, Map<String, QuestMenuItem> items) {}
}
