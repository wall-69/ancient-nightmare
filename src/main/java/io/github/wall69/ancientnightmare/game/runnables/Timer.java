package io.github.wall69.ancientnightmare.game.runnables;

import io.github.wall69.ancientnightmare.Main;
import io.github.wall69.ancientnightmare.arena.Arena;
import io.github.wall69.ancientnightmare.arena.ArenaState;
import io.github.wall69.ancientnightmare.game.Game;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class Timer extends BukkitRunnable {

    private final Main main;
    private final Arena arena;
    private final Game game;

    private final Random random;

    private int timerSeconds, caveSoundSeconds;

    public Timer(Main main, Arena arena) {
        this.main = main;
        this.arena = arena;
        this.game = arena.getGame();

        this.random = new Random();

        this.timerSeconds = arena.getGameTime();
        this.caveSoundSeconds = random.nextInt(20) + 10;
    }

    public void start() {
        runTaskTimer(main, 0L, 20L);
    }

    public void run() {
        if (arena.getState() != ArenaState.PLAYING) {
            cancel();
            return;
        }

        if (timerSeconds == 0) {
            game.gameWin("security");
            cancel();
            return;
        }

        if(caveSoundSeconds == 0){
            arena.playSound(Sound.AMBIENT_CAVE);
            this.caveSoundSeconds = random.nextInt(20) + 22;
        }

        if (timerSeconds <= 15 || timerSeconds == 30 || timerSeconds % 60 == 0) {
            arena.sendMessage(main.getFileUtils().getString("arena.game.timer-syntax")
                    .replace("{timer}", String.valueOf(timerSeconds)));
            arena.playSound(Sound.BLOCK_NOTE_BLOCK_BASS);
        }

        timerSeconds--;
        caveSoundSeconds--;
    }

}
