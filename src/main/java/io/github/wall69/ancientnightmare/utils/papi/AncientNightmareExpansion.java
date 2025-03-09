package io.github.wall69.ancientnightmare.utils.papi;

import io.github.wall69.ancientnightmare.Main;
import io.github.wall69.ancientnightmare.utils.PlayerStatistic;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class AncientNightmareExpansion extends PlaceholderExpansion {

    private final Main main;

    public AncientNightmareExpansion(Main main){
        this.main = main;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "ancientnightmare";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Wall_";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        for(PlayerStatistic statistic : PlayerStatistic.values()){
            if(params.equalsIgnoreCase(statistic.getName())){
                return String.valueOf(main.getFileUtils().getPlayerStatistic(player.getUniqueId(), statistic));
            }
        }

        return null;
    }
}
