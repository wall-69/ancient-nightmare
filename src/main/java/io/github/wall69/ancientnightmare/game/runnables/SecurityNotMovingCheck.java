package io.github.wall69.ancientnightmare.game.runnables;

import io.github.wall69.ancientnightmare.Main;
import io.github.wall69.ancientnightmare.arena.Arena;
import io.github.wall69.ancientnightmare.arena.ArenaState;
import io.github.wall69.ancientnightmare.game.Game;
import org.bukkit.*;
import org.bukkit.Vibration.Destination.BlockDestination;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class SecurityNotMovingCheck extends BukkitRunnable {

    private final Main main;
    private final Arena arena;
    private final Game game;

    private final UUID player;

    public SecurityNotMovingCheck(Main main, Arena arena, UUID player) {
        this.main = main;
        this.arena = arena;
        this.game = arena.getGame();

        this.player = player;
    }

    public void start() {
        runTaskTimer(main, 0L, 20L);
    }

    public void run() {
        if (arena.getState() != ArenaState.PLAYING) {
            cancel();
            return;
        }

        if (System.currentTimeMillis() - game.getLastSound(game.getSecurity()) > 4000
                && System.currentTimeMillis() - game.getLastMovement(game.getSecurity()) > 15000) {
            Player p = Bukkit.getPlayer(player);
            Location pLoc = p.getLocation();

            Bukkit.getPlayer(game.getWarden()).getWorld().spawnParticle(Particle.VIBRATION, pLoc, 1,
                    new Vibration(pLoc, new BlockDestination(p.getLocation().clone().add(0, 1.8, 0)), 50));

            p.getWorld().playSound(pLoc, Sound.BLOCK_SCULK_SENSOR_STEP, 5.0f, 1.0f);
            p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 5 * 20, 0, false, false));

            game.setLastMovement(game.getSecurity());
            game.setLastSound(game.getSecurity());
        }
    }

}
