package io.github.wall69.ancientnightmare.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AncientNightmareTab implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player))
            return null;

        Player p = (Player) sender;
        List<String> results = new ArrayList<>();

        boolean hasPlayerPermission = p.hasPermission("ancientnightmare.player");
        boolean hasAdminPermission = p.hasPermission("ancientnightmare.admin");

        switch (args.length) {
            case 1:
                if (hasPlayerPermission) {
                    results.add("join");
                    results.add("leave");
                    results.add("stats");
                    results.add("queue");
                }
                if (hasAdminPermission) {
                    results.add("admin");
                }
                break;

            case 2:
                if (hasPlayerPermission) {
                    if (args[0].equalsIgnoreCase("stats")) {
                        for (Player online : Bukkit.getServer().getOnlinePlayers()) {
                            if (p == online)
                                continue;

                            results.add(online.getName());
                        }
                    }
                }
                if (hasAdminPermission) {
                    if (args[0].equalsIgnoreCase("admin")) {
                        results.add("setgloballobby");
                        results.add("create");
                        results.add("delete");
                        results.add("reload");
                        results.add("blocks");
                        results.add("skins");
                    }
                }
                break;
            case 3:
                if (hasAdminPermission) {
                    if (args[1].equalsIgnoreCase("blocks")) {
                        results.add("list");
                        results.add("set");
                    } else if (args[1].equalsIgnoreCase("skins")) {
                        results.add("warden");
                        results.add("security");
                    }
                }
                break;
            case 4:
                if (hasAdminPermission) {
                    if (args[2].equalsIgnoreCase("set")) {
                        results.add("generator");
                        results.add("battery-supply");
                    } else if (args[2].equalsIgnoreCase("warden")) {
                        results.add("default");
                        results.add("<URL>");
                    } else if (args[2].equalsIgnoreCase("security")) {
                        results.add("male");
                        results.add("female");
                    }
                }
                break;
            case 5:
                if (hasAdminPermission) {
                    if (args[3].equalsIgnoreCase("male") ||
                            args[3].equalsIgnoreCase("female")) {
                        results.add("default");
                        results.add("<URL>");
                    }
                }
                break;
        }

        return StringUtil.copyPartialMatches(args[args.length - 1], results, new ArrayList<>());
    }

}
