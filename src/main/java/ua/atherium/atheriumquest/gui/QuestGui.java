package ua.atherium.atheriumquest.gui;

import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ua.atherium.atheriumquest.AtheriumQuest;
import ua.atherium.atheriumquest.quest.NpcType;
import ua.atherium.atheriumquest.quest.Quest;
import ua.atherium.atheriumquest.quest.QuestLevel;
import ua.atherium.atheriumquest.quest.QuestTask;
import ua.atherium.atheriumquest.user.UserProfile;

public class QuestGui extends Menu {

    private final AtheriumQuest plugin;
    private final Player player;
    private final NpcType npcType;

    public QuestGui(AtheriumQuest plugin, Player player, NpcType npcType) {
        this.plugin = plugin;
        this.player = player;
        this.npcType = npcType;
    }

    public void open() {
        MenuManager.MenuConfig menuConfig = plugin.getMenuManager().getMenuConfig(npcType);
        if (menuConfig == null) {
            player.sendMessage("Menu config not found for " + npcType);
            return;
        }

        Inventory inv = Bukkit.createInventory(this, menuConfig.rows() * 9, plugin.getConfigManager().parse(menuConfig.title()));
        updateInventory(inv);
        player.openInventory(inv);
    }

    private void updateInventory(Inventory inv) {
        UserProfile profile = plugin.getUserManager().getUser(player.getUniqueId());
        int currentLevelNum = profile.getLevel(npcType);
        int questIndex = profile.getQuestIndex(npcType);

        QuestLevel level = plugin.getQuestManager().getLevel(npcType, currentLevelNum);

        if (level != null) {
            List<Quest> quests = level.getQuests();
            for (int i = 0; i < quests.size(); i++) {
                Quest quest = quests.get(i);
                QuestMenuItem visual = plugin.getMenuManager().getMenuItem(npcType, quest.getId());

                if (visual != null) {
                    ItemStack is = new ItemStack(visual.material());
                    ItemMeta meta = is.getItemMeta();
                    meta.displayName(plugin.getConfigManager().parse(visual.name()));

                    List<Component> lore = new ArrayList<>();

                    String status = "Locked";
                    if (i < questIndex) status = "Completed";
                    else if (i == questIndex) status = "Active";

                    String progressStr = "";
                    if (i == questIndex) {
                        StringBuilder sb = new StringBuilder();
                        for (int t = 0; t < quest.getTasks().size(); t++) {
                             QuestTask task = quest.getTasks().get(t);
                             String key = npcType.name() + "_" + currentLevelNum + "_" + i + "_" + t;
                             int current = profile.getProgress(key);
                             sb.append(task.getType()).append(": ").append(current).append("/").append(task.getAmount());
                             if (t < quest.getTasks().size() - 1) sb.append(", ");
                        }
                        progressStr = sb.toString();
                    } else if (i < questIndex) {
                        progressStr = "100%";
                    } else {
                        progressStr = "0%";
                    }

                    for (String line : visual.lore()) {
                         String formatted = line.replace("%status%", status).replace("%progress%", progressStr);
                         lore.add(plugin.getConfigManager().parse(formatted));
                    }

                    meta.lore(lore);
                    is.setItemMeta(meta);

                    inv.setItem(visual.slot(), is);
                }
            }
        } else {
             ItemStack is = new ItemStack(Material.EMERALD_BLOCK);
             ItemMeta meta = is.getItemMeta();
             meta.displayName(plugin.getConfigManager().parse("<green>Completed!"));
             is.setItemMeta(meta);
             inv.setItem(13, is);
        }
    }

    @Override
    public void handleMenu(InventoryClickEvent event) {
        event.setCancelled(true);
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}
