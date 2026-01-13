package ua.atherium.atheriumquest.quest;

import java.util.List;

public class QuestLevel {
    private final int levelNumber;
    private final List<Quest> quests;

    public QuestLevel(int levelNumber, List<Quest> quests) {
        this.levelNumber = levelNumber;
        this.quests = quests;
    }

    public int getLevelNumber() {
        return levelNumber;
    }

    public List<Quest> getQuests() {
        return quests;
    }
}
