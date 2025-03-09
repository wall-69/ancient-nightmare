package io.github.wall69.ancientnightmare.commands;

import io.github.wall69.ancientnightmare.Main;
import io.github.wall69.ancientnightmare.arena.Arena;
import io.github.wall69.ancientnightmare.arena.ArenaSetup;
import io.github.wall69.ancientnightmare.arena.ArenaState;
import io.github.wall69.ancientnightmare.arena.JoinGUI;
import io.github.wall69.ancientnightmare.game.GameBlock;
import io.github.wall69.ancientnightmare.game.jumpscare.jumpscares.HeadJumpscare;
import io.github.wall69.ancientnightmare.utils.PlayerStatistic;
import net.skinsrestorer.api.exception.DataRequestException;
import net.skinsrestorer.api.exception.MineSkinException;
import net.skinsrestorer.api.property.SkinIdentifier;
import net.skinsrestorer.api.property.SkinProperty;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Random;
import java.util.UUID;

public class AncientNightmareCommand implements CommandExecutor {

    private final Main main;

    public AncientNightmareCommand(Main main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(main.prefix + "§fThis command is players only.");
            return true;
        }

        Player p = (Player) sender;

        if (!p.hasPermission("ancientnightmare.player")) {
            p.sendMessage(main.prefix + main.getFileUtils().getString("commands.player.no-permission"));
            return true;
        }

        if (args.length == 0) {
            for (String s : main.getFileUtils().getStringList("commands.player.no-args")) {
                p.sendMessage(main.prefix + s);
            }
            return true;
        }

        Arena arena = main.getArenaManager().getArena(p);

        switch (args[0].toLowerCase()) {
            case "join":
                if (main.getFileUtils().isProxy()){
                    break;
                }

                if (main.getFileUtils().getGlobalLobbyLocation() == null) {
                    p.sendMessage(main.prefix + main.getFileUtils().getString("commands.player.join-global-lobby-not-set"));
                    break;
                }

                if (main.getQueue().getCurrentPlayers().contains(p.getUniqueId())) {
                    p.sendMessage(main.prefix + main.getFileUtils().getString("commands.player.queue-already-queued"));
                    break;
                }

                if (arena != null) {
                    p.sendMessage(main.prefix + main.getFileUtils().getString("commands.player.join-already-joined"));
                    break;
                }

                if (args.length > 2) {
                    p.sendMessage(main.prefix + main.getFileUtils().getString("commands.player.join-invalid-usage"));
                    break;
                }

                if (args.length == 2) {
                    String arenaName = args[1];
                    Arena specifiedArena = main.getArenaManager().getArena(arenaName);

                    if (specifiedArena == null) {
                        p.sendMessage(main.prefix + main.getFileUtils()
                                .getString("commands.player.join-invalid-specified"));
                    } else {
                        if (specifiedArena.getState() == ArenaState.WAITING ||
                                (specifiedArena.getState() == ArenaState.COUNTDOWN && specifiedArena.getPlayers().size() != 2)) {
                            specifiedArena.addPlayer(p);

                            p.sendMessage(main.prefix + main.getFileUtils().getString("commands.player.join")
                                    .replace("{arena}", specifiedArena.getName()));

                            p.closeInventory();
                        } else if (specifiedArena.getState() == ArenaState.PLAYING
                                || specifiedArena.getState() == ArenaState.ENDING) {
                            p.sendMessage(main.prefix + main.getFileUtils().getString("commands.player.join-arena-playing"));
                        } else if (specifiedArena.getPlayers().size() == 2) {
                            p.sendMessage(main.prefix + main.getFileUtils().getString("commands.player.join-arena-full"));
                        }
                    }
                } else {
                    new JoinGUI(main, p.getUniqueId(), 1);
                }
                break;

            case "leave":
                if (args.length != 1) {
                    p.sendMessage(main.prefix + main.getFileUtils().getString("commands.player" +
                            ".leave-invalid-usage"));
                    break;
                }

                if (arena != null) {
                    p.sendMessage(main.prefix + main.getFileUtils().getString("commands.player.leave"));

                    arena.removePlayer(p);
                } else {
                    p.sendMessage(main.prefix + main.getFileUtils().getString("commands.player" +
                            ".leave-not-joined"));
                }
                break;

            case "stats":
                if (args.length > 2) {
                    p.sendMessage(main.prefix + main.getFileUtils().getString("commands.player.stats-invalid-usage"));
                    break;
                }

                OfflinePlayer who = args.length == 2 ? Bukkit.getOfflinePlayer(args[1]) : p;

                p.sendMessage(main.prefix + main.getFileUtils().getString("commands.player.stats-header")
                        .replace("{name}", who.getName()));

                for (PlayerStatistic playerStatistic : PlayerStatistic.values()) {
                    p.sendMessage(main.prefix + main.getFileUtils().getString("commands.player.stats-syntax")
                            .replace("{statisticName}",
                                    main.getFileUtils().getString("statistic." +
                                            playerStatistic.getName().replace("_", "-")))
                            .replace("{statistic}",
                                    String.valueOf(main.getFileUtils().getPlayerStatistic(who.getUniqueId(),
                                            playerStatistic))));
                }
                break;

            case "queue":
                if (main.getFileUtils().getGlobalLobbyLocation() == null) {
                    p.sendMessage(main.prefix + main.getFileUtils().getString
                            ("commands.player.join-global-lobby-not-set"));
                    break;
                }

                if (main.getArenaManager().getArena(p) != null) {
                    p.sendMessage(main.prefix + main.getFileUtils().getString("commands.player.join-already-joined"));
                    break;
                }
                // TODO zvuky?
                if (main.getQueue().getCurrentPlayers().contains(p.getUniqueId())) {
                    main.getQueue().removePlayer(p.getUniqueId());
                    p.sendMessage(main.prefix + main.getFileUtils().getString("commands.player.queue-leave"));
                    break;
                }

                main.getQueue().addPlayer(p.getUniqueId());
                p.sendMessage(main.prefix + main.getFileUtils().getString("commands.player.queue-join"));
                break;

            case "admin":
                if (!p.hasPermission("ancientnightmare.admin")) {
                    p.sendMessage(main.prefix + main.getFileUtils().getString("commands.admin.no-permission"));
                    return true;
                }

                if (args.length == 1) {
                    p.sendMessage(main.prefix + "§cADMIN §7COMMANDS: ");
                    p.sendMessage(main.prefix + "§7/an admin setgloballobby");
                    p.sendMessage(main.prefix + "§7/an admin create <ArenaName> <GameTime[s]>");
                    p.sendMessage(main.prefix + "§7/an admin delete <ArenaName>");
                    p.sendMessage(main.prefix + "§7/an admin reload");
                    p.sendMessage(main.prefix + "§7/an admin blocks <list/set>");
                    p.sendMessage(main.prefix + "§7/an admin skins <warden/security>");
                    return true;
                }

                switch (args[1].toLowerCase()) {
                    case "setgloballobby":
                        main.getFileUtils().setLobbyLocation(p.getLocation());
                        p.sendMessage(main.prefix + "§cGlobal lobby was set!");
                        break;

                    case "create":
                        if (main.getFileUtils().getGlobalLobbyLocation() == null) {
                            p.sendMessage(main.prefix + "§cPlease set the global lobby first! (/an admin " +
                                    "setgloballobby)");
                            break;
                        }
                        if (args.length != 4) {
                            p.sendMessage(main.prefix + "§cUsage: /an admin create <ArenaName> " +
                                    "<GameTime[s]>");
                            break;
                        }

                        String arenaName = args[2];
                        int gameTime;

                        if (main.getArenaManager().getArena(arenaName) != null) {
                            p.sendMessage(main.prefix + "§cThis arena name is already being used!");
                            break;
                        }

                        try {
                            gameTime = Integer.parseInt(args[3]);
                        } catch (NumberFormatException ex) {
                            p.sendMessage(main.prefix + "§cThe game time must be a number!");
                            break;
                        }

                        main.getFileUtils().setArenaGameTime(arenaName, gameTime);

                        ArenaSetup setup = new ArenaSetup(main, p.getUniqueId(), arenaName);
                        Bukkit.getServer().getPluginManager().registerEvents(setup, main);
                        setup.start();
                        break;

                    case "delete":
                        if (args.length < 3) {
                            p.sendMessage(main.prefix + "§cUsage: /an admin delete <ArenaName>");
                            break;
                        }

                        if (main.getArenaManager().getArena(args[2]) != null) {
                            main.getArenaManager().deleteArena(args[2]);
                            p.sendMessage(main.prefix + "§cArena named " + args[2] + " was deleted!");
                            break;
                        }

                        p.sendMessage(main.prefix + "§cYou specified invalid arena name !");
                        break;

                    case "reload":
                        main.getFileUtils().reloadAllFiles();
                        main.prefix = main.getFileUtils().getString("prefix") + " ";

                        p.sendMessage(main.prefix + "§cReloaded!");
                        break;

                    case "blocks":
                        if (args.length < 3) {
                            p.sendMessage(main.prefix + "§cUsage /an admin blocks <list/set>");
                            break;
                        }

                        if (args[2].equalsIgnoreCase("list")) {
                            p.sendMessage(main.prefix + "§cGame block - material type");

                            for (GameBlock gameBlock : GameBlock.values()) {
                                p.sendMessage(main.prefix + "§7" + gameBlock.toString() + " - " + gameBlock.getType().name());
                            }
                        } else if (args[2].equalsIgnoreCase("set")) {
                            if (args.length < 4 || !(args[3].equalsIgnoreCase("generator")
                                    || args[3].equalsIgnoreCase("battery-supply"))) {
                                p.sendMessage(main.prefix + "§cUsage: /an admin blocks set <generator/battery-supply>");
                                break;
                            }
                            ItemStack itemInHand = p.getEquipment().getItemInMainHand();

                            if (itemInHand == null || itemInHand.getType() == Material.AIR ||
                                    !itemInHand.getType().isBlock()) {
                                p.sendMessage(main.prefix + "§cYou need to have a block in your hand!");
                                break;
                            }

                            main.getFileUtils().setBlockType(args[3], itemInHand.getType());
                            for (GameBlock gameBlock : GameBlock.values()) {
                                gameBlock.setType(main.getFileUtils().getBlockType(gameBlock));
                            }
                            p.sendMessage(main.prefix + "§cSet " + args[3] + " to " + itemInHand.getType().name() + "!");

                            if (main.getArenaManager().getArenas().size() == 0)
                                break;

                            p.sendMessage(main.prefix + "§4Starting to replace all " + args[3] + " blocks to "
                                    + itemInHand.getType().name() + ", this can take some time!");

                            new BukkitRunnable() {

                                int i = 0;

                                @Override
                                public void run() {
                                    if (i >= main.getArenaManager().getArenas().size()) {
                                        p.sendMessage(main.prefix + "§cAll blocks were successfully replaced in all arenas!");
                                        cancel();
                                        return;
                                    }

                                    if (args[3].equalsIgnoreCase("generator")) {
                                        main.getArenaManager().getArenas().get(i)
                                                .getGeneratorLocation().getBlock().setType(itemInHand.getType());
                                    } else {
                                        main.getArenaManager().getArenas().get(i)
                                                .getBatterySupplyLocation().getBlock().setType(itemInHand.getType());
                                    }

                                    i++;
                                }
                            }.runTaskTimer(main, 20L, 20L);
                        } else {
                            p.sendMessage(main.prefix + "§cUsage /an admin blocks <list/set>");
                            break;
                        }
                        break;

                    case "skins":
                        if (!main.getFileUtils().useSkins()) {
                            p.sendMessage(main.prefix + "§cTo set custom skins you need to enable them in config.yml (use-skins)!");
                            break;
                        }
                        if (main.getSkinsRestorerAPI() == null) {
                            p.sendMessage(main.prefix + "§cThe SkinsRestorerAPI is null, try to restart the server or contact support (Discord or Spigot).");
                            break;
                        }
                        if (args.length < 3) {
                            p.sendMessage(main.prefix + "§cUsage /an admin skins <warden/security>");
                            break;
                        }
                        if (args[2].equals("warden")) {
                            if (args.length < 4) {
                                p.sendMessage(main.prefix + "§cUsage /an admin skins warden <default/URL>");
                                p.sendMessage(main.prefix + "§cURL should be URL of a image of a minecraft skin!");
                                break;
                            }

                            if (args[3].equals("default")) {
                                main.getFileUtils().setWardenSkin(main.getSkinUtils().getDefaultWardenValue(),
                                        main.getSkinUtils().getDefaultWardenSignature());
                                main.getSkinUtils().setupWardenSkin(main);
                                p.sendMessage(main.prefix + "§cSkin for Warden was set to default!");
                                break;
                            }

                            try {
                                main.getSkinUtils().setWardenSkin(main, args[3]);
                            } catch (Exception e) {
                                p.sendMessage(main.prefix + "§cInvalid skin url or format.");
                                break;
                            }

                            p.sendMessage(main.prefix + "§cSkin for Warden was set!");
                        } else if (args[2].equals("security")) {
                            if (args.length < 5) {
                                p.sendMessage(main.prefix + "§cUsage /an admin skins security <male/female> <default/URL>");
                                p.sendMessage(main.prefix + "§cURL should be URL of a image of a minecraft skin!");
                                break;
                            }

                            if (!args[3].equals("male") && !args[3].equals("female")) {
                                p.sendMessage(main.prefix + "§cUsage /an admin skins security <male/female> <default/URL>");
                                break;
                            }

                            if (args[4].equals("default")) {
                                if (args[3].equals("male")) {
                                    main.getFileUtils().setSecurityMaleSkin(main.getSkinUtils().getDefaultSecurityMaleValue(),
                                            main.getSkinUtils().getDefaultSecurityMaleSignature());
                                    main.getSkinUtils().setupSecurityMaleSkin(main);
                                } else {
                                    main.getFileUtils().setSecurityFemaleSkin(main.getSkinUtils().getDefaultSecurityFemaleValue(),
                                            main.getSkinUtils().getDefaultSecurityFemaleSignature());
                                    main.getSkinUtils().setupSecurityFemaleSkin(main);
                                }

                                p.sendMessage(main.prefix + "§cSkin for " + args[3] + " Security was set to default!");
                                break;
                            }

                            try {
                                main.getSkinUtils().setSecuritySkin(main, args[4], args[3]);

                                p.sendMessage(main.prefix + "§cSkin for " + args[3] + " Security was set!");
                            } catch (Exception e) {
                                p.sendMessage(main.prefix + "§cInvalid skin url or format.");
                                break;
                            }
                        } else {
                            p.sendMessage(main.prefix + "§cUsage /an admin skins <warden/security>");
                            break;
                        }
                        break;
                    default:
                        p.sendMessage(main.prefix + "§cYou specified invalid command.");
                        break;
                }
                break;

            default:
                for (String s : main.getFileUtils().getStringList("commands.player.no-args")) {
                    p.sendMessage(main.prefix + s);
                }
                break;
        }

        return true;
    }
}
