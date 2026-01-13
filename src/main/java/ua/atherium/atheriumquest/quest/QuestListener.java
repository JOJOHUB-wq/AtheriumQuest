package ua.atherium.atheriumquest.quest;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import ua.atherium.atheriumquest.AtheriumQuest;
import ua.atherium.atheriumquest.user.UserProfile;

public class QuestListener implements Listener {

    private final AtheriumQuest plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public QuestListener(AtheriumQuest plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        handleProgress(event.getPlayer(), "break_log", event.getBlock().getType().name(), 1);
        if (event.getBlock().getType() == Material.WHEAT && event.getBlock().getData() == 7) {
             handleProgress(event.getPlayer(), "harvest_wheat", "", 1);
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() != null) {
            String type = event.getEntityType().name();
            handleProgress(event.getEntity().getKiller(), "kill_" + type.toLowerCase(), "", 1);
            if (event.getEntity() instanceof Player) {
                handleProgress(event.getEntity().getKiller(), "kill_player", "", 1);
            }
        }
    }

    @EventHandler
    public void onBlockPlace(org.bukkit.event.block.BlockPlaceEvent event) {
         if (event.getBlock().getType() == Material.POTATOES) {
             handleProgress(event.getPlayer(), "plant_potato", "", 1);
         }
    }

    @EventHandler
    public void onShear(org.bukkit.event.player.PlayerShearEntityEvent event) {
        if (event.getEntity().getType() == EntityType.SHEEP) {
            handleProgress(event.getPlayer(), "shear_sheep", "", 1);
        }
    }

    @EventHandler
    public void onTame(org.bukkit.event.entity.EntityTameEvent event) {
        if (event.getOwner() instanceof Player player) {
            handleProgress(player, "tame_animal", "", 1);
        }
    }

    @EventHandler
    public void onConsume(org.bukkit.event.player.PlayerItemConsumeEvent event) {
        if (event.getItem().getType() == Material.POTION) {
            handleProgress(event.getPlayer(), "eat_food", "", 1);
        } else if (event.getItem().getType().isEdible()) {
            handleProgress(event.getPlayer(), "eat_food", "", 1);
        }
    }

    @EventHandler
    public void onBrew(org.bukkit.event.inventory.BrewEvent event) {
    }

    @EventHandler
    public void onInventoryClick(org.bukkit.event.inventory.InventoryClickEvent event) {
        if (event.getInventory().getType() == org.bukkit.event.inventory.InventoryType.BREWING) {
            if (event.getSlotType() == org.bukkit.event.inventory.InventoryType.SlotType.RESULT && event.getCurrentItem() != null) {
                if (event.getWhoClicked() instanceof Player player) {
                    handleProgress(player, "brew_potion", "", 1);
                }
            }
        }
        if (event.getInventory().getType() == org.bukkit.event.inventory.InventoryType.FURNACE) {
             if (event.getSlotType() == org.bukkit.event.inventory.InventoryType.SlotType.RESULT && event.getCurrentItem() != null) {
                 if (event.getCurrentItem().getType() == Material.IRON_INGOT) {
                     if (event.getWhoClicked() instanceof Player player) {
                         handleProgress(player, "smelt_iron", "", event.getCurrentItem().getAmount());
                     }
                 }
             }
        }
    }

    @EventHandler
    public void onCraft(org.bukkit.event.inventory.CraftItemEvent event) {
        if (event.getRecipe().getResult().getType().name().contains("SWORD")) {
             if (event.getWhoClicked() instanceof Player player) {
                 handleProgress(player, "craft_sword", "", 1);
             }
        }
    }

    @EventHandler
    public void onPickup(org.bukkit.event.entity.EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player) {
            handleProgress(player, "collect_reagents", event.getItem().getItemStack().getType().name(), event.getItem().getItemStack().getAmount());
        }
    }

    private void handleProgress(Player player, String type, String target, int amount) {
        UserProfile profile = plugin.getUserManager().getUser(player.getUniqueId());

        checkNpcProgress(player, profile, NpcType.FARMER, type, target, amount);
        checkNpcProgress(player, profile, NpcType.ALCHEMIST, type, target, amount);
        checkNpcProgress(player, profile, NpcType.WEAPONS, type, target, amount);
    }

    private void checkNpcProgress(Player player, UserProfile profile, NpcType npcType, String type, String target, int amount) {
        int levelNum = profile.getLevel(npcType);
        QuestLevel level = plugin.getQuestManager().getQuestLevel(npcType, levelNum);

        if (level == null) return;

        if (npcType == NpcType.FARMER && levelNum == 1) {
             boolean justStarted = true;
             for (int j = 0; j < level.getTasks().size(); j++) {
                 if (profile.getProgress(npcType.name() + "_1_" + j) > 0) {
                     justStarted = false;
                     break;
                 }
             }
             if (justStarted) {
                 plugin.getDatabaseManager().getStorage().setQuestStart(player.getUniqueId(), System.currentTimeMillis());
             }
        }

        for (int i = 0; i < level.getTasks().size(); i++) {
            ua.atherium.atheriumquest.quest.QuestTask task = level.getTasks().get(i);

            if (task.getType().equalsIgnoreCase(type) && task.getTarget().equalsIgnoreCase(target)) {
                String key = npcType.name() + "_" + levelNum + "_" + i;
                int current = profile.getProgress(key);

                if (current < task.getAmount()) {
                    int newAmount = Math.min(current + amount, task.getAmount());
                    profile.setProgress(key, newAmount);

                    if (newAmount >= task.getAmount()) {
                    }

                    plugin.getUserManager().saveUser(player.getUniqueId());
                    checkLevelCompletion(player, profile, npcType, level);
                }
            }
        }
    }

    private void checkLevelCompletion(Player player, UserProfile profile, NpcType npcType, QuestLevel level) {
        boolean allComplete = true;
        for (int i = 0; i < level.getTasks().size(); i++) {
             String key = npcType.name() + "_" + level.getLevelNumber() + "_" + i;
             if (profile.getProgress(key) < level.getTasks().get(i).getAmount()) {
                 allComplete = false;
                 break;
             }
        }

        if (allComplete) {
            player.closeInventory();
            player.sendMessage(miniMessage.deserialize("<green>Level completed! Talk to the NPC to unlock the next level."));

            if (npcType == NpcType.FARMER && level.getLevelNumber() == 3) {
                 plugin.getDatabaseManager().getStorage().setQuestEnd(player.getUniqueId(), System.currentTimeMillis());
            }

            for (String cmd : level.getRewards()) {
                String finalCmd = cmd.replace("%player%", player.getName());
                if (finalCmd.startsWith("msg ") || finalCmd.startsWith("message ")) {
                     String msg = finalCmd.substring(finalCmd.indexOf(" ") + 1);
                     player.sendMessage(plugin.getConfigManager().parse(msg));
                } else {
                     Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCmd);
                }
            }
        }
    }
}
