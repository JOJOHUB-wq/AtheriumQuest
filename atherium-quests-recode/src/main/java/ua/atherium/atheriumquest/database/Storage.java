package ua.atherium.atheriumquest.database;

import java.util.UUID;
import java.util.Map;

public interface Storage {
    void init();
    void close();


    int getLevel(UUID uuid, String npcType);
    void setLevel(UUID uuid, String npcType, int level);


    int getQuestProgress(UUID uuid, String npcType, int level, String questId);
    void setQuestProgress(UUID uuid, String npcType, int level, String questId, int progress);

    boolean isQuestCompleted(UUID uuid, String npcType, int level, String questId);
    void setQuestCompleted(UUID uuid, String npcType, int level, String questId, boolean completed);


    int getShopPurchaseCount(UUID uuid, String itemId);
    long getShopPurchaseTime(UUID uuid, String itemId);
    void setShopPurchase(UUID uuid, String itemId, int count, long lastRefresh);


    int getCompletedLevels(UUID uuid, String npcType);
    void setCompletedLevels(UUID uuid, String npcType, int levels);


    int getTotalQuestsCompleted(UUID uuid);
    int getTotalLevelsCompleted(UUID uuid);
}
