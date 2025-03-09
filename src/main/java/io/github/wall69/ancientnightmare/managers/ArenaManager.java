package io.github.wall69.ancientnightmare.managers;

import java.util.ArrayList;
import java.util.List;

import io.github.wall69.ancientnightmare.Main;
import io.github.wall69.ancientnightmare.arena.Arena;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ArenaManager {

    private final Main main;

    private List<Arena> arenas;

    public ArenaManager(Main main) {
        this.main = main;

        this.updateArenas();
    }

    public Arena getArena(Player p) {
        for (Arena arena : arenas) {
            if (arena.getPlayers().contains(p.getUniqueId())) {
                return arena;
            }
        }

        return null;
    }

    public Arena getArena(String name) {
        for (Arena arena : arenas) {
            if (arena.getName().equals(name)) {
                return arena;
            }
        }

        return null;
    }

    public Arena getFirst(){
        return !arenas.isEmpty() ? arenas.get(0) : null;
    }

    public List<Arena> getArenas() {
        if (this.arenas == null)
            return new ArrayList<>();

        return this.arenas;
    }

    public void updateArenas() {
        if (main.getFileUtils().getArenas() == null) {
            this.arenas = new ArrayList<>();
            return;
        }

        this.arenas = main.getFileUtils().getArenas();
    }

    public void deleteArena(String arenaName) {
        Arena arena = getArena(arenaName);

        this.arenas.remove(arena);
        main.getFileUtils().deleteArena(arenaName);
    }

}
