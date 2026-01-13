package ua.atherium.atheriumquest.database;

import java.util.UUID;

public interface Storage {
    void init();
    void close();
    int getLevel(UUID uuid, String npcType);
    void setLevel(UUID uuid, String npcType, int level);
    java.util.Map<String, Integer> getProgress(UUID uuid);
    void saveProgress(UUID uuid, java.util.Map<String, Integer> progress);

    void setQuestStart(UUID uuid, long time);
    void setQuestEnd(UUID uuid, long time);
    java.util.Map<String, Long> getTopFarmers(int limit);

    double getBalance(UUID uuid);
    void setBalance(UUID uuid, double amount);
}
