package ua.atherium.atheriumquest.quest;

import java.util.LinkedHashMap;
import java.util.Map;

public class QuestLevel {
    private final int level;
    private final Map<String, Quest> quests = new LinkedHashMap<>();

    public QuestLevel(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public Map<String, Quest> getQuests() {
        return quests;
    }

    public void addQuest(Quest quest) {
        quests.put(quest.getId(), quest);
    }
}
