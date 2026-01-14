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
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
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
        if (event.getBlock().getType().name().contains("LOG")) {
            handleProgress(event.getPlayer(), "break_log", event.getBlock().getType().name(), 1);
        }
        if (event.getBlock().getBlockData() instanceof org.bukkit.block.data.Ageable ageable) {
             if (ageable.getAge() == ageable.getMaximumAge()) {
                 if (event.getBlock().getType() == Material.WHEAT) {
                     handleProgress(event.getPlayer(), "harvest_wheat", "", 1);
                 } else if (event.getBlock().getType() == Material.POTATOES) {
                     handleProgress(event.getPlayer(), "harvest_potato", "", 1);
                 }
             }
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
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;

        if (event.getInventory().getType() == InventoryType.BREWING) {
            if (event.getSlotType() == InventoryType.SlotType.RESULT && event.getCurrentItem() != null) {
                if (event.getWhoClicked() instanceof Player player) {
                    handleProgress(player, "brew_potion", "", 1);
                }
            }
        }
        if (event.getInventory().getType() == InventoryType.FURNACE) {
             if (event.getSlotType() == InventoryType.SlotType.RESULT && event.getCurrentItem() != null) {
                 if (event.getCurrentItem().getType() == Material.IRON_INGOT) {
                     if (event.getWhoClicked() instanceof Player player) {
                         handleProgress(player, "smelt_iron", "", event.getCurrentItem().getAmount());
                     }
                 }
             }
        }
        if (event.getInventory().getType() == InventoryType.SMITHING) {
             if (event.getSlotType() == InventoryType.SlotType.RESULT && event.getCurrentItem() != null) {
                 if (event.getWhoClicked() instanceof Player player) {
                     handleProgress(player, "upgrade_armor", "", 1);
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
        int questIndex = profile.getQuestIndex(npcType);
        QuestLevel level = plugin.getQuestManager().getLevel(npcType, levelNum);

        if (level == null) return;
        if (questIndex >= level.getQuests().size()) return;

        if (npcType == NpcType.FARMER && levelNum == 1 && questIndex == 0) {
             boolean justStarted = true;
             Quest firstQuest = level.getQuests().get(0);
             for (int t = 0; t < firstQuest.getTasks().size(); t++) {
                 if (profile.getProgress(npcType.name() + "_1_0_" + t) > 0) {
                     justStarted = false;
                     break;
                 }
             }
             if (justStarted) {
                 plugin.getDatabaseManager().getStorage().setQuestStart(player.getUniqueId(), System.currentTimeMillis());
             }
        }

        Quest activeQuest = level.getQuests().get(questIndex);

        for (int t = 0; t < activeQuest.getTasks().size(); t++) {
            QuestTask task = activeQuest.getTasks().get(t);
            boolean typeMatch = task.getType().equalsIgnoreCase(type);
            boolean targetMatch = task.getTarget().isEmpty() || task.getTarget().equalsIgnoreCase(target);

            if (typeMatch && targetMatch) {
                String key = npcType.name() + "_" + levelNum + "_" + questIndex + "_" + t;
                int current = profile.getProgress(key);

                if (current < task.getAmount()) {
                    int newAmount = Math.min(current + amount, task.getAmount());
                    profile.setProgress(key, newAmount);
                    plugin.getUserManager().saveUser(player.getUniqueId());
                    checkQuestCompletion(player, profile, npcType, level, activeQuest, questIndex);
                }
            }
        }
    }

    private void checkQuestCompletion(Player player, UserProfile profile, NpcType npcType, QuestLevel level, Quest quest, int questIndex) {
        boolean allComplete = true;
        for (int t = 0; t < quest.getTasks().size(); t++) {
             String key = npcType.name() + "_" + level.getLevelNumber() + "_" + questIndex + "_" + t;
             if (profile.getProgress(key) < quest.getTasks().get(t).getAmount()) {
                 allComplete = false;
                 break;
             }
        }

        if (allComplete) {
            for (String cmd : quest.getRewards()) {
                String finalCmd = cmd.replace("%player%", player.getName()).replace("%player_name%", player.getName());
                if (finalCmd.startsWith("msg ") || finalCmd.startsWith("message ")) {
                     String msg = finalCmd.substring(finalCmd.indexOf(" ") + 1);
                     player.sendMessage(plugin.getConfigManager().parse(msg));
                } else {
                     Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCmd);
                }
            }

            int nextIndex = questIndex + 1;
            profile.setQuestIndex(npcType, nextIndex);

            if (nextIndex >= level.getQuests().size()) {
                profile.setLevel(npcType, level.getLevelNumber() + 1);
                profile.setQuestIndex(npcType, 0);

                String msg = plugin.getConfigManager().getConfig().getString("messages.level_up", "Level {level}").replace("{level}", String.valueOf(level.getLevelNumber() + 1));
                player.sendMessage(plugin.getConfigManager().parse(msg));

                 if (npcType == NpcType.FARMER && level.getLevelNumber() == 3) {
                     plugin.getDatabaseManager().getStorage().setQuestEnd(player.getUniqueId(), System.currentTimeMillis());
                }
            }

            plugin.getUserManager().saveUser(player.getUniqueId());
        }
    }
}
