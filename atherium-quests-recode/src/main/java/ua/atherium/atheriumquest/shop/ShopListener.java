package ua.atherium.atheriumquest.shop;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import ua.atherium.atheriumquest.AtheriumQuests;

import java.util.List;

public class ShopListener implements Listener {

    private final AtheriumQuests plugin;

    public ShopListener(AtheriumQuests plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!(event.getInventory().getHolder() instanceof ShopHolder)) return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack current = event.getCurrentItem();
        if (current == null || current.getType() == Material.AIR) return;

        int slot = event.getSlot();

        ConfigurationSection itemsSec = plugin.getConfigManager().getShopConfig().getConfigurationSection("items");
        if (itemsSec != null) {
            for (String key : itemsSec.getKeys(false)) {
                ConfigurationSection itemSec = itemsSec.getConfigurationSection(key);
                boolean match = false;
                List<Integer> slots = itemSec.getIntegerList("slots");
                if (slots.contains(slot)) match = true;
                else if (itemSec.getInt("slot", -1) == slot) match = true;

                if (match) {
                    plugin.getShopManager().buyItem(player, key);
                    return;
                }
            }
        }
    }
}
