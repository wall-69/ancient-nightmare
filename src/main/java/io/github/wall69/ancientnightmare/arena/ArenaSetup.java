package io.github.wall69.ancientnightmare.arena;

import io.github.wall69.ancientnightmare.Main;
import io.github.wall69.ancientnightmare.arena.ArenaLocation;
import io.github.wall69.ancientnightmare.utils.Cuboid;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class ArenaSetup implements Listener {

    private final Main main;

    private final Player player;
    private ArenaLocation arenaLocation;
    private final String arenaName;

    private final ItemStack[] savedInventory, savedArmor;

    public ArenaSetup(Main main, UUID playerUUID, String arenaName) {
        this.main = main;

        this.player = Bukkit.getPlayer(playerUUID);
        this.arenaLocation = ArenaLocation.LOBBY;
        this.arenaName = arenaName;

        this.savedInventory = player.getInventory().getContents();
        this.savedArmor = player.getInventory().getArmorContents();
    }

    public void start() {
        player.sendMessage(main.prefix + "§cSetup of arena named " + arenaName + " has started!");
        player.sendMessage(main.prefix + "§4§lPlease read everything carefully!");

        player.getInventory().clear();
        player.setGameMode(GameMode.CREATIVE);

        giveItem();
    }

    public void stop(boolean disconnected) {
        HandlerList.unregisterAll(this);

        player.getInventory().clear();

        player.getInventory().setContents(savedInventory);
        player.getInventory().setArmorContents(savedArmor);

        if (!disconnected) {
            player.sendMessage(main.prefix + "§cThe arena setup has finished! Enjoy!");

            main.getFileUtils().setDoors(arenaName, new Cuboid(
                    main.getFileUtils().getArenaLocation(arenaName, ArenaLocation.ARENA_REGION_POS_1),
                    main.getFileUtils().getArenaLocation(arenaName, ArenaLocation.ARENA_REGION_POS_2)
            ).doorList());
        } else {
            main.getArenaManager().deleteArena(arenaName);
        }

        main.getArenaManager().updateArenas();
    }

    private void giveItem() {
        player.getInventory().clear();
        player.getInventory().addItem(main.getItemUtils().getSetupItem(arenaLocation, arenaName));

        if (arenaLocation != ArenaLocation.LOBBY) {
            for (int i = 0; i < 10; i++) {
                player.sendMessage(" ");
            }
        }

        player.sendMessage(main.prefix + "§c" + arenaLocation.getMessage());
    }

    private void setLocation(Location location) {
        main.getFileUtils().setArenaLocation(arenaName, arenaLocation, location);

        if (arenaLocation.ordinal() + 1 >= ArenaLocation.values().length) {
            stop(false);
            return;
        }

        this.arenaLocation = ArenaLocation.values()[arenaLocation.ordinal() + 1];

        giveItem();
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();

        if (p.getUniqueId() != player.getUniqueId()
                || e.getHand() != EquipmentSlot.HAND
                || !e.hasItem()
                || e.getAction() != Action.RIGHT_CLICK_BLOCK
                || e.getClickedBlock() == null)
            return;

        ItemStack itemInHand = e.getItem();
        if (itemInHand.getItemMeta() == null)
            return;

        if (itemInHand.getItemMeta().getLocalizedName().equals("an_setupBarrelBlock") ||
                itemInHand.getItemMeta().getLocalizedName().equals("an_setupGeneratorBlock")) {
            setLocation(e.getClickedBlock().getLocation());
            e.setCancelled(true);
        }

    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Player p = e.getPlayer();

        if (p.getUniqueId() != player.getUniqueId())
            return;

        ItemStack itemInHand = p.getEquipment().getItemInMainHand();

        if (itemInHand == null || itemInHand.getItemMeta() == null)
            return;

        Block block = e.getBlockPlaced();

        List<String> names = Arrays.asList("an_setupLobby", "an_setupArenaRegion",
                "an_setupExitRegion", "an_setupWardenSpawn", "an_setupSecuritySpawn");

        if (names.contains(itemInHand.getItemMeta().getLocalizedName())) {
            setLocation(block.getLocation());
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        if (p.getUniqueId() == player.getUniqueId()) {
            Bukkit.getLogger().log(Level.WARNING, main.consolePrefix + "Arena setup of arena " + arenaName +
                    " was cancelled due to disconnection of player.");
            stop(true);
        }
    }

}
