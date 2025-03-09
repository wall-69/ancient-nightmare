package io.github.wall69.ancientnightmare.arena;

import java.util.*;
import java.util.logging.Level;

import io.github.wall69.ancientnightmare.Main;
import io.github.wall69.ancientnightmare.game.Game;
import io.github.wall69.ancientnightmare.utils.Cuboid;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.skinsrestorer.api.exception.DataRequestException;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;
import org.bukkit.scoreboard.Team.Option;

public class Arena {

    private final Main main;
    private final String name;
    private final Location lobbySpawn;

    private ArenaState state;
    private final List<UUID> players;
    private Countdown countdown;
    private Game game;

    private final int gameTime;
    private final Cuboid arenaRegion, exitRegion;
    private final List<Location> doors;
    private final Location generatorLocation, batterySupplyLocation;

    private final HashMap<UUID, ItemStack[]> savedInventory;
    private final HashMap<UUID, ItemStack[]> savedArmor;

    public Arena(Main main, String name, Location spawn) {
        this.main = main;
        this.name = name;
        this.lobbySpawn = spawn;

        this.state = ArenaState.WAITING;
        this.players = new ArrayList<>();
        this.countdown = new Countdown(main, this);
        this.game = new Game(main, this);

        this.gameTime = main.getFileUtils().getArenaGameTime(name);
        this.arenaRegion = new Cuboid(
                main.getFileUtils().getArenaLocation(name, ArenaLocation.ARENA_REGION_POS_1),
                main.getFileUtils().getArenaLocation(name, ArenaLocation.ARENA_REGION_POS_2));
        this.exitRegion = new Cuboid(
                main.getFileUtils().getArenaLocation(name, ArenaLocation.EXIT_REGION_POS_1),
                main.getFileUtils().getArenaLocation(name, ArenaLocation.EXIT_REGION_POS_2));
        this.doors = getDoorsLocation();
        this.generatorLocation = main.getFileUtils().getArenaLocation(name, ArenaLocation.GENERATOR_BLOCK);
        this.batterySupplyLocation = main.getFileUtils().getArenaLocation(name, ArenaLocation.BATTERY_SUPPLY_BLOCK);

        this.savedInventory = new HashMap<>();
        this.savedArmor = new HashMap<>();
    }

    /*
     * GAME
     */

    public void start() {
        game.start();
    }

    public void reset(boolean kickPlayers) {
        if (kickPlayers) {
            if (players != null && players.size() != 0) {
                for (UUID uuid : players) {
                    Player p = Bukkit.getPlayer(uuid);

                    resetPlayer(p);

                    // Play again
                    if (main.getConfig().getBoolean("arena.play-again-message")) {
                        TextComponent playAgainText = new TextComponent(main.prefix +
                                main.getFileUtils().getString("arena.game.play-again"));
                        playAgainText.addExtra(" ");

                        TextComponent playAgainClickableText = new TextComponent(main.getFileUtils()
                                .getString("arena.game.play-again-clickable"));
                        playAgainClickableText.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                "/an join " + this.name));

                        playAgainText.addExtra(playAgainClickableText);

                        p.spigot().sendMessage(playAgainText);
                    }
                }

                players.clear();
            }
        }

        state = ArenaState.WAITING;
        if (countdown.hasStarted()) {
            countdown.cancel();
        }
        countdown = new Countdown(main, this);
        game = new Game(main, this);
    }

    public void addPlayer(Player p) {
        players.add(p.getUniqueId());

        savedInventory.put(p.getUniqueId(), p.getInventory().getContents());
        savedArmor.put(p.getUniqueId(), p.getInventory().getArmorContents());

        p.teleport(lobbySpawn);
        p.setGameMode(GameMode.ADVENTURE);
        p.getInventory().clear();
        p.getActivePotionEffects().forEach(effect -> p.removePotionEffect(effect.getType()));

        // Scoreboard
        if (main.getFileUtils().useLobbyScoreboard()) {
            p.setScoreboard(getLobbyScoreboard(p.getUniqueId()));
        } else {
            p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        }

        main.getItemUtils().giveLobbyItems(p);

        Team nametagHideTeam = p.getScoreboard().getTeam("nhide");
        if (nametagHideTeam == null) {
            nametagHideTeam = p.getScoreboard().registerNewTeam("nhide");
            nametagHideTeam.setOption(Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
            nametagHideTeam.setDisplayName("");
        }

        nametagHideTeam.addEntry(p.getName());

        if (state == ArenaState.WAITING && players.size() == 2) {
            countdown.start();
        }

        // Scoreboard update
        if (main.getFileUtils().useLobbyScoreboard()) {
            for (UUID uuid : players) {
                updateScoreboard(uuid);
            }
        }
    }

    public void removePlayer(Player p) {
        players.remove(p.getUniqueId());

        if (players.size() < 2) {
            switch (state) {
                case COUNTDOWN:
                    sendMessage(main.getFileUtils().getString("arena.countdown.not-enough-players"));
                    reset(false);

                    // Scoreboard update
                    if (main.getFileUtils().useLobbyScoreboard()) {
                        for (UUID uuid : players) {
                            updateScoreboard(uuid);
                        }
                    }
                    break;

                case PLAYING:
                    sendMessage(main.getFileUtils().getString("arena.game.not-enough-players"));
                    game.gameWin(game.getRole(p.getUniqueId()).equals("warden") ? "security" : "warden");
                    break;
            }
        }

        resetPlayer(p);
    }

    public void resetPlayer(Player p) {
        if (this.main.getFileUtils().isProxy()) {
            p.kickPlayer(this.main.getFileUtils().getString("commands.player.leave"));
        } else {
            p.teleport(this.main.getFileUtils().getGlobalLobbyLocation());
        }

        p.sendTitle("", "", 0, 0, 0);

        p.getActivePotionEffects().forEach(effect -> p.removePotionEffect(effect.getType()));
        p.setFoodLevel(20);
        p.setHealth(20);
        p.setGameMode(GameMode.SURVIVAL);
        p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());

        p.getInventory().clear();
        p.getInventory().setContents(savedInventory.get(p.getUniqueId()));
        p.getInventory().setArmorContents(savedArmor.get(p.getUniqueId()));
        savedInventory.remove(p.getUniqueId());
        savedArmor.remove(p.getUniqueId());

        p.setWalkSpeed(0.2f);
        p.setFlySpeed(0.1f);

        Team nametagHideTeam = p.getScoreboard().getTeam("nhide");
        if (nametagHideTeam != null) {
            nametagHideTeam.removeEntry(p.getName());
        }

        p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());


        if (p.getUniqueId().equals(game.getWarden())) {
            game.setWarden(null);
        } else if (p.getUniqueId().equals(game.getSecurity())) {
            game.setSecurity(null);
        }

        if (game.getBatteryBar() != null) {
            game.getBatteryBar().removePlayer(p);
        }

        game.removeFromLists(p.getUniqueId());

        // Skin
        if (main.getFileUtils().useSkins() && main.getSkinsRestorerAPI() != null) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        main.getSkinUtils().clearSkin(p);
                    } catch (DataRequestException e) {
                        Bukkit.getLogger().log(Level.SEVERE, main.consolePrefix + "Failed to reset skin for player "
                                + p.getName() + ": " + e.getMessage());
                    }
                }
            }.runTaskAsynchronously(main);
        }
    }

    public void sendMessage(String message) {
        for (UUID uuid : players) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                p.sendMessage(main.prefix + message);
            }
        }
    }

    public void sendTitle(String title, String subtitle) {
        for (UUID uuid : players) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                p.sendTitle(title, subtitle, 20, 80, 20);
            }
        }
    }

    public void playSound(Sound sound) {
        for (UUID uuid : players) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                p.playSound(p, sound, 0.5f, 1.0f);
            }
        }
    }

    public void freezePlayers() {
        for (UUID uuid : players) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                p.setWalkSpeed(0.0f);
                p.setFlySpeed(0.0f);
            }
        }
    }

    public void updateScoreboard(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);

        for (Team team : player.getScoreboard().getTeams()) {
            player.getScoreboard().getTeam(team.getName()).setPrefix(team.getDisplayName()
                    .replace("{arena_state}", main.getFileUtils().getString("arena-state." + state.getPathName()))
                    .replace("{players}", String.valueOf(players.size()))
                    .replace("{countdown}", String.valueOf(countdown.getCountdownSeconds()))
                    .replace("{prefix}", main.prefix)
                    .replace("{arena}", name)
            );
        }
    }

    /*
     * GETTERS
     */

    public String getName() {
        return name;
    }

    public ArenaState getState() {
        return state;
    }

    public List<UUID> getPlayers() {
        return players;
    }

    public Game getGame() {
        return game;
    }

    public int getGameTime() {
        return gameTime;
    }

    public Location getWardenSpawn() {
        return main.getFileUtils().getArenaLocation(name, ArenaLocation.WARDEN_SPAWN);
    }

    public Location getSecuritySpawn() {
        return main.getFileUtils().getArenaLocation(name, ArenaLocation.SECURITY_SPAWN);
    }

    public List<Location> getDoors() {
        return doors;
    }

    public Location getGeneratorLocation() {
        return generatorLocation;
    }

    public Location getBatterySupplyLocation() {
        return batterySupplyLocation;
    }

    public Cuboid getArenaRegion() {
        return arenaRegion;
    }

    public Cuboid getExitRegion() {
        return exitRegion;
    }

    private List<Location> getDoorsLocation() {
        return main.getFileUtils().getArenaDoors(name);
    }

    private Scoreboard getLobbyScoreboard(UUID uuid) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

        Objective objective = scoreboard.registerNewObjective("ancientnightmare", "H");
        objective.setDisplayName(main.getFileUtils().getString("lobby-scoreboard.title")
                .replace("{arena_state}", main.getFileUtils().getString("arena-state." + state.getPathName()))
                .replace("{players}", String.valueOf(players.size()))
                .replace("{countdown}", String.valueOf(countdown.getCountdownSeconds()))
                .replace("{prefix}", main.prefix)
                .replace("{arena}", name));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        List<String> lines = main.getFileUtils().getStringList("lobby-scoreboard.lines");
        Collections.reverse(lines);
        int usedBlankScores = 0, usedTeams = 0;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);

            if (line.isEmpty()) {
                usedBlankScores++;

                String blankScore = "";
                for (int j = 0; j < usedBlankScores; j++) {
                    blankScore = blankScore.concat(" ");
                }

                objective.getScore(blankScore).setScore(i);
            } else {
                if (!line.contains("{arena_state}") && !line.contains("{players}") && !line.contains("{countdown}")) {
                    objective.getScore(line
                            .replace("{prefix}", main.prefix)
                            .replace("{arena}", name)
                    ).setScore(i);
                } else {
                    usedTeams++;

                    Team team = scoreboard.registerNewTeam("team" + usedTeams);
                    team.addEntry(ChatColor.values()[usedTeams].toString());
                    team.setDisplayName(line);
                    team.setPrefix(line
                            .replace("{arena_state}", main.getFileUtils().getString("arena-state." + state.getPathName()))
                            .replace("{players}", String.valueOf(players.size()))
                            .replace("{countdown}", String.valueOf(countdown.getCountdownSeconds()))
                            .replace("{prefix}", main.prefix)
                            .replace("{arena}", name)
                    );

                    objective.getScore(ChatColor.values()[usedTeams].toString()).setScore(i);
                }
            }
        }

        return scoreboard;
    }

    /*
     * SETTERS
     */

    public void setState(ArenaState state) {
        this.state = state;
    }

}
