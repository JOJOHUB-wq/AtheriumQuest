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
        UserProfile profile = plugin.getUserManager().getUser(player.getUniqueId());
        int currentLevelNum = profile.getLevel(npcType);

        MenuManager.MenuConfig menuConfig = plugin.getMenuManager().getMenuConfig(npcType, currentLevelNum);

        if (menuConfig == null) {
             ItemStack is = new ItemStack(Material.EMERALD_BLOCK);
             ItemMeta meta = is.getItemMeta();
             meta.displayName(plugin.getConfigManager().parse("<green>Completed!"));
             is.setItemMeta(meta);

             Inventory inv = Bukkit.createInventory(this, 27, plugin.getConfigManager().parse("<green>Completed"));
             inv.setItem(13, is);
             player.openInventory(inv);
             return;
        }

        Inventory inv = Bukkit.createInventory(this, menuConfig.rows() * 9, plugin.getConfigManager().parse(menuConfig.title()));
        updateInventory(inv, menuConfig, currentLevelNum, profile);
        player.openInventory(inv);
    }

    private void updateInventory(Inventory inv, MenuManager.MenuConfig menuConfig, int currentLevelNum, UserProfile profile) {
        java.util.Map<String, Quest> logicQuests = plugin.getQuestManager().getLevelQuests(npcType, currentLevelNum);
        if (logicQuests == null) return;

        List<String> questIds = new ArrayList<>(logicQuests.keySet());

        int currentIndex = profile.getQuestIndex(npcType);

        for (int i = 0; i < questIds.size(); i++) {
            String qId = questIds.get(i);
            Quest quest = logicQuests.get(qId);
            QuestMenuItem visual = menuConfig.items().get(qId);

            if (visual != null) {
                ItemStack is = new ItemStack(visual.material());
                ItemMeta meta = is.getItemMeta();
                meta.displayName(plugin.getConfigManager().parse(visual.name()));

                List<Component> lore = new ArrayList<>();

                String status = "Locked";
                if (i < currentIndex) status = "Completed";
                else if (i == currentIndex) status = "Active";

                String progressStr = "";
                if (i == currentIndex) {
                    StringBuilder sb = new StringBuilder();
                    for (int t = 0; t < quest.getTasks().size(); t++) {
                         QuestTask task = quest.getTasks().get(t);
                         String key = npcType.name() + "_" + currentLevelNum + "_" + qId + "_" + t;
                         int current = profile.getProgress(key);
                         sb.append(task.getType()).append(": ").append(current).append("/").append(task.getAmount());
                         if (t < quest.getTasks().size() - 1) sb.append(", ");
                    }
                    progressStr = sb.toString();
                } else if (i < currentIndex) {
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
