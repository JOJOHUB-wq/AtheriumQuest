package ua.atherium.atheriumquest.shop;

import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ua.atherium.atheriumquest.AtheriumQuest;
import ua.atherium.atheriumquest.gui.Menu;
import ua.atherium.atheriumquest.quest.NpcType;
import ua.atherium.atheriumquest.user.UserProfile;

public class ShopMenu extends Menu {

    private final AtheriumQuest plugin;
    private final Player player;

    public ShopMenu(AtheriumQuest plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    public void open() {
        ua.atherium.atheriumquest.gui.MenuConfig config = new ua.atherium.atheriumquest.gui.MenuConfig(plugin.getConfigManager().getConfig().getConfigurationSection("menus.shop"));
        Inventory inv = Bukkit.createInventory(this, config.getRows() * 9, plugin.getConfigManager().parse(config.getTitle()));
        updateInventory(inv, config);
        player.openInventory(inv);
        startLiveUpdate(inv, config);
    }

    private void updateInventory(Inventory inv, ua.atherium.atheriumquest.gui.MenuConfig config) {
        UserProfile user = plugin.getUserManager().getUser(player.getUniqueId());

        ItemStack bg = new ItemStack(config.getFillerMaterial());
        ItemMeta bgMeta = bg.getItemMeta();
        bgMeta.displayName(Component.empty());
        bg.setItemMeta(bgMeta);

        for (int i = 0; i < inv.getSize(); i++) {
             if (inv.getItem(i) == null || inv.getItem(i).getType() == config.getFillerMaterial()) {
                inv.setItem(i, bg);
             }
        }

        setupRow(inv, user, NpcType.FARMER, 10);
        setupRow(inv, user, NpcType.ALCHEMIST, 19);
        setupRow(inv, user, NpcType.WEAPONS, 28);
    }

    private void startLiveUpdate(Inventory inv, ua.atherium.atheriumquest.gui.MenuConfig config) {
        new org.bukkit.scheduler.BukkitRunnable() {
            @Override
            public void run() {
                if (player.getOpenInventory().getTopInventory() != inv) {
                    this.cancel();
                    return;
                }
                updateInventory(inv, config);
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void setupRow(Inventory inv, UserProfile user, NpcType type, int startSlot) {
        MiniMessage mm = MiniMessage.miniMessage();
        int userLevel = user.getLevel(type);

        for (int i = 1; i <= 3; i++) {
            int slot = startSlot + (i - 1);
            if (userLevel >= i) {
                 if (userLevel > i) {
                     ShopItem item = plugin.getShopManager().getItem(type, i);
                     if (item != null) {
                         item.checkRefill();

                         ItemStack is = new ItemStack(item.getMaterial());
                         if (item.getCurrentStock() <= 0) {
                             is = new ItemStack(Material.GRAY_DYE);
                         }

                         ItemMeta meta = is.getItemMeta();
                         List<Component> lore = new ArrayList<>();
                         lore.add(mm.deserialize("<gray>Price: <gold>" + item.getPrice()));
                         lore.add(mm.deserialize("<gray>Stock: <yellow>" + item.getCurrentStock() + "/" + item.getMaxStock()));

                         long secondsLeft = (item.getNextRefillTime() - System.currentTimeMillis()) / 1000;
                         if (secondsLeft < 0) secondsLeft = 0;

                         String timerMsg = plugin.getConfigManager().getConfig().getString("messages.shop_restock_timer", "<gray>Refill: {time}s")
                                 .replace("{time}", String.valueOf(secondsLeft));
                         lore.add(plugin.getConfigManager().parse(timerMsg));

                         meta.lore(lore);
                         is.setItemMeta(meta);
                         inv.setItem(slot, is);
                     }
                 } else {
                     ItemStack locked = new ItemStack(Material.BARRIER);
                     ItemMeta meta = locked.getItemMeta();
                     meta.displayName(mm.deserialize("<red>Locked"));
                     meta.lore(List.of(mm.deserialize("<gray>Complete quests first")));
                     locked.setItemMeta(meta);
                     inv.setItem(slot, locked);
                 }
            } else {
                 ItemStack locked = new ItemStack(Material.BARRIER);
                 ItemMeta meta = locked.getItemMeta();
                 meta.displayName(mm.deserialize("<red>Locked"));
                 meta.lore(List.of(mm.deserialize("<gray>Complete quests first")));
                 locked.setItemMeta(meta);
                 inv.setItem(slot, locked);
            }
        }
    }

    @Override
    public void handleMenu(InventoryClickEvent event) {
        if (event.getCurrentItem() == null) return;

        int slot = event.getSlot();
        NpcType type = null;
        int level = -1;

        if (slot >= 10 && slot <= 12) {
             type = NpcType.FARMER;
             level = slot - 9;
        } else if (slot >= 19 && slot <= 21) {
             type = NpcType.ALCHEMIST;
             level = slot - 18;
        } else if (slot >= 28 && slot <= 30) {
             type = NpcType.WEAPONS;
             level = slot - 27;
        }

        if (type != null) {
             ShopItem item = plugin.getShopManager().getItem(type, level);
             UserProfile user = plugin.getUserManager().getUser(player.getUniqueId());

             if (user.getLevel(type) > level && item != null) {
                  if (item.getCurrentStock() > 0) {
                      double price = item.getPrice();
                      if (plugin.getEconomyManager().getBalance(player.getUniqueId()) >= price) {
                          plugin.getEconomyManager().withdraw(player.getUniqueId(), price);
                          item.setStock(item.getCurrentStock() - 1);
                          player.getInventory().addItem(new ItemStack(item.getMaterial()));
                          player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Purchased " + item.getMaterial().name()));
                          open();
                      } else {
                          player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Not enough money!"));
                      }
                  } else {
                      player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Out of stock!"));
                  }
             }
        }
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}
