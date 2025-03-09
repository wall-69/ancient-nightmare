package io.github.wall69.ancientnightmare.listeners;

import io.github.wall69.ancientnightmare.Main;
import io.github.wall69.ancientnightmare.UpdateChecker;
import io.github.wall69.ancientnightmare.arena.Arena;
import io.github.wall69.ancientnightmare.arena.ArenaState;
import io.github.wall69.ancientnightmare.arena.JoinGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

public class JoinQuitListener implements Listener {

    private final Main main;

    public JoinQuitListener(Main main) {
        this.main = main;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();

        if (p.hasPermission("ancientnightmare.admin")) {
            if (main.getFileUtils().checkUpdates()) {
                UpdateChecker updateChecker = new UpdateChecker(main, 104076);
                String latestVersion = updateChecker.getLatestVersionString();
                String currentVersion = updateChecker.getCurrentVersionString();

                if (updateChecker.getUpdateCheckResult() == UpdateChecker.UpdateCheckResult.OUT_DATED) {
                    p.sendMessage(main.prefix + "§cYou are running old version of AncientNightmare!"
                            + " (Latest: §8" + latestVersion + "§c, Current: §8" + currentVersion + "§c)");
                }
            }
        }

        if (main.getFileUtils().isProxy()) {
            Arena arena = main.getArenaManager().getFirst();

            if (arena != null) {
                e.setJoinMessage(null);
                if (arena.getState() == ArenaState.WAITING || arena.getState() == ArenaState.COUNTDOWN && arena.getPlayers().size() != 2) {
                    arena.addPlayer(p);
                    p.sendMessage(main.prefix + main.getFileUtils().getString("commands.player.join").replace("{arena}", arena.getName()));
                } else if (arena.getState() != ArenaState.PLAYING && arena.getState() != ArenaState.ENDING) {
                    if (arena.getPlayers().size() == 2) {
                        p.kickPlayer(main.prefix + main.getFileUtils().getString("commands.player.join-arena-full"));
                    }
                } else {
                    p.kickPlayer(main.prefix + main.getFileUtils().getString("commands.player.join-arena-playing"));
                }
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();

        Arena arena = main.getArenaManager().getArena(p);
        if (arena != null) {
            e.setQuitMessage(null);

            if (main.getFileUtils().isProxy() && e.getQuitMessage().equals(p.getName() + " ancientnightmare kick")) {
                return;
            }

            arena.removePlayer(p);
        }

        if (main.getQueue().getCurrentPlayers().contains(p.getUniqueId())) {
            main.getQueue().removePlayer(p.getUniqueId());
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        InventoryView inventoryView = e.getView();
        Player p = (Player) e.getWhoClicked();

        if (!inventoryView.getTitle().equals(main.getFileUtils().getString("join-gui.title")))
            return;

        e.setCancelled(true);

        ItemStack clickedItem = e.getCurrentItem();

        if (clickedItem == null || clickedItem.getItemMeta() == null)
            return;

        String localizedName = clickedItem.getItemMeta().getLocalizedName();

        if (localizedName.startsWith("an_guiJoin")) {
            String arenaName = localizedName.split("an_guiJoin")[1];

            Arena arena = main.getArenaManager().getArena(arenaName);

            if (arena != null) {
                if (arena.getState() == ArenaState.WAITING ||
                        (arena.getState() == ArenaState.COUNTDOWN && arena.getPlayers().size() != 2)) {
                    main.getArenaManager().getArena(arenaName).addPlayer(p);

                    p.sendMessage(main.prefix + main.getFileUtils().getString("commands.player.join")
                            .replace("{arena}", main.getArenaManager().getArena(arenaName).getName()));

                    p.closeInventory();
                } else if (arena.getState() == ArenaState.PLAYING || arena.getState() == ArenaState.ENDING) {
                    p.sendMessage(main.prefix + main.getFileUtils().getString("commands.player.join-arena-playing"));
                } else if (arena.getPlayers().size() == 2) {
                    p.sendMessage(main.prefix + main.getFileUtils().getString("commands.player.join-arena-full"));
                }
            } else {
                p.sendMessage(main.prefix + main.getFileUtils().getString("commands.player.join-arena-invalid"));
            }

        } else if (localizedName.startsWith("an_guiGo")) {
            String where = localizedName.split("an_guiGo")[1].toLowerCase();
            int page = 1;

            if (where.contains("left")) {
                page = Integer.parseInt(where.split("left")[1]);

                new JoinGUI(main, p.getUniqueId(), page - 1);
            } else if (where.contains("right")) {
                page = Integer.parseInt(where.split("right")[1]);

                new JoinGUI(main, p.getUniqueId(), page + 1);
            }
        }
    }

}
