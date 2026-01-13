package ua.atherium.atheriumquest.shop;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import ua.atherium.atheriumquest.AtheriumQuest;
import ua.atherium.atheriumquest.quest.NpcType;

public class ShopManager {
    private final AtheriumQuest plugin;
    private final Map<NpcType, Map<Integer, ShopItem>> shopItems = new HashMap<>();
    private File file;
    private YamlConfiguration config;

    public ShopManager(AtheriumQuest plugin) {
        this.plugin = plugin;
        loadShops();
    }

    public void loadShops() {
        file = new File(plugin.getDataFolder(), "shops.yml");
        if (!file.exists()) {
            plugin.saveResource("shops.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(file);

        shopItems.clear();
        for (String typeStr : config.getKeys(false)) {
            try {
                NpcType type = NpcType.valueOf(typeStr);
                ConfigurationSection typeSec = config.getConfigurationSection(typeStr);
                Map<Integer, ShopItem> items = new HashMap<>();
                for (String lvlStr : typeSec.getKeys(false)) {
                    int level = Integer.parseInt(lvlStr);
                    ConfigurationSection itemSec = typeSec.getConfigurationSection(lvlStr);
                    Material mat = Material.valueOf(itemSec.getString("material"));
                    double price = itemSec.getDouble("price");
                    int stock = itemSec.getInt("stock");
                    long refill = itemSec.getLong("refill");
                    items.put(level, new ShopItem(mat, price, stock, refill));
                }
                shopItems.put(type, items);
            } catch (Exception e) {
                plugin.getLogger().severe("Error loading shop: " + typeStr);
            }
        }
    }

    public void saveShops() {
        for (Map.Entry<NpcType, Map<Integer, ShopItem>> typeEntry : shopItems.entrySet()) {
            for (Map.Entry<Integer, ShopItem> itemEntry : typeEntry.getValue().entrySet()) {
                String path = typeEntry.getKey().name() + "." + itemEntry.getKey();
                ShopItem item = itemEntry.getValue();
                config.set(path + ".material", item.getMaterial().name());
                config.set(path + ".price", item.getPrice());
                config.set(path + ".stock", item.getMaxStock());
                config.set(path + ".refill", item.getRefillTime());
            }
        }
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ShopItem getItem(NpcType type, int level) {
        return shopItems.getOrDefault(type, new HashMap<>()).get(level);
    }
}
