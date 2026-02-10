package ua.atherium.atheriumquest.command;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import ua.atherium.atheriumquest.AtheriumQuests;
import ua.atherium.atheriumquest.shop.ShopHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShopEditorListener implements Listener {

    private final AtheriumQuests plugin;
    private final Map<UUID, EditorSession> sessions = new HashMap<>();

    public ShopEditorListener(AtheriumQuests plugin) {
        this.plugin = plugin;
    }

    public void startSession(Player player) {
        sessions.put(player.getUniqueId(), new EditorSession(player));
        plugin.getShopManager().openShop(player);
        player.sendMessage(plugin.getConfigManager().parse("&eDrag an item from your inventory to a slot to add it."));
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        if (!sessions.containsKey(player.getUniqueId())) return;

        EditorSession session = sessions.get(player.getUniqueId());
        if (session.step != 0) return;

        if (!(event.getInventory().getHolder() instanceof ShopHolder)) return;


        if (event.getClickedInventory() == event.getInventory()) {

            ItemStack cursor = event.getCursor();
            if (cursor != null && cursor.getType() != Material.AIR) {

                event.setCancelled(true);
                session.item = cursor.clone();
                session.slot = event.getSlot();
                session.step = 1;

                player.closeInventory();
                player.sendMessage(plugin.getConfigManager().parse("&eВведите цену за 1 шт (в монетах):"));
            }
        } else {


             event.setCancelled(false);
        }
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        if (!sessions.containsKey(player.getUniqueId())) return;

        EditorSession session = sessions.get(player.getUniqueId());
        if (session.step == 0) return;

        event.setCancelled(true);
        String msg = PlainTextComponentSerializer.plainText().serialize(event.message());

        if (msg.equalsIgnoreCase("cancel")) {
            sessions.remove(player.getUniqueId());
            player.sendMessage(plugin.getConfigManager().parse(plugin.getConfigManager().getMessage("admin_shop_cancelled")));
            return;
        }

        processChat(player, session, msg);
    }

    private void processChat(Player player, EditorSession session, String msg) {

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            try {
                switch (session.step) {
                    case 1:
                        double price = Double.parseDouble(msg);
                        session.price = price;
                        session.step = 2;
                        player.sendMessage(plugin.getConfigManager().parse("&eПодтвердите цену: &6" + price + " &eмонет (напишите 'да' или 'нет'):"));
                        break;
                    case 2:
                        if (msg.equalsIgnoreCase("да") || msg.equalsIgnoreCase("yes")) {
                            session.step = 3;
                            player.sendMessage(plugin.getConfigManager().parse("&eВведите максимальное количество для покупки:"));
                        } else {
                            sessions.remove(player.getUniqueId());
                            player.sendMessage(plugin.getConfigManager().parse(plugin.getConfigManager().getMessage("admin_shop_cancelled")));
                        }
                        break;
                    case 3:
                        int max = Integer.parseInt(msg);
                        session.maxPurchase = max;
                        session.step = 4;
                        player.sendMessage(plugin.getConfigManager().parse("&eВведите время обновления (в секундах):"));
                        break;
                    case 4:
                        int refresh = Integer.parseInt(msg);
                        session.refreshTime = refresh;
                        session.step = 5;
                        player.sendMessage(plugin.getConfigManager().parse("&eВведите количество требуемых полных уровней для разблокировки:"));
                        break;
                    case 5:
                        int req = Integer.parseInt(msg);
                        session.requiredLevels = req;
                        session.step = 6;
                        player.sendMessage(plugin.getConfigManager().parse("&eВыберите NPC (fermer/alximik/weapons):"));
                        break;
                    case 6:
                        String npc = msg.toLowerCase();
                        if (!npc.equals("fermer") && !npc.equals("alximik") && !npc.equals("weapons")) {
                            player.sendMessage(plugin.getConfigManager().parse("&cНеверный NPC! Используйте: fermer, alximik, weapons"));
                            return;
                        }
                        session.npc = npc;
                        saveItem(session);
                        sessions.remove(player.getUniqueId());
                        player.sendMessage(plugin.getConfigManager().parse(plugin.getConfigManager().getMessage("admin_shop_item_added")));
                        break;
                }
            } catch (NumberFormatException e) {
                player.sendMessage(plugin.getConfigManager().parse("&cВведите число!"));
            }
        });
    }

    private void saveItem(EditorSession session) {

        String id = session.item.getType().name().toLowerCase() + "_" + System.currentTimeMillis();
        ConfigurationSection config = plugin.getConfigManager().getShopConfig().createSection("items." + id);

        config.set("slot", session.slot);
        config.set("material", session.item.getType().name());
        config.set("amount", session.item.getAmount());
        config.set("display_name", "&f" + session.item.getType().name());
        config.set("price", session.price);
        config.set("max_purchase", session.maxPurchase);
        config.set("refresh_time", session.refreshTime);
        config.set("required_levels_completed", session.requiredLevels);
        config.set("npc", session.npc);


        config.set("lore", java.util.Arrays.asList(
            "&7Item added via editor",
            "",
            "&eЦена: &6" + session.price + " монет",
            "&aДоступно: &f%available%&7/&f%max%",
            "&7Обновление через: &e%timer%"
        ));


        config.set("locked_material", "BARRIER");
        config.set("locked_display_name", "&c✘ Заблокировано");
        config.set("locked_lore", java.util.Arrays.asList("&cПройдите " + session.requiredLevels + " полных уровней"));

        plugin.getConfigManager().saveShopConfig();
    }

    private static class EditorSession {
        Player player;
        int step = 0;
        ItemStack item;
        int slot;
        double price;
        int maxPurchase;
        int refreshTime;
        int requiredLevels;
        String npc;

        public EditorSession(Player player) {
            this.player = player;
        }
    }
}
