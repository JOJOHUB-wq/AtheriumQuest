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
        new ua.atherium.atheriumquest.gui.QuestGui(plugin, player, type).open();
        return true;
    }
}
