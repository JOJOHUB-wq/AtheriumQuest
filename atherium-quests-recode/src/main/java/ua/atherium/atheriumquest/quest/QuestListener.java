package ua.atherium.atheriumquest.quest;

import org.bukkit.Material;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import ua.atherium.atheriumquest.AtheriumQuests;
import ua.atherium.atheriumquest.quest.NpcType;

public class QuestListener implements Listener {

    private final AtheriumQuests plugin;

    public QuestListener(AtheriumQuests plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        String blockType = event.getBlock().getType().name();














        for (NpcType npc : NpcType.values()) {
            plugin.getQuestManager().updateProgress(player, npc, "plant_blocks", event.getItemInHand().getType().name(), 1);
            plugin.getQuestManager().updateProgress(player, npc, "place_blocks", event.getBlock().getType().name(), 1);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        String blockType = event.getBlock().getType().name();


        for (NpcType npc : NpcType.values()) {
            plugin.getQuestManager().updateProgress(player, npc, "break_blocks", blockType, 1);
        }



        if (event.getBlock().getBlockData() instanceof Ageable) {
            Ageable ageable = (Ageable) event.getBlock().getBlockData();
            if (ageable.getAge() == ageable.getMaximumAge()) {












                for (NpcType npc : NpcType.values()) {
                    plugin.getQuestManager().updateProgress(player, npc, "harvest_blocks", blockType, 1);
                }
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() != null) {
            Player player = event.getEntity().getKiller();
            String mobType = event.getEntityType().name();

            for (NpcType npc : NpcType.values()) {
                plugin.getQuestManager().updateProgress(player, npc, "kill_mobs", mobType, 1);
            }
        }
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            ItemStack item = event.getItem().getItemStack();
            String itemType = item.getType().name();
            int amount = item.getAmount();

            for (NpcType npc : NpcType.values()) {
                plugin.getQuestManager().updateProgress(player, npc, "collect_items", itemType, amount);
            }
        }
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            ItemStack item = event.getCurrentItem();
            if (item != null) {







                String itemType = item.getType().name();
                int amount = item.getAmount();
                if (event.isShiftClick()) {



                }

                for (NpcType npc : NpcType.values()) {
                    plugin.getQuestManager().updateProgress(player, npc, "craft_items", itemType, amount);
                }
            }
        }
    }
}
