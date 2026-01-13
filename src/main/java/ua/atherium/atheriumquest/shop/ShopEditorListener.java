package ua.atherium.atheriumquest.shop;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import ua.atherium.atheriumquest.AtheriumQuest;
import ua.atherium.atheriumquest.quest.NpcType;

public class ShopEditorListener implements Listener {

    private final AtheriumQuest plugin;
    private final Map<UUID, EditorAction> editors = new HashMap<>();

    private record EditorAction(NpcType type, int level, String field) {}

    public ShopEditorListener(AtheriumQuest plugin) {
        this.plugin = plugin;
    }

    public void toggleEditor(Player player) {
        if (editors.containsKey(player.getUniqueId())) {
            editors.remove(player.getUniqueId());
            player.sendMessage("Exited editor mode.");
        } else {
            editors.put(player.getUniqueId(), null);
            player.sendMessage("Entered shop editor mode. Open a shop menu to edit.");
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!editors.containsKey(player.getUniqueId())) return;
        if (!(event.getInventory().getHolder() instanceof ShopMenu)) return;

        event.setCancelled(true);

        int slot = event.getSlot();
        NpcType type = null;
        int level = -1;

        if (slot >= 10 && slot <= 12) { type = NpcType.FARMER; level = slot - 9; }
        else if (slot >= 19 && slot <= 21) { type = NpcType.ALCHEMIST; level = slot - 18; }
        else if (slot >= 28 && slot <= 30) { type = NpcType.WEAPONS; level = slot - 27; }

        if (type == null) return;

        ShopItem item = plugin.getShopManager().getItem(type, level);
        if (item == null) {
        }

        if (event.getClick() == ClickType.RIGHT) {
            if (player.getInventory().getItemInMainHand().getType() != Material.AIR) {
                 item.setMaterial(player.getInventory().getItemInMainHand().getType());
                 plugin.getShopManager().saveShops();
                 player.sendMessage("Set item material to " + item.getMaterial());
            }
        } else if (event.getClick() == ClickType.LEFT) {
            editors.put(player.getUniqueId(), new EditorAction(type, level, "stock"));
            player.sendMessage("Type new max stock amount in chat:");
            player.closeInventory();
        } else if (event.getClick() == ClickType.MIDDLE) {
            editors.put(player.getUniqueId(), new EditorAction(type, level, "price"));
            player.sendMessage("Type new price in chat:");
            player.closeInventory();
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (editors.containsKey(player.getUniqueId())) {
            EditorAction action = editors.get(player.getUniqueId());
            if (action != null) {
                event.setCancelled(true);
                String msg = event.getMessage();
                org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> {
                    try {
                        double val = Double.parseDouble(msg);
                        ShopItem item = plugin.getShopManager().getItem(action.type, action.level);
                        if (item != null) {
                            if (action.field.equals("stock")) item.setMaxStock((int)val);
                            if (action.field.equals("price")) item.setPrice(val);
                            plugin.getShopManager().saveShops();
                            player.sendMessage("Updated " + action.field + " to " + val);
                        }
                    } catch (NumberFormatException e) {
                        player.sendMessage("Invalid number.");
                    }
                    editors.put(player.getUniqueId(), null);
                });
            }
        }
    }
}
