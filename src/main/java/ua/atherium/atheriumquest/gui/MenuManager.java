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
    private final Map<NpcType, MenuConfig> menuConfigs = new HashMap<>();
    private final Map<NpcType, Map<String, QuestMenuItem>> menuItems = new HashMap<>();

    public MenuManager(AtheriumQuest plugin) {
        this.plugin = plugin;
        loadMenus();
    }

    public void loadMenus() {
        loadMenu(NpcType.FARMER, "farmer.yml");
        loadMenu(NpcType.ALCHEMIST, "alchemist.yml");
        loadMenu(NpcType.WEAPONS, "weapons.yml");
    }

    private void loadMenu(NpcType type, String fileName) {
        File file = new File(plugin.getDataFolder(), "menus/" + fileName);
        if (!file.exists()) {
            plugin.saveResource("menus/" + fileName, false);
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        String title = config.getString("menu_title", "Menu");
        int rows = config.getInt("size", 27) / 9;

        ConfigurationSection itemsSec = config.getConfigurationSection("items");
        Map<String, QuestMenuItem> items = new HashMap<>();
        if (itemsSec != null) {
            for (String key : itemsSec.getKeys(false)) {
                ConfigurationSection is = itemsSec.getConfigurationSection(key);
                items.put(key, new QuestMenuItem(
                    key,
                    is.getInt("slot"),
                    Material.valueOf(is.getString("material", "STONE")),
                    is.getString("name"),
                    is.getStringList("lore")
                ));
            }
        }

        menuConfigs.put(type, new MenuConfig(title, rows));
        menuItems.put(type, items);
    }

    public MenuConfig getMenuConfig(NpcType type) {
        return menuConfigs.get(type);
    }

    public QuestMenuItem getMenuItem(NpcType type, String questId) {
        return menuItems.getOrDefault(type, new HashMap<>()).get(questId);
    }

    public record MenuConfig(String title, int rows) {}
}
