package ua.atherium.atheriumquest.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import ua.atherium.atheriumquest.quest.NpcType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AdminTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("atherium.admin")) return new ArrayList<>();

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            String[] subs = {"reload", "skip", "shop"};
            for (String s : subs) {
                if (s.startsWith(args[0].toLowerCase())) completions.add(s);
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("skip")) {
            return null;
        } else if (args.length == 3 && args[0].equalsIgnoreCase("skip")) {
            for (NpcType type : NpcType.values()) {
                if (type.getId().startsWith(args[2].toLowerCase())) completions.add(type.getId());
            }
        } else if (args.length == 4 && args[0].equalsIgnoreCase("skip")) {
            completions.add("1");
            completions.add("5");
        }

        return completions;
    }
}
