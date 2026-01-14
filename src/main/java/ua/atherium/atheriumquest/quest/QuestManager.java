package ua.atherium.atheriumquest.quest;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import ua.atherium.atheriumquest.AtheriumQuest;

public class QuestManager {
    private final AtheriumQuest plugin;
    private final Map<NpcType, Map<Integer, Map<String, Quest>>> npcQuests = new HashMap<>();

    public QuestManager(AtheriumQuest plugin) {
        this.plugin = plugin;
    }

    public void loadQuests() {
        npcQuests.clear();
        loadNpcQuest(NpcType.FARMER, "farmer.yml");
        loadNpcQuest(NpcType.ALCHEMIST, "alchemist.yml");
        loadNpcQuest(NpcType.WEAPONS, "weapons.yml");
    }

    private void loadNpcQuest(NpcType type, String fileName) {
        File file = new File(plugin.getDataFolder(), "quests/" + fileName);
        if (!file.exists()) {
            plugin.saveResource("quests/" + fileName, false);
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        Map<Integer, Map<String, Quest>> levels = new HashMap<>();

        for (String key : config.getKeys(false)) {
            if (key.startsWith("level_")) {
                try {
                    int levelNum = Integer.parseInt(key.replace("level_", ""));

                    ConfigurationSection questsListSec = config.getConfigurationSection(key + ".quests_list");
                    Map<String, Quest> levelQuests = new LinkedHashMap<>();

                    if (questsListSec != null) {
                        for (String qId : questsListSec.getKeys(false)) {
                            ConfigurationSection qSec = questsListSec.getConfigurationSection(qId);
                            if (qSec != null) {
                                List<String> taskStrs = qSec.getStringList("tasks");
                                List<String> rewardStrs = qSec.getStringList("rewards");

                                List<QuestTask> tasks = new ArrayList<>();
                                for (String tStr : taskStrs) {
                                    String[] parts = tStr.split(" ");
                                    if (parts.length >= 2) {
                                        String tType = parts[0];
                                        String target = "";
                                        int amount = 0;

                                        if (parts.length == 2) {
                                            try {
                                                amount = Integer.parseInt(parts[1]);
                                            } catch (NumberFormatException e) {
                                                target = parts[1];
                                            }
                                        } else if (parts.length > 2) {
                                            target = parts[1];
                                            amount = Integer.parseInt(parts[2]);
                                        }

                                        tasks.add(new QuestTask(tType, target, amount));
                                    }
                                }

                                levelQuests.put(qId, new Quest(qId, tasks, rewardStrs));
                            }
                        }
                    }

                    levels.put(levelNum, levelQuests);
                } catch (Exception e) {
                    plugin.getLogger().severe("Error loading " + key + " for " + type);
                    e.printStackTrace();
                }
            }
        }
        npcQuests.put(type, levels);
    }

    public Map<String, Quest> getLevelQuests(NpcType type, int level) {
        return npcQuests.getOrDefault(type, new HashMap<>()).get(level);
    }

    public Quest getQuest(NpcType type, int level, String questId) {
        Map<String, Quest> levelMap = getLevelQuests(type, level);
        if (levelMap != null) {
            return levelMap.get(questId);
        }
        return null;
    }
}
