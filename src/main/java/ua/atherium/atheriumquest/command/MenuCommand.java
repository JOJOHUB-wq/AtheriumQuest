package ua.atherium.atheriumquest.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ua.atherium.atheriumquest.AtheriumQuest;
import ua.atherium.atheriumquest.quest.NpcType;

public class MenuCommand implements CommandExecutor {

    private final AtheriumQuest plugin;
    private final NpcType type;

    public MenuCommand(AtheriumQuest plugin, NpcType type) {
        this.plugin = plugin;
        this.type = type;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        ua.atherium.atheriumquest.user.UserProfile profile = plugin.getUserManager().getUser(player.getUniqueId());
        int currentLevel = profile.getLevel(type);
        ua.atherium.atheriumquest.quest.QuestLevel qLevel = plugin.getQuestManager().getQuestLevel(type, currentLevel);

        if (qLevel != null) {
            boolean allComplete = true;
            for (int i = 0; i < qLevel.getTasks().size(); i++) {
                 String key = type.name() + "_" + currentLevel + "_" + i;
                 if (profile.getProgress(key) < qLevel.getTasks().get(i).getAmount()) {
                     allComplete = false;
                     break;
                 }
            }

            if (allComplete) {
                profile.setLevel(type, currentLevel + 1);
                plugin.getUserManager().saveUser(player.getUniqueId());

                String msg = plugin.getConfigManager().getConfig().getString("messages.level_up", "Level {level}").replace("{level}", String.valueOf(currentLevel + 1));
                player.sendMessage(plugin.getConfigManager().parse(msg));
            }
        }

        new ua.atherium.atheriumquest.shop.ShopMenu(plugin, player).open();
        return true;
    }
}
