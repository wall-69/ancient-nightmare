package io.github.wall69.ancientnightmare.game.runnables;

import io.github.wall69.ancientnightmare.Main;
import io.github.wall69.ancientnightmare.arena.Arena;
import io.github.wall69.ancientnightmare.arena.ArenaState;
import io.github.wall69.ancientnightmare.game.Game;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class SecurityExitCheck extends BukkitRunnable {

    private final Main main;
    private final Arena arena;
    private final Game game;

    private final UUID player;

    public SecurityExitCheck(Main main, Arena arena, UUID player) {
        this.main = main;
        this.arena = arena;
        this.game = arena.getGame();

        this.player = player;
    }

    public void start() {
        runTaskTimer(main, 0L, 10L);
    }

    public void run() {
        if (arena.getState() != ArenaState.PLAYING) {
            cancel();
            return;
        }

        if(arena.getExitRegion().isIn(Bukkit.getPlayer(player).getLocation())){
            game.gameWin("security");
            cancel();
        }
    }

}
