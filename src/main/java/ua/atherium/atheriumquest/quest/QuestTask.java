package ua.atherium.atheriumquest.quest;

public class QuestTask {
    private final String type;
    private final String target;
    private final int amount;

    public QuestTask(String type, String target, int amount) {
        this.type = type;
        this.target = target;
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public String getTarget() {
        return target;
    }

    public int getAmount() {
        return amount;
    }
}
