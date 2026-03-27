package ua.atherium.atheriumquest.integration;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import ua.atherium.atheriumquest.AtheriumQuests;
import ua.atherium.atheriumquest.quest.NpcType;
import ua.atherium.atheriumquest.quest.Quest;

public class AtheriumQuestsPlaceholderExpansion extends PlaceholderExpansion {

    private final AtheriumQuests plugin;

    public AtheriumQuestsPlaceholderExpansion(AtheriumQuests plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "atherium";
    }

    @Override
    public String getAuthor() {
        return "Oleze_bebidjonov";
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (player == null || !player.isOnline()) return null;




        if (params.equalsIgnoreCase("total_quests")) {
            return String.valueOf(plugin.getStorageManager().getStorage().getTotalQuestsCompleted(player.getUniqueId()));
        }

        if (params.equalsIgnoreCase("total_levels")) {
            return String.valueOf(plugin.getStorageManager().getStorage().getTotalLevelsCompleted(player.getUniqueId()));
        }


        if (params.startsWith("quest_progress_")) {
















            return null;
        }


        for (NpcType npc : NpcType.values()) {
            String id = npc.getId();
            if (params.startsWith(id + "_")) {
                String sub = params.substring(id.length() + 1);

                if (sub.equalsIgnoreCase("level")) {
                    return String.valueOf(plugin.getStorageManager().getStorage().getLevel(player.getUniqueId(), id));
                }

                if (sub.equalsIgnoreCase("completed")) {
                    return String.valueOf(plugin.getStorageManager().getStorage().getCompletedLevels(player.getUniqueId(), id));
                }

                if (sub.equalsIgnoreCase("progress")) {



                    int level = plugin.getStorageManager().getStorage().getLevel(player.getUniqueId(), id);
                    ua.atherium.atheriumquest.quest.QuestLevel qLevel = plugin.getQuestManager().getQuestLevel(npc, level);
                    if (qLevel == null) return "Max";

                    int total = qLevel.getQuests().size();
                    int completed = 0;
                    for (Quest q : qLevel.getQuests().values()) {
                        if (plugin.getStorageManager().getStorage().isQuestCompleted(player.getUniqueId(), id, level, q.getId())) {
                            completed++;
                        }
                    }
                    return completed + "/" + total;
                }

                if (sub.equalsIgnoreCase("total_quests")) {





                    return "0";
                }
            }
        }

        return null;
    }
}
