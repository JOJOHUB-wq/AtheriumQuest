package ua.atherium.atheriumquest.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ua.atherium.atheriumquest.AtheriumQuests;
import ua.atherium.atheriumquest.quest.NpcType;
import ua.atherium.atheriumquest.quest.Quest;
import ua.atherium.atheriumquest.quest.QuestLevel;

public class AdminCommand implements CommandExecutor {

    private final AtheriumQuests plugin;

    public AdminCommand(AtheriumQuests plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("atherium.admin")) {
            sender.sendMessage(plugin.getConfigManager().parse(plugin.getConfigManager().getMessage("no_permission")));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(plugin.getConfigManager().parse(plugin.getConfigManager().getMessage("invalid_command")));
            return true;
        }

        String sub = args[0].toLowerCase();

        if (sub.equals("reload")) {
            if (!sender.hasPermission("atherium.admin.reload")) {
                sender.sendMessage(plugin.getConfigManager().parse(plugin.getConfigManager().getMessage("no_permission")));
                return true;
            }
            plugin.getConfigManager().reloadConfigs();
            plugin.getQuestManager().loadQuests();
            sender.sendMessage(plugin.getConfigManager().parse(plugin.getConfigManager().getMessage("admin_reload_success")));
            return true;
        } else if (sub.equals("shop")) {
            if (!sender.hasPermission("atherium.admin.shop")) {
                sender.sendMessage(plugin.getConfigManager().parse(plugin.getConfigManager().getMessage("no_permission")));
                return true;
            }
            if (!(sender instanceof Player)) {
                sender.sendMessage(plugin.getConfigManager().parse(plugin.getConfigManager().getMessage("player_only")));
                return true;
            }
            plugin.getShopEditorListener().startSession((Player) sender);
            return true;
        } else if (sub.equals("skip")) {
            if (!sender.hasPermission("atherium.admin.skip")) {
                sender.sendMessage(plugin.getConfigManager().parse(plugin.getConfigManager().getMessage("no_permission")));
                return true;
            }

            if (args.length < 3) {
                 sender.sendMessage(plugin.getConfigManager().parse("&cUsage: /atq skip <player> <npc> [amount]"));
                 return true;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(plugin.getConfigManager().parse(plugin.getConfigManager().getMessage("admin_skip_player_not_found")));
                return true;
            }

            NpcType npc = NpcType.fromId(args[2]);
            if (npc == null) {
                sender.sendMessage(plugin.getConfigManager().parse(plugin.getConfigManager().getMessage("admin_skip_invalid_npc")));
                return true;
            }

            int amount = 1;
            if (args.length > 3) {
                try {
                    amount = Integer.parseInt(args[3]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(plugin.getConfigManager().parse("&cInvalid amount"));
                    return true;
                }
            }





            int skipped = 0;
            for (int i = 0; i < amount; i++) {
                Quest active = plugin.getQuestManager().getActiveQuest(target, npc);
                if (active == null) {
                    sender.sendMessage(plugin.getConfigManager().parse("&cNo active quest for " + target.getName() + " (Level completed or finished)"));
                    break;
                }

                int level = plugin.getStorageManager().getStorage().getLevel(target.getUniqueId(), npc.getId());









                plugin.getQuestManager().forceCompleteActiveQuest(target, npc);
                skipped++;
            }

            String msg = plugin.getConfigManager().getMessage("admin_skip_success")
                .replace("%amount%", String.valueOf(skipped))
                .replace("%player%", target.getName())
                .replace("%npc%", npc.getName());
            sender.sendMessage(plugin.getConfigManager().parse(msg));
            return true;
        }

        sender.sendMessage(plugin.getConfigManager().parse(plugin.getConfigManager().getMessage("invalid_command")));
        return true;
    }
}
