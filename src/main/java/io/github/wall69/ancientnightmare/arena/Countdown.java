package io.github.wall69.ancientnightmare.arena;

import io.github.wall69.ancientnightmare.Main;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class Countdown extends BukkitRunnable {

    private final Main main;
    private final Arena arena;

    private int countdownSeconds;
    private boolean started = false;

    public Countdown(Main main, Arena arena) {
        this.main = main;
        this.arena = arena;

        this.countdownSeconds = main.getFileUtils().getCountdownSeconds();
    }

    public void start() {
        arena.setState(ArenaState.COUNTDOWN);
        runTaskTimer(main, 0L, 20L);
        started = true;
    }

    public void run() {
        if (countdownSeconds == 0) {
            arena.start();
            cancel();
            return;
        }

        if (countdownSeconds <= 10 || countdownSeconds % 15 == 0) {
            arena.sendMessage(main.getFileUtils().getString("arena.countdown.syntax")
                    .replace("{countdown}", String.valueOf(countdownSeconds)));
        }

        // Scoreboard update
        if(main.getFileUtils().useLobbyScoreboard()) {
            for (UUID uuid : arena.getPlayers()) {
                arena.updateScoreboard(uuid);
            }
        }

        countdownSeconds--;
    }

    public int getCountdownSeconds() {
        return countdownSeconds;
    }

    public boolean hasStarted(){
        return started;
    }
}