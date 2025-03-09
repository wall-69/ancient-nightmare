package io.github.wall69.ancientnightmare;

import io.github.wall69.ancientnightmare.arena.Arena;
import io.github.wall69.ancientnightmare.commands.AncientNightmareCommand;
import io.github.wall69.ancientnightmare.commands.AncientNightmareTab;
import io.github.wall69.ancientnightmare.game.GameBlock;
import io.github.wall69.ancientnightmare.listeners.GameListener;
import io.github.wall69.ancientnightmare.listeners.JoinQuitListener;
import io.github.wall69.ancientnightmare.listeners.LobbyListener;
import io.github.wall69.ancientnightmare.managers.ArenaManager;
import io.github.wall69.ancientnightmare.managers.FileManager;
import io.github.wall69.ancientnightmare.queue.Queue;
import io.github.wall69.ancientnightmare.utils.FileUtils;
import io.github.wall69.ancientnightmare.utils.ItemUtils;
import io.github.wall69.ancientnightmare.utils.SkinUtils;
import io.github.wall69.ancientnightmare.utils.papi.AncientNightmareExpansion;
import net.skinsrestorer.api.SkinsRestorer;
import net.skinsrestorer.api.SkinsRestorerProvider;
import net.skinsrestorer.api.VersionProvider;
import net.skinsrestorer.api.exception.DataRequestException;
import net.skinsrestorer.api.exception.MineSkinException;
import net.skinsrestorer.api.property.SkinProperty;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

public final class Main extends JavaPlugin {

    private ArenaManager arenaManager;
    private FileManager fileManager;
    private Queue queue;

    private FileUtils fileUtils;
    private ItemUtils itemUtils;
    private SkinUtils skinUtils;

    public String prefix = "", consolePrefix = "[AncientNightmare] ";

    private SkinsRestorer skinsRestorerAPI;

    public void onEnable() {
        this.fileManager = new FileManager(this);
        this.itemUtils = new ItemUtils(this);
        this.fileUtils = new FileUtils(this, fileManager);
        this.arenaManager = new ArenaManager(this);
        this.queue = new Queue(this);

        this.prefix = fileUtils.getString("prefix") + " ";

        this.registerCommands();
        this.registerListeners();

        // Game blocks
        for (GameBlock gameBlock : GameBlock.values()) {
            gameBlock.setType(this.fileUtils.getBlockType(gameBlock));
        }

        // Custom skins
        if (this.fileUtils.useSkins()) {
            if (Bukkit.getPluginManager().getPlugin("SkinsRestorer") != null) {
                this.skinsRestorerAPI = SkinsRestorerProvider.get();
                try {
                    this.skinUtils = new SkinUtils(this, skinsRestorerAPI);
                } catch (Exception e){
                    e.printStackTrace();
                }
                Bukkit.getLogger().log(Level.INFO, consolePrefix + "SkinsRestorer found!");
            } else {
                this.skinsRestorerAPI = null;
                Bukkit.getLogger().log(Level.WARNING, consolePrefix + "Use-skins is enabled in config.yml, but " +
                        "SkinsRestorer wasn't found! (due to this use-skins was set to false)");
                this.fileUtils.setUseSkins(false);
            }
        }

        // bStats
        Metrics metrics = new Metrics(this, 15766);
        if (metrics.isEnabled()) {
            metrics.addCustomChart(new Metrics.SimplePie(
                    "use_skins", () -> String.valueOf(this.fileUtils.useSkins())));
            metrics.addCustomChart(new Metrics.SimplePie(
                    "arenas_amount", () -> String.valueOf(this.fileUtils.getArenas().size())));

            Bukkit.getLogger().log(Level.INFO, consolePrefix + "This plugin uses bStats to collect anonymous data," +
                    " if you wish to disable it, you can do it in bStats/config.yml.");
        }

        // Placeholders
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new AncientNightmareExpansion(this).register();
        }

        // Updates check
        if (this.fileUtils.checkUpdates()) {
            this.checkUpdates();
        }
    }

    @Override
    public void onDisable() {
        // Reset arenas
        if (this.arenaManager.getArenas() != null && this.arenaManager.getArenas().size() > 0) {
            for (Arena arena : this.arenaManager.getArenas()) {
                arena.reset(true);
            }
        }

        // Remove players from queue
        if (this.queue.getCurrentPlayers() != null && this.queue.getCurrentPlayers().size() > 0) {
            for (UUID uuid : this.queue.getCurrentPlayers()) {
                Bukkit.getPlayer(uuid).sendMessage(this.prefix + this.fileUtils.getString("commands.player.queue-leave"));
                this.queue.removePlayer(uuid);
            }
        }
    }

    private void registerCommands() {
        getCommand("ancientnightmare").setExecutor(new AncientNightmareCommand(this));
        getCommand("ancientnightmare").setTabCompleter(new AncientNightmareTab());

        Bukkit.getLogger().log(Level.INFO, consolePrefix + "Commands registered!");
    }

    private void registerListeners() {
        PluginManager pm = Bukkit.getServer().getPluginManager();

        pm.registerEvents(new JoinQuitListener(this), this);
        pm.registerEvents(new GameListener(this), this);
        pm.registerEvents(new LobbyListener(this), this);

        Bukkit.getLogger().log(Level.INFO, consolePrefix + "Listeners registered!");
    }

    private void checkUpdates() {
        UpdateChecker updateChecker = new UpdateChecker(this, 104076);
        String latestVersion = updateChecker.getLatestVersionString();
        String currentVersion = updateChecker.getCurrentVersionString();

        switch (updateChecker.getUpdateCheckResult()) {
            case OUT_DATED:
                Bukkit.getLogger().log(Level.WARNING,
                        consolePrefix + "You are running old version of AncientNightmare!" +
                                " (Latest: " + latestVersion + ", Current: " + currentVersion + ")");
                break;
            case UP_TO_DATE:
                Bukkit.getLogger().log(Level.INFO,
                        consolePrefix + "You are running the newest version of AncientNightmare! That's cool!");
                break;
            case NO_RESULT:
                Bukkit.getLogger().log(Level.INFO,
                        consolePrefix + "Couldn't check for newest version :( (But it's okay!)");
                break;
            case UNRELEASED:
                Bukkit.getLogger().log(Level.INFO,
                        consolePrefix + "You are running unreleased version of AncientNightmare! Really cool!");
                break;
        }
    }

    /*
        GETTERS
     */

    public ArenaManager getArenaManager() {
        return arenaManager;
    }

    public Queue getQueue() {
        return queue;
    }

    public FileUtils getFileUtils() {
        return fileUtils;
    }

    public ItemUtils getItemUtils() {
        return itemUtils;
    }

    public SkinUtils getSkinUtils() {
        return skinUtils;
    }

    public SkinsRestorer getSkinsRestorerAPI() {
        return skinsRestorerAPI;
    }

}