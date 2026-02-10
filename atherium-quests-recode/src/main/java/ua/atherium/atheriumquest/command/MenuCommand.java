package ua.atherium.atheriumquest.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ua.atherium.atheriumquest.AtheriumQuests;
import ua.atherium.atheriumquest.quest.NpcType;

public class MenuCommand implements CommandExecutor {

    private final AtheriumQuests plugin;
    private final NpcType npcType;

    public MenuCommand(AtheriumQuests plugin, NpcType npcType) {
        this.plugin = plugin;
        this.npcType = npcType;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().parse(plugin.getConfigManager().getMessage("player_only")));
            return true;
        }

        Player player = (Player) sender;

        if (npcType != null) {
            plugin.getMenuManager().openNpcMenu(player, npcType);
            return true;
        }


        if (args.length > 0 && args[0].equalsIgnoreCase("shop")) {
            plugin.getShopManager().openShop(player);
            return true;
        }

        plugin.getMenuManager().openMainMenu(player);
        return true;
    }
}
