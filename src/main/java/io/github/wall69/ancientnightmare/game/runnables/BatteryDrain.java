package io.github.wall69.ancientnightmare.game.runnables;

import io.github.wall69.ancientnightmare.Main;
import io.github.wall69.ancientnightmare.arena.Arena;
import io.github.wall69.ancientnightmare.arena.ArenaState;
import io.github.wall69.ancientnightmare.game.Game;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.TrapDoor;
import org.bukkit.boss.BossBar;
import org.bukkit.scheduler.BukkitRunnable;

public class BatteryDrain extends BukkitRunnable {

    private final Main main;
    private final Game game;
    private final Arena arena;

    private final BossBar batteryBar;

    public BatteryDrain(Main main, Arena arena, BossBar batteryBar) {
        this.main = main;
        this.arena = arena;
        this.game = arena.getGame();

        this.batteryBar = batteryBar;
    }

    public void start() {
        runTaskTimer(main, 0L, 40L);
    }

    public void run() {
        if (arena.getState() != ArenaState.PLAYING) {
            cancel();
            return;
        }

        game.setBattery(game.getNextBattery());

        if (game.getBattery() == 0) {
            batteryBar.setTitle(main.getFileUtils().getString("arena.game.battery-boss-bar").replace("{battery}",
                    String.valueOf(game.getBattery())));

            for (Location l : arena.getDoors()) {
                Block b = l.getWorld().getBlockAt(l.getBlockX(), l.getBlockY(), l.getBlockZ());

                if (b.getBlockData() instanceof Door) {
                    Door door = (Door) b.getBlockData();
                    if (door.isOpen())
                        continue;

                    door.setOpen(true);
                    b.setBlockData(door);
                    b.getWorld().playSound(b.getLocation(), Sound.BLOCK_IRON_DOOR_OPEN, 1.0f, 1.0f);

                    game.setClosedDoors(game.getClosedDoors() - 1);
                } else if (b.getBlockData() instanceof TrapDoor) {
                    TrapDoor trapdoor = (TrapDoor) b.getBlockData();
                    if (trapdoor.isOpen())
                        continue;

                    trapdoor.setOpen(true);
                    b.setBlockData(trapdoor);
                    b.getWorld().playSound(b.getLocation(), Sound.BLOCK_IRON_TRAPDOOR_OPEN, 1.0f, 1.0f);

                    game.setClosedDoors(game.getClosedDoors() - 1);
                }
            }

            return;
        }

        batteryBar.setTitle(main.getFileUtils().getString("arena.game.battery-boss-bar").replace("{battery}",
                String.valueOf(game.getBattery())));
        batteryBar.setProgress((double) game.getBattery() / 100);

        int diff = game.getClosedDoors();

        game.setNextBattery(Math.max(game.getNextBattery() - diff, 0));
    }

}
