package ua.atherium.atheriumquest.command;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ua.atherium.atheriumquest.AtheriumQuest;
import ua.atherium.atheriumquest.quest.NpcType;
import ua.atherium.atheriumquest.user.UserProfile;

public class AdminCommand implements CommandExecutor {

    private final AtheriumQuest plugin;

    public AdminCommand(AtheriumQuest plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("atq.admin")) {
             sender.sendMessage("No permission.");
             return true;
        }

        if (args.length == 0) {
            sender.sendMessage("Usage: /atq <reload|skip|npc>");
            return true;
        }

        String sub = args[0].toLowerCase();

        if (sub.equals("reload")) {
            plugin.getConfigManager().reloadConfig();
            plugin.getQuestManager().loadQuests();
            sender.sendMessage("Reloaded config and quests.");
            return true;
        }

        if (sub.equals("skip")) {
            if (args.length < 4) {
                sender.sendMessage("Usage: /atq skip <player> <npc> <amount>");
                return true;
            }

            Player target = plugin.getServer().getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage("Player not found.");
                return true;
            }

            NpcType type;
            try {
                type = NpcType.valueOf(args[2].toUpperCase());
            } catch (IllegalArgumentException e) {
                sender.sendMessage("Invalid NPC: FARMER, ALCHEMIST, WEAPONS");
                return true;
            }

            int amount = 0;
            try {
                amount = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                sender.sendMessage("Invalid amount.");
                return true;
            }

            UserProfile profile = plugin.getUserManager().getUser(target.getUniqueId());
            int current = profile.getLevel(type);
            profile.setLevel(type, current + amount);
            plugin.getUserManager().saveUser(target.getUniqueId());
            sender.sendMessage("Skipped " + amount + " levels for " + target.getName() + " in " + type.name());
            return true;
        }

        if (sub.equals("npc")) {
            return new NpcCommand(plugin).onCommand(sender, command, label, args);
        }

        if (sub.equals("shop")) {
            if (sender instanceof Player p) {
                plugin.getShopEditorListener().toggleEditor(p);
            } else {
                sender.sendMessage("Players only.");
            }
            return true;
        }

        if (sub.equals("top")) {
            java.util.Map<String, Long> top = plugin.getDatabaseManager().getStorage().getTopFarmers(10);
            sender.sendMessage(plugin.getConfigManager().parse("<gradient:#00ff00:#00aa00>Top 10 Fastest Farmers:</gradient>"));
            int i = 1;
            for (java.util.Map.Entry<String, Long> entry : top.entrySet()) {
                String uuid = entry.getKey();
                long millis = entry.getValue();
                String name = org.bukkit.Bukkit.getOfflinePlayer(java.util.UUID.fromString(uuid)).getName();
                if (name == null) name = "Unknown";

                long seconds = millis / 1000;
                long minutes = seconds / 60;
                seconds = seconds % 60;

                sender.sendMessage(plugin.getConfigManager().parse("<gray>" + i + ". <gold>" + name + " <white>- <aqua>" + minutes + "m " + seconds + "s"));
                i++;
            }
            return true;
        }

        return false;
    }
}
