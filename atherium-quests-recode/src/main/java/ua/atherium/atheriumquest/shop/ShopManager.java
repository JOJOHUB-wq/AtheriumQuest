package ua.atherium.atheriumquest.shop;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ua.atherium.atheriumquest.AtheriumQuests;
import ua.atherium.atheriumquest.quest.NpcType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ShopManager {

    private final AtheriumQuests plugin;

    public ShopManager(AtheriumQuests plugin) {
        this.plugin = plugin;
        plugin.getServer().getScheduler().runTaskTimer(plugin, this::updateShops, 20L, 20L);
    }

    public void openShop(Player player) {
        ConfigurationSection config = plugin.getConfigManager().getShopConfig();
        if (config == null) return;

        String title = config.getString("menu_title", "Shop");
        int size = config.getInt("size", 27);

        Inventory inv = Bukkit.createInventory(new ShopHolder(), size, plugin.getConfigManager().parse(title));

        fillShop(inv, player, config.getConfigurationSection("items"));

        player.openInventory(inv);
    }

    private void fillShop(Inventory inv, Player player, ConfigurationSection itemsSec) {
        if (itemsSec == null) return;

        for (String key : itemsSec.getKeys(false)) {
            ConfigurationSection itemSec = itemsSec.getConfigurationSection(key);
            if (itemSec == null) continue;

            ItemStack item = buildShopItem(player, key, itemSec);

            List<Integer> slots = itemSec.getIntegerList("slots");
            if (slots.isEmpty()) {
                int slot = itemSec.getInt("slot", -1);
                if (slot >= 0 && slot < inv.getSize()) {
                    inv.setItem(slot, item);
                }
            } else {
                for (int slot : slots) {
                    if (slot >= 0 && slot < inv.getSize()) {
                        inv.setItem(slot, item);
                    }
                }
            }
        }
    }

    private ItemStack buildShopItem(Player player, String itemId, ConfigurationSection section) {

        int required = section.getInt("required_levels_completed", 0);
        String npcId = section.getString("npc");

        boolean locked = false;
        if (required > 0 && npcId != null) {
            int completed = plugin.getStorageManager().getStorage().getCompletedLevels(player.getUniqueId(), npcId);
            if (completed < required) {
                locked = true;
            }
        }

        if (locked) {

            String matName = section.getString("locked_material", "BARRIER");
            Material mat = Material.matchMaterial(matName);
            if (mat == null) mat = Material.BARRIER;

            ItemStack item = new ItemStack(mat);
            ItemMeta meta = item.getItemMeta();

            String name = section.getString("locked_display_name");
            if (name != null) {
                name = replaceShopPlaceholders(name, player, itemId, section);
                meta.displayName(plugin.getConfigManager().parse(name));
            }

            List<String> lore = section.getStringList("locked_lore");
            if (lore != null) {
                List<net.kyori.adventure.text.Component> loreComp = new ArrayList<>();
                for (String l : lore) {
                    l = replaceShopPlaceholders(l, player, itemId, section);
                    loreComp.add(plugin.getConfigManager().parse(l));
                }
                meta.lore(loreComp);
            }
            item.setItemMeta(meta);
            return item;
        }



        int max = section.getInt("max_purchase", -1);
        int refreshTime = section.getInt("refresh_time", 0);

        int purchased = plugin.getStorageManager().getStorage().getShopPurchaseCount(player.getUniqueId(), itemId);
        long lastRefresh = plugin.getStorageManager().getStorage().getShopPurchaseTime(player.getUniqueId(), itemId);


        if (refreshTime > 0 && lastRefresh > 0) {
            long elapsed = (System.currentTimeMillis() - lastRefresh) / 1000;
            if (elapsed >= refreshTime) {










                purchased = 0;
            }
        }

        int available = (max == -1) ? 999 : (max - purchased);
        if (available < 0) available = 0;

        boolean soldOut = (available == 0);

        if (soldOut) {
            String matName = section.getString("material", "STONE");




            Material mat = Material.matchMaterial(matName);
            if (mat == null) mat = Material.STONE;

            ItemStack item = new ItemStack(mat);
            ItemMeta meta = item.getItemMeta();

            String name = section.getString("sold_out_display_name");
            if (name != null) {
                name = replaceShopPlaceholders(name, player, itemId, section);
                meta.displayName(plugin.getConfigManager().parse(name));
            }

            List<String> lore = section.getStringList("sold_out_lore");
             if (lore != null) {
                List<net.kyori.adventure.text.Component> loreComp = new ArrayList<>();
                for (String l : lore) {
                    l = replaceShopPlaceholders(l, player, itemId, section);
                    loreComp.add(plugin.getConfigManager().parse(l));
                }
                meta.lore(loreComp);
            }
            item.setItemMeta(meta);
            return item;
        }


        String matName = section.getString("material", "STONE");
        Material mat = Material.matchMaterial(matName);
        if (mat == null) mat = Material.STONE;

        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();

        if (section.contains("display_name")) {
            String name = section.getString("display_name");
            name = replaceShopPlaceholders(name, player, itemId, section);
            meta.displayName(plugin.getConfigManager().parse(name));
        }

        if (section.contains("lore")) {
            List<String> lore = section.getStringList("lore");
            List<net.kyori.adventure.text.Component> loreComp = new ArrayList<>();
            for (String l : lore) {
                l = replaceShopPlaceholders(l, player, itemId, section);
                loreComp.add(plugin.getConfigManager().parse(l));
            }
            meta.lore(loreComp);
        }

        if (section.getBoolean("enchanted", false)) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        item.setItemMeta(meta);
        return item;
    }

    private String replaceShopPlaceholders(String text, Player player, String itemId, ConfigurationSection section) {
        if (text == null) return "";

        int purchased = plugin.getStorageManager().getStorage().getShopPurchaseCount(player.getUniqueId(), itemId);
        long lastRefresh = plugin.getStorageManager().getStorage().getShopPurchaseTime(player.getUniqueId(), itemId);
        int max = section.getInt("max_purchase", -1);
        int refreshTime = section.getInt("refresh_time", 0);


        if (refreshTime > 0 && lastRefresh > 0) {
             long elapsed = (System.currentTimeMillis() - lastRefresh) / 1000;
             if (elapsed >= refreshTime) {
                 purchased = 0;
             }
        }

        int available = (max == -1) ? 999 : (max - purchased);
        if (available < 0) available = 0;

        text = text.replace("%available%", String.valueOf(available));
        text = text.replace("%max%", String.valueOf(max));
        text = text.replace("%price%", String.valueOf(section.getInt("price", 0)));





        long timeLeft = 0;
        if (refreshTime > 0) {
            if (lastRefresh == 0) {
















                if (purchased > 0) {
                     if (lastRefresh == 0) lastRefresh = System.currentTimeMillis();
                     long nextRefresh = lastRefresh + (refreshTime * 1000L);
                     timeLeft = (nextRefresh - System.currentTimeMillis()) / 1000;
                     if (timeLeft < 0) timeLeft = 0;
                }
            } else {
                 long nextRefresh = lastRefresh + (refreshTime * 1000L);
                 timeLeft = (nextRefresh - System.currentTimeMillis()) / 1000;
                 if (timeLeft < 0) timeLeft = 0;
            }
        }

        text = text.replace("%timer%", formatTime(timeLeft));


        int required = section.getInt("required_levels_completed", 0);
        String npcId = section.getString("npc");
        if (npcId != null) {
            int completed = plugin.getStorageManager().getStorage().getCompletedLevels(player.getUniqueId(), npcId);
            text = text.replace("%required%", String.valueOf(required));
            text = text.replace("%player_completed%", String.valueOf(completed));
        }

        return text;
    }

    private String formatTime(long seconds) {
        if (seconds <= 0) return "0с";
        long h = seconds / 3600;
        long m = (seconds % 3600) / 60;
        long s = seconds % 60;

        StringBuilder sb = new StringBuilder();
        if (h > 0) sb.append(h).append("ч ");
        if (m > 0 || h > 0) sb.append(m).append("м ");
        sb.append(s).append("с");
        return sb.toString().trim();
    }

    public void updateShops() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Inventory top = player.getOpenInventory().getTopInventory();
            if (top.getHolder() instanceof ShopHolder) {

                ConfigurationSection config = plugin.getConfigManager().getShopConfig().getConfigurationSection("items");
                fillShop(top, player, config);
            }
        }
    }

    public void buyItem(Player player, String itemId) {
        ConfigurationSection config = plugin.getConfigManager().getShopConfig().getConfigurationSection("items." + itemId);
        if (config == null) return;


        int required = config.getInt("required_levels_completed", 0);
        String npcId = config.getString("npc");
        if (required > 0 && npcId != null) {
            int completed = plugin.getStorageManager().getStorage().getCompletedLevels(player.getUniqueId(), npcId);
            if (completed < required) {
                 String msg = plugin.getConfigManager().getMessage("shop_item_locked")
                     .replace("%required%", String.valueOf(required));
                 player.sendMessage(plugin.getConfigManager().parse(msg));
                 return;
            }
        }


        int max = config.getInt("max_purchase", -1);
        int purchased = plugin.getStorageManager().getStorage().getShopPurchaseCount(player.getUniqueId(), itemId);
        long lastRefresh = plugin.getStorageManager().getStorage().getShopPurchaseTime(player.getUniqueId(), itemId);
        int refreshTime = config.getInt("refresh_time", 0);

        if (refreshTime > 0 && lastRefresh > 0) {
             long elapsed = (System.currentTimeMillis() - lastRefresh) / 1000;
             if (elapsed >= refreshTime) {
                 purchased = 0;

             }
        }

        if (max != -1 && purchased >= max) {
            long nextRefresh = (lastRefresh > 0 ? lastRefresh : System.currentTimeMillis()) + (refreshTime * 1000L);
            long timeLeft = (nextRefresh - System.currentTimeMillis()) / 1000;
            String msg = plugin.getConfigManager().getMessage("shop_sold_out")
                .replace("%time%", formatTime(timeLeft));
            player.sendMessage(plugin.getConfigManager().parse(msg));
            return;
        }


        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage(plugin.getConfigManager().parse(plugin.getConfigManager().getMessage("shop_inventory_full")));
            return;
        }


        double price = config.getDouble("price", 0);
        if (plugin.getEconomy() != null) {
            if (!plugin.getEconomy().has(player, price)) {
                String msg = plugin.getConfigManager().getMessage("shop_insufficient_funds")
                    .replace("%price%", String.valueOf(price));
                player.sendMessage(plugin.getConfigManager().parse(msg));
                return;
            }
        }


        if (plugin.getEconomy() != null) {
            plugin.getEconomy().withdrawPlayer(player, price);
        }


        String matName = config.getString("material", "STONE");
        Material mat = Material.matchMaterial(matName);
        if (mat == null) mat = Material.STONE;
        int amount = config.getInt("amount", 1);

        ItemStack item = new ItemStack(mat, amount);
        player.getInventory().addItem(item);



        long newRefresh = lastRefresh;
        if (purchased == 0) {
            newRefresh = System.currentTimeMillis();
        } else if (lastRefresh == 0) {




             if (refreshTime > 0 && lastRefresh > 0) {
                 long elapsed = (System.currentTimeMillis() - lastRefresh) / 1000;
                 if (elapsed >= refreshTime) {

                     purchased = 0;
                     newRefresh = System.currentTimeMillis();
                 }
             } else if (lastRefresh == 0) {
                 newRefresh = System.currentTimeMillis();
             }
        }

        plugin.getStorageManager().getStorage().setShopPurchase(player.getUniqueId(), itemId, purchased + 1, newRefresh);


        String msg = plugin.getConfigManager().getMessage("shop_item_purchased")
            .replace("%amount%", String.valueOf(amount))
            .replace("%item%", config.getString("display_name", matName))
            .replace("%price%", String.valueOf(price));
        player.sendMessage(plugin.getConfigManager().parse(msg));

        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);


        openShop(player);
    }
}
