package io.github.wall69.ancientnightmare.game.runnables;

import io.github.wall69.ancientnightmare.Main;
import io.github.wall69.ancientnightmare.arena.Arena;
import io.github.wall69.ancientnightmare.arena.ArenaState;
import io.github.wall69.ancientnightmare.game.Game;
import io.github.wall69.ancientnightmare.game.SpecialAbilityItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class Cooldown extends BukkitRunnable {

    private final Main main;
    private final Arena arena;
    private final Game game;

    private int cooldownSeconds;
    private final UUID player;
    private final String localizedName;

    public Cooldown(Main main, Arena arena, int cooldownSeconds, UUID player, String localizedName) {
        this.main = main;
        this.arena = arena;
        this.game = arena.getGame();

        this.cooldownSeconds = cooldownSeconds;
        this.player = player;
        this.localizedName = localizedName;
    }

    public void start() {
        runTaskTimer(main, 0L,20L);
    }

    public void run() {
        if (arena.getState() != ArenaState.PLAYING) {
            cancel();
            return;
        }

        if(cooldownSeconds == 0){
            SpecialAbilityItem item = null;
            for(SpecialAbilityItem specialAbilityItem : SpecialAbilityItem.values()){
                if(specialAbilityItem.getLocalizedName().equals(localizedName))
                    item = specialAbilityItem;
            }

            Bukkit.getPlayer(player).sendMessage(main.prefix +
                    main.getFileUtils().getString("items.game.item-no-longer-on-cooldown")
                    .replace("{item}",
                            main.getItemUtils().getSecurityAbilityItem(item).getType() == Material.BARRIER ?
                            main.getItemUtils().getWardenAbilityItem(item).getItemMeta().getDisplayName() :
                            main.getItemUtils().getSecurityAbilityItem(item).getItemMeta().getDisplayName()));
            game.removeItemCooldown(player, localizedName);
            cancel();
            return;
        }

        cooldownSeconds--;
    }

}
