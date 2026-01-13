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

                new ua.atherium.atheriumquest.gui.QuestGui(plugin, player, type).open();

            } catch (IllegalArgumentException ignored) {}
        }
    }
}
