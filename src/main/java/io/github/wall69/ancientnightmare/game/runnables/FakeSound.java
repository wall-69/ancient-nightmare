package io.github.wall69.ancientnightmare.game.runnables;

import io.github.wall69.ancientnightmare.Main;
import io.github.wall69.ancientnightmare.arena.Arena;
import io.github.wall69.ancientnightmare.arena.ArenaState;
import io.github.wall69.ancientnightmare.game.Game;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.Vibration;
import org.bukkit.Vibration.Destination.BlockDestination;
import org.bukkit.entity.Villager;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class FakeSound extends BukkitRunnable {

    private final Main main;
    private final Arena arena;
    private final Game game;

    private final Villager villager;
    private int seconds;

    public FakeSound(Main main, Arena arena, UUID villager) {
        this.main = main;
        this.arena = arena;
        this.game = arena.getGame();

        this.villager = (Villager) Bukkit.getEntity(villager);
        this.seconds = 7;
    }

    public void start() {
        runTaskTimer(main, 10L, 20L);
    }

    public void run() {
        if (arena.getState() != ArenaState.PLAYING || villager == null || villager.isDead()) {
            if(villager != null && !villager.isDead()) {
                villager.remove();
            }

            cancel();
            return;
        }
        if (seconds == 0) {
            villager.getWorld().playSound(villager.getLocation(), Sound.ENTITY_VILLAGER_DEATH, 5.0f, 1.0f);
            villager.remove();
            cancel();
            return;
        }

        Bukkit.getPlayer(game.getWarden()).spawnParticle(Particle.VIBRATION, villager.getLocation(),
                1, new Vibration(villager.getLocation(),
                        new BlockDestination(villager.getLocation().clone().add(0, 1.8, 0)), 50));
        villager.getWorld().playSound(villager.getLocation(), Sound.BLOCK_SCULK_SENSOR_STEP, 5.0f, 1.0f);

        seconds--;
    }

}
