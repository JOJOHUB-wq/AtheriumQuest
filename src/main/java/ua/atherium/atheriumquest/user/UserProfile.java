package ua.atherium.atheriumquest.user;

import java.util.UUID;
import ua.atherium.atheriumquest.quest.NpcType;

public class UserProfile {
    private final UUID uuid;
    private int farmerLevel;
    private int alchemistLevel;
    private int weaponsLevel;
    private final java.util.Map<String, Integer> progress = new java.util.HashMap<>();

    public UserProfile(UUID uuid) {
        this.uuid = uuid;
        this.farmerLevel = 1;
        this.alchemistLevel = 1;
        this.weaponsLevel = 1;
    }

    public UUID getUuid() {
        return uuid;
    }

    public int getProgress(String key) {
        return progress.getOrDefault(key, 0);
    }

    public void setProgress(String key, int amount) {
        progress.put(key, amount);
    }

    public java.util.Map<String, Integer> getProgressMap() {
        return progress;
    }

    public int getLevel(NpcType type) {
        switch (type) {
            case FARMER: return farmerLevel;
            case ALCHEMIST: return alchemistLevel;
            case WEAPONS: return weaponsLevel;
            default: return 1;
        }
    }

    public void setLevel(NpcType type, int level) {
         switch (type) {
            case FARMER: farmerLevel = level; break;
            case ALCHEMIST: alchemistLevel = level; break;
            case WEAPONS: weaponsLevel = level; break;
        }
    }
}
