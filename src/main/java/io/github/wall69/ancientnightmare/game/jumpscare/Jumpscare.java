package io.github.wall69.ancientnightmare.game.jumpscare;

import io.github.wall69.ancientnightmare.Main;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;
import java.util.Random;

public abstract class Jumpscare {

    protected final Random random = new Random();

    public final void play(Main main, Player player) {
        if (!player.hasMetadata("an_jumpscared")) {
            jumpscare(main, player, new BukkitRunnable() {
                @Override
                public void run() {
                    player.removeMetadata("an_jumpscared", main);
                }
            });

            player.setMetadata("an_jumpscared", new FixedMetadataValue(main, "dummy"));
        }
    }

    protected abstract void jumpscare(Main main, Player player, BukkitRunnable callback);
}