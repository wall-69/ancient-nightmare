package io.github.wall69.ancientnightmare.game;

import java.net.MalformedURLException;
import java.util.*;
import java.util.logging.Level;

import io.github.wall69.ancientnightmare.Main;
import io.github.wall69.ancientnightmare.arena.Arena;
import io.github.wall69.ancientnightmare.arena.ArenaLocation;
import io.github.wall69.ancientnightmare.arena.ArenaState;
import io.github.wall69.ancientnightmare.game.runnables.*;
import io.github.wall69.ancientnightmare.game.runnables.Timer;
import io.github.wall69.ancientnightmare.utils.PlayerStatistic;
import net.skinsrestorer.api.exception.DataRequestException;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.TrapDoor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

public class Game {

    private final Main main;
    private final Arena arena;

    private UUID warden, security;
    private final HashMap<UUID, Long> lastMovement, lastSound;
    private final HashMap<UUID, List<String>> itemCooldown;

    private int battery;
    private BossBar batteryBar;
    private int nextBattery;
    private int closedDoors;

    private Timer timer;
    private SecurityNotMovingCheck securityNotMovingCheck;
    private SecurityExitCheck securityExitCheck;
    private BatteryDrain batteryDrain;

    private final Random random;

    public Game(Main main, Arena arena) {
        this.main = main;
        this.arena = arena;

        this.lastMovement = new HashMap<>();
        this.itemCooldown = new HashMap<>();
        this.lastSound = new HashMap<>();

        this.random = new Random();
    }

    public void start() {
        arena.setState(ArenaState.PLAYING);
        arena.sendMessage(main.getFileUtils().getString("arena.game.start"));

        // SET WARDEN & SECURITY
        List<UUID> players = arena.getPlayers();
        if (warden == null && security == null) {
            boolean randomSelection = random.nextBoolean();

            setWarden(players.get(randomSelection ? 0 : 1));
            setSecurity(players.get(randomSelection ? 1 : 0));
        } else if (security != null && warden == null) {
            setWarden(players.get(players.indexOf(security) == 0 ? 1 : 0));
        } else if (security == null) {
            setSecurity(players.get(players.indexOf(warden) == 0 ? 1 : 0));
        }

        Player wPlayer = Bukkit.getPlayer(warden);
        Player sPlayer = Bukkit.getPlayer(security);

        // WORLD
        for (Location l : arena.getDoors()) {
            Block b = l.getWorld().getBlockAt(l.getBlockX(), l.getBlockY(), l.getBlockZ());

            if (b.getType() == Material.IRON_DOOR) {
                Door door = (Door) b.getBlockData();

                if (!door.isOpen()) {
                    door.setOpen(true);
                    b.setBlockData(door);
                }
            } else if (b.getType() == Material.IRON_TRAPDOOR) {
                TrapDoor trapdoor = (TrapDoor) b.getBlockData();

                if (!trapdoor.isOpen()) {
                    trapdoor.setOpen(true);
                    b.setBlockData(trapdoor);
                }
            }
        }

        if (wPlayer.getWorld().getDifficulty() == Difficulty.PEACEFUL) {
            wPlayer.getWorld().setDifficulty(Difficulty.EASY);
        }
        if (sPlayer.getWorld().getDifficulty() == Difficulty.PEACEFUL) {
            sPlayer.getWorld().setDifficulty(Difficulty.EASY);
        }

        // GAME
        this.battery = 100;
        this.batteryBar = Bukkit.createBossBar(main.getFileUtils().getString("arena.game.battery-boss-bar")
                .replace("{battery}", String.valueOf(battery)), BarColor.BLUE, BarStyle.SEGMENTED_20);
        this.nextBattery = 100;
        this.closedDoors = 0;

        this.timer = new Timer(main, arena);
        this.securityNotMovingCheck = new SecurityNotMovingCheck(main, arena, security);
        this.securityExitCheck = new SecurityExitCheck(main, arena, security);
        this.batteryDrain = new BatteryDrain(main, arena, batteryBar);

        // SKIN
        if (main.getFileUtils().useSkins() && main.getSkinsRestorerAPI() != null) {
            main.getSkinUtils().applySecuritySkin(sPlayer);
            main.getSkinUtils().applyWardenSkin(wPlayer);
        }

        wPlayer.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE,
                0, false, false));

        timer.start();
        securityNotMovingCheck.start();
        securityExitCheck.start();
        batteryDrain.start();

        // SECURITY
        sPlayer.getInventory().clear();
        sPlayer.teleport(arena.getSecuritySpawn());
        sPlayer.setHealth(20);
        sPlayer.setFoodLevel(20);
        sPlayer.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());

        main.getItemUtils().giveSecurityItems(sPlayer);

        sPlayer.sendMessage(main.prefix + main.getFileUtils().getString("arena.game.security-objective"));
        sPlayer.sendTitle(main.getFileUtils().getString("arena.game.security-start-title"),
                "", 20, 100, 20);

        this.batteryBar.addPlayer(sPlayer);
        this.lastSound.put(security, System.currentTimeMillis());
        this.itemCooldown.put(security, new ArrayList<>());
        this.lastMovement.put(security, System.currentTimeMillis());

        // WARDEN
        wPlayer.getInventory().clear();
        wPlayer.teleport(arena.getWardenSpawn());
        wPlayer.setHealth(20);
        wPlayer.setFoodLevel(20);
        wPlayer.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());

        main.getItemUtils().giveWardenItems(wPlayer);
        wPlayer.setCompassTarget(main.getFileUtils().getArenaLocation(arena.getName(), ArenaLocation.SECURITY_SPAWN));

        wPlayer.sendMessage(main.prefix + main.getFileUtils().getString("arena.game.warden-objective"));
        wPlayer.sendTitle(main.getFileUtils().getString("arena.game.warden-start-title"),
                "", 20, 100, 20);

        this.itemCooldown.put(warden, new ArrayList<>());
        this.lastMovement.put(warden, System.currentTimeMillis());

        // Start commands
        if(main.getFileUtils().hasStartCommands()){
            List<String> commands = main.getFileUtils().getStartCommands(wPlayer.getName(), sPlayer.getName());
            for(String command : commands){
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            }
        }
    }

    public void gameWin(String who) {
        String winner = "", loser = "";

        arena.setState(ArenaState.ENDING);
        arena.freezePlayers();

        switch (who) {
            case "warden":
                arena.sendMessage(main.getFileUtils().getString("arena.game.warden-win-chat"));
                arena.sendTitle(main.getFileUtils().getString("arena.game.warden-win-title"),
                        main.getFileUtils().getString("arena.game.warden-win-subtitle"));
                arena.playSound(Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST_FAR);

                winner = Bukkit.getOfflinePlayer(warden).getName();
                loser = Bukkit.getOfflinePlayer(security).getName();

                main.getFileUtils().incrementPlayerStatistic(warden, PlayerStatistic.WINS);
                main.getFileUtils().incrementPlayerStatistic(warden, PlayerStatistic.WARDEN_WINS);
                main.getFileUtils().incrementPlayerStatistic(security, PlayerStatistic.LOSSES);
                break;

            case "security":
                arena.sendMessage(main.getFileUtils().getString("arena.game.security-win-chat"));
                arena.sendTitle(main.getFileUtils().getString("arena.game.security-win-title"),
                        main.getFileUtils().getString("arena.game.security-win-subtitle"));
                arena.playSound(Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST_FAR);

                winner = Bukkit.getOfflinePlayer(security).getName();
                loser = Bukkit.getOfflinePlayer(warden).getName();

                main.getFileUtils().incrementPlayerStatistic(security, PlayerStatistic.WINS);
                main.getFileUtils().incrementPlayerStatistic(security, PlayerStatistic.SECURITY_WINS);
                main.getFileUtils().incrementPlayerStatistic(warden, PlayerStatistic.LOSSES);
                break;
        }

        main.getFileUtils().incrementPlayerStatistic(warden, PlayerStatistic.GAMES_PLAYED);
        main.getFileUtils().incrementPlayerStatistic(security, PlayerStatistic.GAMES_PLAYED);

        List<String> commands = main.getFileUtils().getRewardsCommands(winner, loser);

        new BukkitRunnable() {

            public void run() {
                arena.reset(true);

                // Rewards commands
                if(main.getFileUtils().giveRewards()){
                    for(String command : commands){
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                    }
                }
            }

        }.runTaskLater(main, 5 * 20L);
    }

    /*
     * SETTERS
     */

    public void setWarden(UUID warden) {
        this.warden = warden;
    }

    public void setSecurity(UUID security) {
        this.security = security;
    }

    public void setBattery(int battery) {
        this.battery = battery;
    }

    public void setNextBattery(int nextBattery) {
        this.nextBattery = nextBattery;
    }

    public void setClosedDoors(int closedDoors) {
        this.closedDoors = closedDoors;
    }

    public void setLastSound(UUID who) {
        lastSound.replace(who, System.currentTimeMillis());
    }

    public void setLastMovement(UUID who) {
        lastMovement.replace(who, System.currentTimeMillis());
    }

    public void setItemCooldown(UUID who, String localizedName, int cooldownSeconds) {
        List<String> list = itemCooldown.getOrDefault(who, new ArrayList<>());

        list.add(localizedName);

        itemCooldown.replace(who, list);

        new Cooldown(main, arena, cooldownSeconds, who, localizedName).start();
    }

    public void removeItemCooldown(UUID who, String localizedName) {
        List<String> list = itemCooldown.getOrDefault(who, new ArrayList<>());

        if (list.contains(localizedName)) {
            list.remove(localizedName);

            itemCooldown.replace(who, list);
        } else {
            Bukkit.getLogger().log(Level.SEVERE,
                    main.consolePrefix + "Invalid localized name in removeItemCooldown method!");
        }
    }

    public void removeFromLists(UUID who) {
        itemCooldown.remove(who);

        lastMovement.remove(who);

        lastSound.remove(who);
    }

    /*
     * GETTERS
     */

    public UUID getWarden() {
        return warden;
    }

    public UUID getSecurity() {
        return security;
    }

    public long getLastMovement(UUID who) {
        return lastMovement.get(who);
    }

    public long getLastSound(UUID who) {
        return lastSound.get(who);
    }

    public BossBar getBatteryBar() {
        return batteryBar;
    }

    public int getBattery() {
        return battery;
    }

    public int getNextBattery() {
        return nextBattery;
    }

    public int getClosedDoors() {
        return closedDoors;
    }

    public boolean isOnItemCooldown(UUID who, String localizedName) {
        if (!itemCooldown.containsKey(who))
            return false;

        for (String s : itemCooldown.get(who)) {
            if (s.equals(localizedName)) {
                return true;
            }
        }

        return false;
    }

    public String getRole(UUID who) {
        return who.equals(warden) ? "warden" : "security";
    }

}
