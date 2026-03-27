package ua.atherium.atheriumquest.quest;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import ua.atherium.atheriumquest.AtheriumQuests;

import java.io.File;
import java.util.*;
import java.util.logging.Level;

public class QuestManager {

    private final AtheriumQuests plugin;
    private final Map<NpcType, Map<Integer, QuestLevel>> npcQuests = new HashMap<>();

    public QuestManager(AtheriumQuests plugin) {
        this.plugin = plugin;
        loadQuests();
    }

    public void loadQuests() {
        npcQuests.clear();
        for (NpcType npc : NpcType.values()) {
            Map<Integer, QuestLevel> levels = new HashMap<>();
            File npcFolder = new File(plugin.getDataFolder(), npc.getId() + "/quests");
            if (!npcFolder.exists()) {
                npcFolder.mkdirs();

                for (int i = 1; i <= 3; i++) {
                    plugin.saveResource(npc.getId() + "/quests/level_" + i + ".yml", false);
                }
            }

            for (int i = 1; i <= 3; i++) {
                File file = new File(npcFolder, "level_" + i + ".yml");
                if (file.exists()) {
                    YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                    QuestLevel questLevel = new QuestLevel(i);
                    ConfigurationSection questsSec = config.getConfigurationSection("quests");

                    if (questsSec != null) {
                        for (String key : questsSec.getKeys(false)) {
                            ConfigurationSection qs = questsSec.getConfigurationSection(key);
                            if (qs == null) continue;

                            String type = qs.getString("type");
                            String target = qs.getString("material", "STONE");
                            if (qs.contains("mob_type")) target = qs.getString("mob_type");
                            int amount = qs.getInt("amount");
                            List<String> rewards = qs.getStringList("reward_commands");


                            String dName = qs.getString("display_name", "Quest");
                            List<String> dLore = qs.getStringList("lore");
                            Material mat = Material.matchMaterial(qs.getString("material", "STONE"));
                            if (mat == null) mat = Material.STONE;
                            boolean ench = qs.getBoolean("enchanted", false);

                            String cName = qs.getString("completed_display_name", dName);
                            List<String> cLore = qs.getStringList("completed_lore");
                            Material cMat = Material.matchMaterial(qs.getString("completed_material", mat.name()));
                            if (cMat == null) cMat = mat;
                            boolean cEnch = qs.getBoolean("completed_enchanted", false);

                            String lName = qs.getString("locked_display_name", "Locked");
                            List<String> lLore = qs.getStringList("locked_lore");
                            Material lMat = Material.matchMaterial(qs.getString("locked_material", "BARRIER"));
                            if (lMat == null) lMat = Material.BARRIER;
                            boolean lEnch = qs.getBoolean("locked_enchanted", false);

                            Quest quest = new Quest(key, type, target, amount, rewards,
                                    dName, dLore, mat, ench,
                                    cName, cLore, cMat, cEnch,
                                    lName, lLore, lMat, lEnch);

                            questLevel.addQuest(quest);
                        }
                    }
                    levels.put(i, questLevel);
                }
            }
            npcQuests.put(npc, levels);
        }
        plugin.getLogger().info("Quests loaded.");
    }

    public Quest getActiveQuest(Player player, NpcType npc) {
        int level = plugin.getStorageManager().getStorage().getLevel(player.getUniqueId(), npc.getId());
        QuestLevel qLevel = npcQuests.getOrDefault(npc, new HashMap<>()).get(level);
        if (qLevel == null) return null;

        for (Quest quest : qLevel.getQuests().values()) {
            if (!plugin.getStorageManager().getStorage().isQuestCompleted(player.getUniqueId(), npc.getId(), level, quest.getId())) {
                return quest;
            }
        }
        return null;
    }

    public Quest getQuest(NpcType npc, int level, String questId) {
        QuestLevel qLevel = npcQuests.getOrDefault(npc, new HashMap<>()).get(level);
        if (qLevel != null) {
            return qLevel.getQuests().get(questId);
        }
        return null;
    }

    public QuestLevel getQuestLevel(NpcType npc, int level) {
         return npcQuests.getOrDefault(npc, new HashMap<>()).get(level);
    }

    public void forceCompleteActiveQuest(Player player, NpcType npc) {
        Quest active = getActiveQuest(player, npc);
        if (active == null) return;

        int currentLevel = plugin.getStorageManager().getStorage().getLevel(player.getUniqueId(), npc.getId());
        completeQuest(player, npc, currentLevel, active);
    }

    public void updateProgress(Player player, NpcType npc, String type, String target, int amount) {
        Quest active = getActiveQuest(player, npc);
        if (active == null) {

            checkLevelCompletion(player, npc);
            return;
        }

        if (active.getType().equalsIgnoreCase(type)) {


            boolean match = true;
            if (active.getTarget() != null && !active.getTarget().equalsIgnoreCase("ANY")) {
                if (target == null || !target.equalsIgnoreCase(active.getTarget())) {
                    match = false;
                }
            }

            if (match) {
                int currentLevel = plugin.getStorageManager().getStorage().getLevel(player.getUniqueId(), npc.getId());
                int currentProgress = plugin.getStorageManager().getStorage().getQuestProgress(player.getUniqueId(), npc.getId(), currentLevel, active.getId());

                if (currentProgress < active.getAmount()) {
                    int newProgress = currentProgress + amount;
                    if (newProgress >= active.getAmount()) {
                        completeQuest(player, npc, currentLevel, active);
                    } else {
                        plugin.getStorageManager().getStorage().setQuestProgress(player.getUniqueId(), npc.getId(), currentLevel, active.getId(), newProgress);


                    }
                }
            }
        }
    }

    private void completeQuest(Player player, NpcType npc, int level, Quest quest) {
        plugin.getStorageManager().getStorage().setQuestCompleted(player.getUniqueId(), npc.getId(), level, quest.getId(), true);


        executeRewardList(player, quest.getRewardCommands(), 0);


        plugin.getLogger().info("Player " + player.getName() + " completed quest " + quest.getId() + " (Level " + level + " " + npc.getName() + ")");


        checkLevelCompletion(player, npc);
    }

    private void executeRewardList(Player player, List<String> commands, int index) {
        if (index >= commands.size()) return;

        String cmd = commands.get(index);

        if (cmd.startsWith("[delay] ")) {
            try {
                int delay = Integer.parseInt(cmd.substring(8).trim());
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    executeRewardList(player, commands, index + 1);
                }, delay);
            } catch (NumberFormatException e) {

                executeRewardList(player, commands, index + 1);
            }
        } else {
            executeReward(player, cmd);
            executeRewardList(player, commands, index + 1);
        }
    }

    private void checkLevelCompletion(Player player, NpcType npc) {
        int currentLevel = plugin.getStorageManager().getStorage().getLevel(player.getUniqueId(), npc.getId());
        QuestLevel qLevel = npcQuests.getOrDefault(npc, new HashMap<>()).get(currentLevel);

        if (qLevel == null) return;

        boolean allCompleted = true;
        for (Quest q : qLevel.getQuests().values()) {
            if (!plugin.getStorageManager().getStorage().isQuestCompleted(player.getUniqueId(), npc.getId(), currentLevel, q.getId())) {
                allCompleted = false;
                break;
            }
        }

        if (allCompleted) {





             if (npcQuests.get(npc).containsKey(currentLevel + 1)) {
                 plugin.getStorageManager().getStorage().setLevel(player.getUniqueId(), npc.getId(), currentLevel + 1);
                 plugin.getStorageManager().getStorage().setCompletedLevels(player.getUniqueId(), npc.getId(), currentLevel);












                 String msg = plugin.getConfigManager().getMessage("level_completed").replace("%level%", String.valueOf(currentLevel));
                 player.sendMessage(plugin.getConfigManager().parse(msg));
             } else {

                 plugin.getStorageManager().getStorage().setCompletedLevels(player.getUniqueId(), npc.getId(), currentLevel);
                 String msg = plugin.getConfigManager().getMessage("all_quests_completed");
                 player.sendMessage(plugin.getConfigManager().parse(msg));
             }
        }
    }

    public void executeReward(Player player, String command) {



        String cmd = command.replace("%player%", player.getName());

        if (cmd.startsWith("[console] ")) {
            String c = cmd.substring(10);
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), c);
        } else if (cmd.startsWith("[player] ")) {
            String c = cmd.substring(9);
            player.performCommand(c);
        } else if (cmd.startsWith("[message] ")) {
             String msg = cmd.substring(10);
             player.sendMessage(plugin.getConfigManager().parse(msg));
        } else if (cmd.startsWith("[broadcast] ")) {
             String msg = cmd.substring(12);
             plugin.getServer().broadcast(plugin.getConfigManager().parse(msg));
        } else if (cmd.startsWith("[title] ")) {
             String[] parts = cmd.substring(8).split(";");
             if (parts.length >= 2) {
                 player.showTitle(net.kyori.adventure.title.Title.title(
                     plugin.getConfigManager().parse(parts[0]),
                     plugin.getConfigManager().parse(parts[1])
                 ));
             }
        } else if (cmd.startsWith("[actionbar] ")) {
             String msg = cmd.substring(12);
             player.sendActionBar(plugin.getConfigManager().parse(msg));
        } else if (cmd.startsWith("[sound] ")) {
             String[] parts = cmd.substring(8).split("-");
             try {
                 org.bukkit.Sound sound = org.bukkit.Sound.valueOf(parts[0]);
                 float vol = parts.length > 1 ? Float.parseFloat(parts[1]) : 1.0f;
                 float pitch = parts.length > 2 ? Float.parseFloat(parts[2]) : 1.0f;
                 player.playSound(player.getLocation(), sound, vol, pitch);
             } catch (Exception e) {
                 plugin.getLogger().warning("Invalid sound: " + parts[0]);
             }
        } else if (cmd.startsWith("[close]")) {
            player.closeInventory();
        }



        else if (cmd.startsWith("[givemoney] ")) {
             if (plugin.getEconomy() != null) {
                 try {
                     double amount = Double.parseDouble(cmd.substring(12));
                     plugin.getEconomy().depositPlayer(player, amount);
                 } catch (NumberFormatException e) {}
             }
        }
    }
}
