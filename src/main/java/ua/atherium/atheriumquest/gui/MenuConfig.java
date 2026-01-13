package ua.atherium.atheriumquest.gui;

import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import ua.atherium.atheriumquest.ConfigManager;

public class MenuConfig {
    private final String title;
    private final int rows;
    private final Material fillerMaterial;
    private final List<Integer> fillerSlots;
    private final List<MenuItem> items = new ArrayList<>();

    public record MenuItem(int slot, Material material, String name, List<String> lore, String action) {}

    public MenuConfig(ConfigurationSection section) {
        this.title = section.getString("title", "Menu");
        this.rows = section.getInt("rows", 3);
        this.fillerMaterial = Material.valueOf(section.getString("filler.material", "GRAY_STAINED_GLASS_PANE"));
        this.fillerSlots = section.getIntegerList("filler.slots");

        ConfigurationSection itemsSec = section.getConfigurationSection("items");
        if (itemsSec != null) {
            for (String key : itemsSec.getKeys(false)) {
                ConfigurationSection itemSec = itemsSec.getConfigurationSection(key);
                if (itemSec != null) {
                    items.add(new MenuItem(
                        itemSec.getInt("slot"),
                        Material.valueOf(itemSec.getString("material", "STONE")),
                        itemSec.getString("name", ""),
                        itemSec.getStringList("lore"),
                        itemSec.getString("action", "")
                    ));
                }
            }
        }
    }

    public String getTitle() { return title; }
    public int getRows() { return rows; }
    public Material getFillerMaterial() { return fillerMaterial; }
    public List<MenuItem> getItems() { return items; }
}
