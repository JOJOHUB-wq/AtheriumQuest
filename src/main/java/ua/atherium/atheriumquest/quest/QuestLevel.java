package ua.atherium.atheriumquest.quest;

import java.util.List;

public class QuestLevel {
    private final int levelNumber;
    private final List<QuestTask> tasks;
    private final List<String> rewards;

    public QuestLevel(int levelNumber, List<QuestTask> tasks, List<String> rewards) {
        this.levelNumber = levelNumber;
        this.tasks = tasks;
        this.rewards = rewards;
    }

    public int getLevelNumber() {
        return levelNumber;
    }

    public List<QuestTask> getTasks() {
        return tasks;
    }

    public List<String> getRewards() {
        return rewards;
    }
}
