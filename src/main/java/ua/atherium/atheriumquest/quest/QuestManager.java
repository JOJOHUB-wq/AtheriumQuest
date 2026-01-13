package ua.atherium.atheriumquest.quest;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import ua.atherium.atheriumquest.AtheriumQuest;

public class QuestManager {
    private final AtheriumQuest plugin;
    private final Map<NpcType, Map<Integer, QuestLevel>> npcLevels = new HashMap<>();
    private final Map<NpcType, Map<String, Quest>> questIdMap = new HashMap<>();

    public QuestManager(AtheriumQuest plugin) {
        this.plugin = plugin;
    }

    public void loadQuests() {
        npcLevels.clear();
        questIdMap.clear();
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

        Map<Integer, QuestLevel> levels = new HashMap<>();
        Map<String, Quest> idMap = new HashMap<>();

        for (String key : config.getKeys(false)) {
            if (key.startsWith("level_")) {
                try {
                    int levelNum = Integer.parseInt(key.replace("level_", ""));
                    List<Quest> levelQuests = new ArrayList<>();

                    List<Map<?, ?>> list = config.getMapList(key + ".quests_list");
                    for (Map<?, ?> qMap : list) {
                        String id = (String) qMap.get("id");
                        List<String> taskStrs = (List<String>) qMap.get("tasks");
                        List<String> rewardStrs = (List<String>) qMap.get("rewards");

                        List<QuestTask> tasks = new ArrayList<>();
                        for (String tStr : taskStrs) {
                            String[] parts = tStr.split(" ");
                            if (parts.length >= 2) {
                                String tType = parts[0];
                                int amount = Integer.parseInt(parts[1]);
                                String target = "";
                                if (parts.length > 2) target = parts[1];
                                if (parts.length > 2) amount = Integer.parseInt(parts[2]);
                                tasks.add(new QuestTask(tType, target, amount));
                            }
                        }

                        Quest quest = new Quest(id, tasks, rewardStrs);
                        levelQuests.add(quest);
                        idMap.put(id, quest);
                    }

                    levels.put(levelNum, new QuestLevel(levelNum, levelQuests));
                } catch (Exception e) {
                    plugin.getLogger().severe("Error loading level " + key + " for " + type);
                    e.printStackTrace();
                }
            }
        }
        npcLevels.put(type, levels);
        questIdMap.put(type, idMap);
    }

    public QuestLevel getLevel(NpcType type, int level) {
        return npcLevels.getOrDefault(type, new HashMap<>()).get(level);
    }

    public Quest getQuestById(NpcType type, String id) {
        return questIdMap.getOrDefault(type, new HashMap<>()).get(id);
    }
}
