package ua.atherium.atheriumquest.gui;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import ua.atherium.atheriumquest.AtheriumQuests;
import ua.atherium.atheriumquest.quest.NpcType;
import ua.atherium.atheriumquest.quest.Quest;

import java.io.File;
import java.util.List;

public class MenuListener implements Listener {

    private final AtheriumQuests plugin;

    public MenuListener(AtheriumQuests plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!(event.getInventory().getHolder() instanceof MenuHolder)) return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack current = event.getCurrentItem();
        if (current == null || current.getType() == Material.AIR) return;

        MenuHolder holder = (MenuHolder) event.getInventory().getHolder();
        int slot = event.getSlot();


        ConfigurationSection itemsSec = null;
        NpcType npc = null;

        if (holder.getType().equals("main_menu")) {
            itemsSec = plugin.getConfigManager().getMainMenuConfig().getConfigurationSection("items");
        } else if (holder.getType().startsWith("npc_")) {
            String[] parts = holder.getType().split("_");
            if (parts.length >= 2) {
                npc = NpcType.fromId(parts[1]);
                if (npc != null) {
                    File file = new File(plugin.getDataFolder(), npc.getId() + "/menues/level_" + holder.getLevel() + ".yml");
                    if (file.exists()) {
                        itemsSec = YamlConfiguration.loadConfiguration(file).getConfigurationSection("items");
                    }
                }
            }
        }

        if (itemsSec != null) {
            for (String key : itemsSec.getKeys(false)) {
                ConfigurationSection itemSec = itemsSec.getConfigurationSection(key);
                boolean match = false;
                List<Integer> slots = itemSec.getIntegerList("slots");
                if (slots.contains(slot)) match = true;
                else if (itemSec.getInt("slot", -1) == slot) match = true;

                if (match) {


                    if (itemSec.contains("quest")) {
                        handleQuestClick(player, npc, holder.getLevel(), itemSec.getInt("quest"));
                    } else {
                        List<String> commands = itemSec.getStringList("click_commands");
                        for (String cmd : commands) {
                            plugin.getQuestManager().executeReward(player, cmd);
                        }
                    }
                    return;
                }
            }
        }
    }

    private void handleQuestClick(Player player, NpcType npc, int level, int questNum) {
        String questId = "quest_" + questNum;
        Quest quest = plugin.getQuestManager().getQuest(npc, level, questId);
        if (quest == null) return;

        boolean completed = plugin.getStorageManager().getStorage().isQuestCompleted(player.getUniqueId(), npc.getId(), level, questId);
        boolean locked = false;



        ua.atherium.atheriumquest.quest.QuestLevel qLevel = plugin.getQuestManager().getQuestLevel(npc, level);
        if (qLevel != null) {
            boolean prevCompleted = true;
            for (Quest q : qLevel.getQuests().values()) {
                if (q.getId().equals(questId)) {
                    if (!prevCompleted) locked = true;
                    break;
                }
                if (!plugin.getStorageManager().getStorage().isQuestCompleted(player.getUniqueId(), npc.getId(), level, q.getId())) {
                    prevCompleted = false;
                }
            }
        }

        if (locked) {
            String msg = plugin.getConfigManager().getMessage("quest_locked");
            player.sendMessage(plugin.getConfigManager().parse(msg));
        } else if (!completed) {


        }
    }
}
