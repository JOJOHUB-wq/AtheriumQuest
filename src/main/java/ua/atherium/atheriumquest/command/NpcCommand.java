package ua.atherium.atheriumquest.command;

import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import ua.atherium.atheriumquest.AtheriumQuest;
import ua.atherium.atheriumquest.quest.NpcType;

public class NpcCommand implements CommandExecutor {

    private final AtheriumQuest plugin;

    public NpcCommand(AtheriumQuest plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("atq.admin")) {
            player.sendMessage("No permission.");
            return true;
        }

        if (args.length < 2 || !args[0].equalsIgnoreCase("npc")) {
            return false;
        }

        String typeStr = args[1].toUpperCase();
        NpcType type;
        try {
            type = NpcType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            player.sendMessage("Invalid NPC type. Use: FARMER, ALCHEMIST, WEAPONS");
            return true;
        }

        Entity target = player.getTargetEntity(5);
        if (target == null) {
            player.sendMessage("You are not looking at an entity.");
            return true;
        }

        NamespacedKey key = new NamespacedKey(plugin, "npc_type");
        PersistentDataContainer container = target.getPersistentDataContainer();
        container.set(key, PersistentDataType.STRING, type.name());

        player.sendMessage("Bound " + type.name() + " to " + target.getType().name());
        return true;
    }
}
