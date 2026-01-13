package ua.atherium.atheriumquest.user;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import ua.atherium.atheriumquest.AtheriumQuest;
import ua.atherium.atheriumquest.database.Storage;
import ua.atherium.atheriumquest.quest.NpcType;

public class UserManager {
    private final AtheriumQuest plugin;
    private final Map<UUID, UserProfile> users = new HashMap<>();

    public UserManager(AtheriumQuest plugin) {
        this.plugin = plugin;
    }

    public UserProfile getUser(UUID uuid) {
        if (!users.containsKey(uuid)) {
            loadUser(uuid);
        }
        return users.get(uuid);
    }

    public void loadUser(UUID uuid) {
        UserProfile profile = new UserProfile(uuid);
        Storage storage = plugin.getDatabaseManager().getStorage();
        if (storage != null) {
            profile.setLevel(NpcType.FARMER, storage.getLevel(uuid, "FARMER"));
            profile.setLevel(NpcType.ALCHEMIST, storage.getLevel(uuid, "ALCHEMIST"));
            profile.setLevel(NpcType.WEAPONS, storage.getLevel(uuid, "WEAPONS"));
            profile.setQuestIndex(NpcType.FARMER, storage.getQuestIndex(uuid, "FARMER"));
            profile.setQuestIndex(NpcType.ALCHEMIST, storage.getQuestIndex(uuid, "ALCHEMIST"));
            profile.setQuestIndex(NpcType.WEAPONS, storage.getQuestIndex(uuid, "WEAPONS"));
            Map<String, Integer> progress = storage.getProgress(uuid);
            for (Map.Entry<String, Integer> entry : progress.entrySet()) {
                profile.setProgress(entry.getKey(), entry.getValue());
            }
        }
        users.put(uuid, profile);
    }

    public void saveUser(UUID uuid) {
        UserProfile profile = users.get(uuid);
        if (profile != null) {
            Storage storage = plugin.getDatabaseManager().getStorage();
            if (storage != null) {
                storage.setLevel(uuid, "FARMER", profile.getLevel(NpcType.FARMER));
                storage.setLevel(uuid, "ALCHEMIST", profile.getLevel(NpcType.ALCHEMIST));
                storage.setLevel(uuid, "WEAPONS", profile.getLevel(NpcType.WEAPONS));
                storage.setQuestIndex(uuid, "FARMER", profile.getQuestIndex(NpcType.FARMER));
                storage.setQuestIndex(uuid, "ALCHEMIST", profile.getQuestIndex(NpcType.ALCHEMIST));
                storage.setQuestIndex(uuid, "WEAPONS", profile.getQuestIndex(NpcType.WEAPONS));
                storage.saveProgress(uuid, profile.getProgressMap());
            }
        }
    }
}
