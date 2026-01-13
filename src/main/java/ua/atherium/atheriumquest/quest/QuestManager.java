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
    private final Map<NpcType, Map<Integer, QuestLevel>> quests = new HashMap<>();

    public QuestManager(AtheriumQuest plugin) {
        this.plugin = plugin;
    }

    public void loadQuests() {
        quests.clear();
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

        for (int i = 1; i <= 3; i++) {
            String key = "quests_" + i;
            if (config.contains(key)) {
                List<QuestTask> taskList = new ArrayList<>();
                List<String> tasks = config.getStringList(key + ".tasks");

                for (String taskStr : tasks) {
                    String[] parts = taskStr.split(" ");
                    if (parts.length >= 2) {
                         String tType = parts[0];
                         int amount = Integer.parseInt(parts[1]);

                         String target = "";
                         if (parts.length > 2) target = parts[1];
                         if (parts.length > 2) amount = Integer.parseInt(parts[2]);

                         taskList.add(new QuestTask(tType, target, amount));
                    }
                }

                List<String> rewards = config.getStringList(key + ".rewards");
                levels.put(i, new QuestLevel(i, taskList, rewards));
            }
        }
        quests.put(type, levels);
    }

    public QuestLevel getQuestLevel(NpcType type, int level) {
        return quests.getOrDefault(type, new HashMap<>()).get(level);
    }
}
