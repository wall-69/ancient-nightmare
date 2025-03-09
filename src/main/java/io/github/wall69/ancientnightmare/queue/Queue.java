package io.github.wall69.ancientnightmare.queue;

import io.github.wall69.ancientnightmare.Main;
import io.github.wall69.ancientnightmare.arena.Arena;
import io.github.wall69.ancientnightmare.arena.ArenaState;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class Queue {

    private final List<UUID> currentPlayers;

    public Queue(Main main) {
        this.currentPlayers = new ArrayList<>();

        new BukkitRunnable() {
            @Override
            public void run() {
                for (UUID uuid : currentPlayers) {
                    Player player = Bukkit.getPlayer(uuid);

                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                            new TextComponent(main.getFileUtils().getString("queue.current-position")
                                    .replace("{position}", String.valueOf(currentPlayers.indexOf(uuid) + 1))));
                }

                if (currentPlayers.size() == 0)
                    return;

                List<Arena> arenas = main.getArenaManager().getArenas();
                Collections.shuffle(arenas);

                for (Arena arena : arenas) {
                    List<UUID> queued = new ArrayList<>();

                    if (arena.getState() != ArenaState.WAITING)
                        continue;

                    if (arena.getPlayers().size() == 1) {
                        queued.add(currentPlayers.get(0));

                        arena.addPlayer(Bukkit.getPlayer(queued.get(0)));
                        removePlayer(queued.get(0));
                    } else if (arena.getPlayers().size() == 0 && currentPlayers.size() >= 2) {
                        queued.add(currentPlayers.get(0));
                        queued.add(currentPlayers.get(1));

                        for (int i = 0; i < 2; i++) {
                            arena.addPlayer(Bukkit.getPlayer(queued.get(i)));
                            removePlayer(queued.get(i));
                        }

                    }

                    queued.clear();
                }
            }
        }.runTaskTimer(main, 0L, 20L);
    }

    public void addPlayer(UUID uuid) {
        currentPlayers.add(uuid);
    }

    public void removePlayer(UUID uuid) {
        currentPlayers.remove(uuid);
    }

    /*
     *  GETTERS
     */

    public List<UUID> getCurrentPlayers() {
        return currentPlayers;
    }

}