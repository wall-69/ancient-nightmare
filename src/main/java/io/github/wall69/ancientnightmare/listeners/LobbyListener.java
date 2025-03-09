package io.github.wall69.ancientnightmare.listeners;

import io.github.wall69.ancientnightmare.Main;
import io.github.wall69.ancientnightmare.arena.Arena;
import io.github.wall69.ancientnightmare.arena.ArenaState;
import io.github.wall69.ancientnightmare.game.Game;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class LobbyListener implements Listener {

    private final Main main;

    public LobbyListener(Main main) {
        this.main = main;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        ItemStack itemInHand = p.getEquipment().getItemInMainHand();

        if (main.getArenaManager().getArena(p) == null)
            return;

        Arena arena = main.getArenaManager().getArena(p);
        Game game = arena.getGame();

        if ((e.getAction() != Action.RIGHT_CLICK_BLOCK && e.getAction() != Action.RIGHT_CLICK_AIR)
                || arena.getState() == ArenaState.PLAYING || arena.getState() == ArenaState.ENDING
                || !e.getHand().equals(EquipmentSlot.HAND) || itemInHand == null || itemInHand.getItemMeta() == null)
            return;

        switch (itemInHand.getItemMeta().getLocalizedName()) {
            case "an_selectWarden":
                if (game.getWarden() == null) {
                    game.setWarden(p.getUniqueId());
                    p.getInventory().removeItem(main.getItemUtils().getLobbySelectionItem("security"));
                    p.getInventory().removeItem(main.getItemUtils().getLobbySelectionItem("warden"));
                    p.sendMessage(main.prefix + main.getFileUtils().getString("arena.lobby.play-warden"));
                } else {
                    p.sendMessage(main.prefix + main.getFileUtils().getString("arena.lobby.cant-play-warden"));
                }
                break;

            case "an_selectSecurity":
                if (game.getSecurity() == null) {
                    game.setSecurity(p.getUniqueId());
                    p.getInventory().removeItem(main.getItemUtils().getLobbySelectionItem("security"));
                    p.getInventory().removeItem(main.getItemUtils().getLobbySelectionItem("warden"));
                    p.sendMessage(main.prefix + main.getFileUtils().getString("arena.lobby.play-security"));
                } else {
                    p.sendMessage(main.prefix + main.getFileUtils().getString("arena.lobby.cant-play-security"));
                }
                break;

            case "an_leave":
                arena.removePlayer(p);
                p.sendMessage(main.prefix + main.getFileUtils().getString("commands.player.leave"));
                break;
        }

    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();

        if (main.getArenaManager().getArena(p) == null)
            return;

        e.setCancelled(true);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player))
            return;

        Player p = (Player) e.getEntity();

        if (main.getArenaManager().getArena(p) == null)
            return;

        Arena arena = main.getArenaManager().getArena(p);

        if (arena.getState() != ArenaState.PLAYING && arena.getState() != ArenaState.ENDING) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();

        Arena arena = main.getArenaManager().getArena(p);

        if (arena == null)
            return;

        if (e.getMessage().startsWith("/an") || e.getMessage().startsWith("/ancientnightmare"))
            return;

        e.setCancelled(true);
    }

    @EventHandler
    public void onServerListPing(ServerListPingEvent e) {
        if (main.getFileUtils().isProxy()) {
            if (main.getArenaManager().getFirst() == null){
                return;
            }

            e.setMotd(main.getFileUtils().getString("arena-state." + main.getArenaManager().getFirst().getState().getPathName()));
        }
    }
}
