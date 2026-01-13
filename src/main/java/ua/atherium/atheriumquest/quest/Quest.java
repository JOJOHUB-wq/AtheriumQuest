package ua.atherium.atheriumquest.quest;

import java.util.List;

public class Quest {
    private final String id;
    private final List<QuestTask> tasks;
    private final List<String> rewards;

    public Quest(String id, List<QuestTask> tasks, List<String> rewards) {
        this.id = id;
        this.tasks = tasks;
        this.rewards = rewards;
    }

    public String getId() {
        return id;
    }

    public List<QuestTask> getTasks() {
        return tasks;
    }

    public List<String> getRewards() {
        return rewards;
    }
}
