package ua.atherium.atheriumquest.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import ua.atherium.atheriumquest.AtheriumQuest;
import ua.atherium.atheriumquest.quest.NpcType;

public class AdminTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("atq.admin")) return Collections.emptyList();

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], Arrays.asList("reload", "skip", "npc", "shop", "top"), completions);
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("skip")) {
                return null;
            } else if (args[0].equalsIgnoreCase("npc")) {
                StringUtil.copyPartialMatches(args[1], Arrays.stream(NpcType.values()).map(Enum::name).toList(), completions);
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("skip")) {
                StringUtil.copyPartialMatches(args[2], Arrays.stream(NpcType.values()).map(Enum::name).toList(), completions);
            }
        } else if (args.length == 4) {
            if (args[0].equalsIgnoreCase("skip")) {
                StringUtil.copyPartialMatches(args[3], Arrays.asList("1", "2", "3"), completions);
            }
        }

        Collections.sort(completions);
        return completions;
    }
}
