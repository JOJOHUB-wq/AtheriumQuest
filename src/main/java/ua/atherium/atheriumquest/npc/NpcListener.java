package ua.atherium.atheriumquest.npc;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import ua.atherium.atheriumquest.AtheriumQuest;
import ua.atherium.atheriumquest.quest.NpcType;

public class NpcListener implements Listener {

    private final AtheriumQuest plugin;
    private final NamespacedKey npcKey;

    public NpcListener(AtheriumQuest plugin) {
        this.plugin = plugin;
        this.npcKey = new NamespacedKey(plugin, "npc_type");
    }

    @EventHandler
    public void onNpcClick(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();
        PersistentDataContainer container = entity.getPersistentDataContainer();

        if (container.has(npcKey, PersistentDataType.STRING)) {
            event.setCancelled(true);
            String typeStr = container.get(npcKey, PersistentDataType.STRING);
            try {
                NpcType type = NpcType.valueOf(typeStr);
                Player player = event.getPlayer();

                ua.atherium.atheriumquest.user.UserProfile profile = plugin.getUserManager().getUser(player.getUniqueId());
                int currentLevel = profile.getLevel(type);
                ua.atherium.atheriumquest.quest.QuestLevel qLevel = plugin.getQuestManager().getQuestLevel(type, currentLevel);

                if (qLevel != null) {
                    boolean allComplete = true;
                    for (int i = 0; i < qLevel.getTasks().size(); i++) {
                         String key = type.name() + "_" + currentLevel + "_" + i;
                         if (profile.getProgress(key) < qLevel.getTasks().get(i).getAmount()) {
                             allComplete = false;
                             break;
                         }
                    }

                    if (allComplete) {
                        profile.setLevel(type, currentLevel + 1);
                        plugin.getUserManager().saveUser(player.getUniqueId());

                        String msg = plugin.getConfigManager().getConfig().getString("messages.level_up", "Level {level}").replace("{level}", String.valueOf(currentLevel + 1));
                        player.sendMessage(plugin.getConfigManager().parse(msg));
                    }
                }

                new ua.atherium.atheriumquest.shop.ShopMenu(plugin, player).open();

            } catch (IllegalArgumentException ignored) {}
        }
    }
}
