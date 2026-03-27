package ua.atherium.atheriumquest.quest;

import org.bukkit.Material;
import java.util.List;

public class Quest {
    private final String id;
    private final String type;
    private final String target;
    private final int amount;
    private final List<String> rewardCommands;


    private final String activeDisplayName;
    private final List<String> activeLore;
    private final Material activeMaterial;
    private final boolean activeEnchanted;

    private final String completedDisplayName;
    private final List<String> completedLore;
    private final Material completedMaterial;
    private final boolean completedEnchanted;

    private final String lockedDisplayName;
    private final List<String> lockedLore;
    private final Material lockedMaterial;
    private final boolean lockedEnchanted;

    public Quest(String id, String type, String target, int amount, List<String> rewardCommands,
                 String activeDisplayName, List<String> activeLore, Material activeMaterial, boolean activeEnchanted,
                 String completedDisplayName, List<String> completedLore, Material completedMaterial, boolean completedEnchanted,
                 String lockedDisplayName, List<String> lockedLore, Material lockedMaterial, boolean lockedEnchanted) {
        this.id = id;
        this.type = type;
        this.target = target;
        this.amount = amount;
        this.rewardCommands = rewardCommands;
        this.activeDisplayName = activeDisplayName;
        this.activeLore = activeLore;
        this.activeMaterial = activeMaterial;
        this.activeEnchanted = activeEnchanted;
        this.completedDisplayName = completedDisplayName;
        this.completedLore = completedLore;
        this.completedMaterial = completedMaterial;
        this.completedEnchanted = completedEnchanted;
        this.lockedDisplayName = lockedDisplayName;
        this.lockedLore = lockedLore;
        this.lockedMaterial = lockedMaterial;
        this.lockedEnchanted = lockedEnchanted;
    }

    public String getId() { return id; }
    public String getType() { return type; }
    public String getTarget() { return target; }
    public int getAmount() { return amount; }
    public List<String> getRewardCommands() { return rewardCommands; }

    public String getActiveDisplayName() { return activeDisplayName; }
    public List<String> getActiveLore() { return activeLore; }
    public Material getActiveMaterial() { return activeMaterial; }
    public boolean isActiveEnchanted() { return activeEnchanted; }

    public String getCompletedDisplayName() { return completedDisplayName; }
    public List<String> getCompletedLore() { return completedLore; }
    public Material getCompletedMaterial() { return completedMaterial; }
    public boolean isCompletedEnchanted() { return completedEnchanted; }

    public String getLockedDisplayName() { return lockedDisplayName; }
    public List<String> getLockedLore() { return lockedLore; }
    public Material getLockedMaterial() { return lockedMaterial; }
    public boolean isLockedEnchanted() { return lockedEnchanted; }
}
