package io.github.wall69.ancientnightmare.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import io.github.wall69.ancientnightmare.Main;
import io.github.wall69.ancientnightmare.arena.Arena;
import io.github.wall69.ancientnightmare.arena.ArenaLocation;
import io.github.wall69.ancientnightmare.game.GameBlock;
import io.github.wall69.ancientnightmare.managers.FileManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

public class FileUtils {

    private final Main main;
    private final FileManager fileManager;
    private FileConfiguration config, arenas, language, player_stats;

    public FileUtils(Main main, FileManager fileManager) {
        this.main = main;
        this.fileManager = fileManager;

        this.config = fileManager.getConfig(FileType.CONFIG);
        this.arenas = fileManager.getConfig(FileType.ARENAS);
        this.language = fileManager.getConfig(FileType.LANGUAGE);
        this.player_stats = fileManager.getConfig(FileType.PLAYER_STATS);
    }

    /*
        GETTERS
     */

    public List<Arena> getArenas() {
        List<Arena> arenasList = new ArrayList<>();

        if (arenas.getConfigurationSection("arena.") == null
                || arenas.getConfigurationSection("arena.").getKeys(false).size() == 0)
            return null;

        for (String arenaName : arenas.getConfigurationSection("arena.").getKeys(false)) {
            if (getArenaLocation(arenaName, ArenaLocation.LOBBY) == null)
                continue;

            arenasList.add(new Arena(main, arenaName, getArenaLocation(arenaName, ArenaLocation.LOBBY)));
        }

        return arenasList;
    }

    public List<Location> getArenaDoors(String arenaName) {
        List<Location> doorsList = new ArrayList<>();

        if (arenas.getConfigurationSection("arena." + arenaName + ".doors") == null
                || arenas.getConfigurationSection("arena." + arenaName + ".doors.").getKeys(false).size() == 0)
            return null;

        for (String doorID : arenas.getConfigurationSection("arena." + arenaName + ".doors.").getKeys(false)) {
            if (getDoor(arenaName, doorID) == null)
                continue;

            doorsList.add(getDoor(arenaName, doorID));
        }

        return doorsList;
    }

    public Location getGlobalLobbyLocation() {
        if (config.getString("lobby") == null)
            return null;

        return new Location(Bukkit.getWorld(config.getString("lobby.world")), config.getDouble("lobby.x"),
                config.getDouble("lobby.y"), config.getDouble("lobby.z"), (float) config.getDouble("lobby.yaw"),
                (float) config.getDouble("lobby.pitch"));
    }

    public Location getArenaLocation(String arenaName, ArenaLocation arenaLocation) {
        if (arenas.getString("arena." + arenaName + "." + arenaLocation.getPath()) == null)
            return null;

        switch (arenaLocation) {
            case LOBBY:
            case WARDEN_SPAWN:
            case SECURITY_SPAWN:
                return new Location(Bukkit.getWorld(
                        arenas.getString("arena." + arenaName + "." + arenaLocation.getPath() + ".world")),
                        arenas.getDouble("arena." + arenaName + "." + arenaLocation.getPath() + ".x"),
                        arenas.getDouble("arena." + arenaName + "." + arenaLocation.getPath() + ".y"),
                        arenas.getDouble("arena." + arenaName + "." + arenaLocation.getPath() + ".z"),
                        (float) arenas.getDouble("arena." + arenaName + "." + arenaLocation.getPath() + ".yaw"),
                        (float) arenas.getDouble("arena." + arenaName + "." + arenaLocation.getPath() + ".pitch"));

            case BATTERY_SUPPLY_BLOCK:
            case GENERATOR_BLOCK:
            case EXIT_REGION_POS_1:
            case EXIT_REGION_POS_2:
            case ARENA_REGION_POS_1:
            case ARENA_REGION_POS_2:
                return new Location(Bukkit.getWorld(
                        arenas.getString("arena." + arenaName + "." + arenaLocation.getPath() + ".world")),
                        arenas.getDouble("arena." + arenaName + "." + arenaLocation.getPath() + ".x"),
                        arenas.getDouble("arena." + arenaName + "." + arenaLocation.getPath() + ".y"),
                        arenas.getDouble("arena." + arenaName + "." + arenaLocation.getPath() + ".z"));
        }

        return null;
    }

    public Location getDoor(String arenaName, String doorID) {
        if (arenas.getString("arena." + arenaName + ".doors." + doorID) == null)
            return null;

        return new Location(Bukkit.getWorld(
                arenas.getString("arena." + arenaName + ".doors." + doorID + ".world")),
                arenas.getDouble("arena." + arenaName + ".doors." + doorID + ".x"),
                arenas.getDouble("arena." + arenaName + ".doors." + doorID + ".y"),
                arenas.getDouble("arena." + arenaName + ".doors." + doorID + ".z"));
    }

    public int getCountdownSeconds() {
        return config.getInt("arena.countdown-seconds");
    }

    public int getGeneratorRechargePerDoor() {
        return config.getInt("arena.generator-recharge-per-door");
    }

    public int getArenaGameTime(String arenaName) {
        return arenas.getInt("arena." + arenaName + ".game-time");
    }

    public int getWardenAbilityCooldown(String ability) {
        return config.getInt("warden.abilities." + ability + ".cooldown");
    }

    public int getSecurityAbilityCooldown(String ability) {
        return config.getInt("security.abilities." + ability + ".cooldown");
    }

    public int getWardenSonicAttackMaxRange() {
        return config.getInt("warden.abilities.sonic-attack.max-range");
    }

    public int getSecurityAppleAmount() {
        return config.getInt("security.abilities.apple.amount");
    }

    public double getWardenVentSpeed() {
        return config.getDouble("warden.vent-speed");
    }

    public boolean checkUpdates() {
        return config.getBoolean("check-updates");
    }

    public boolean disableMobSpawn() {
        return config.getBoolean("disable-mob-spawn");
    }

    public boolean useSkins() {
        return config.getBoolean("use-skins");
    }

    public boolean isWardenAbilityEnabled(String ability) {
        return config.getBoolean("warden.abilities." + ability + ".enabled");
    }

    public boolean isSecurityAbilityEnabled(String ability) {
        return config.getBoolean("security.abilities." + ability + ".enabled");
    }

    public boolean giveRewards() {
        return config.getBoolean("arena.rewards.enabled");
    }

    public boolean hasStartCommands() {
        return config.getBoolean("arena.start-commands.enabled");
    }

    public boolean allowRoleSelect() {
        return config.getBoolean("arena.allow-role-select");
    }

    public boolean useLobbyScoreboard() {
        return config.getBoolean("arena.lobby-scoreboard");
    }

    public boolean isProxy() {
        return config.getBoolean("proxy");
    }

    public String getString(String string) {
        if(language == null || language.getString(string) == null){
            Bukkit.getLogger().log(Level.SEVERE, "Field " + string + " was not found in language.yml!" +
                    " Please try removing it & then restarting the server, if that does not help, contact us.");

            return "§cSomething went wrong, please contact the server administrator.";
        }

        return ChatColor.translateAlternateColorCodes('&', language.getString(string));
    }

    public List<String> getStringList(String string) {
        List<String> list = new ArrayList<>();

        for (String s : language.getStringList(string)) {
            int cooldown = -69;

            String[] wardenAbilities = {"rage", "stealth", "sonic-attack"};

            for (String ability : wardenAbilities) {
                if (string.contains(ability)) {
                    cooldown = getWardenAbilityCooldown(ability);
                }
            }

            String[] securityAbilities = {"fake-sound", "baton"};

            for (String ability : securityAbilities) {
                if (string.contains(ability)) {
                    cooldown = getSecurityAbilityCooldown(ability);
                }
            }

            if (cooldown != -69) {
                list.add(ChatColor.translateAlternateColorCodes('&',
                        s.replace("{cooldown}", String.valueOf(cooldown))));
            } else {
                list.add(ChatColor.translateAlternateColorCodes('&', s));
            }
        }

        return list;
    }

    public List<String> getRewardsCommands(String winnerName, String loserName) {
        List<String> list = new ArrayList<>();

        for (String s : config.getStringList("arena.rewards.commands")) {
            list.add(ChatColor.translateAlternateColorCodes('&', s.replace("%winner%",
                    winnerName).replace("%loser%", loserName)));
        }

        return list;
    }

    public List<String> getStartCommands(String warden, String security) {
        List<String> list = new ArrayList<>();

        for (String s : config.getStringList("arena.start-commands.commands")) {
            list.add(ChatColor.translateAlternateColorCodes('&', s.replace("%warden%",
                    warden).replace("%security%", security)));
        }

        return list;
    }

    public int getPlayerStatistic(UUID uuid, PlayerStatistic statistic){
        if(!player_stats.contains(uuid.toString())){
            createPlayerStatisticProfile(uuid);
        }

        return player_stats.getInt(uuid + "." + statistic.getName());
    }

    public Material getBlockType(GameBlock gameBlock){
        return Material.valueOf(config.getString("arena.blocks." +
                gameBlock.toString().toLowerCase().replace("_", "-")));
    }

    public String getWardenSkinValue(){
        return config.getString("warden.custom-skin.value");
    }

    public String getWardenSkinSignature(){
        return config.getString("warden.custom-skin.signature");
    }

    public String getSecurityMaleSkinValue(){
        return config.getString("security.custom-skin.male.value");
    }

    public String getSecurityMaleSkinSignature(){
        return config.getString("security.custom-skin.male.signature");
    }

    public String getSecurityFemaleSkinValue(){
        return config.getString("security.custom-skin.female.value");
    }

    public String getSecurityFemaleSkinSignature(){
        return config.getString("security.custom-skin.female.signature");
    }

    /*
        SETTERS
     */

    public void setLobbyLocation(Location lobby) {
        config.set("lobby.world", lobby.getWorld().getName());
        config.set("lobby.x", lobby.getBlockX());
        config.set("lobby.y", lobby.getBlockY());
        config.set("lobby.z", lobby.getBlockZ());
        config.set("lobby.yaw", lobby.getYaw());
        config.set("lobby.pitch", lobby.getPitch());
        fileManager.save(FileType.CONFIG);
    }

    public void setArenaLocation(String arenaName, ArenaLocation arenaLocation, Location location) {
        switch (arenaLocation) {
            case LOBBY:
            case WARDEN_SPAWN:
            case SECURITY_SPAWN:
                arenas.set("arena." + arenaName + "." + arenaLocation.getPath() + ".world",
                        location.getWorld().getName());
                arenas.set("arena." + arenaName + "." + arenaLocation.getPath() + ".x", location.getBlockX() + 0.5);
                arenas.set("arena." + arenaName + "." + arenaLocation.getPath() + ".y", location.getBlockY());
                arenas.set("arena." + arenaName + "." + arenaLocation.getPath() + ".z", location.getBlockZ() + 0.5);
                arenas.set("arena." + arenaName + "." + arenaLocation.getPath() + ".yaw", location.getYaw());
                arenas.set("arena." + arenaName + "." + arenaLocation.getPath() + ".pitch", location.getPitch());
                break;
            case BATTERY_SUPPLY_BLOCK:
            case GENERATOR_BLOCK:
            case EXIT_REGION_POS_1:
            case EXIT_REGION_POS_2:
            case ARENA_REGION_POS_1:
            case ARENA_REGION_POS_2:
                arenas.set("arena." + arenaName + "." + arenaLocation.getPath() + ".world",
                        location.getWorld().getName());
                arenas.set("arena." + arenaName + "." + arenaLocation.getPath() + ".x", location.getBlockX());
                arenas.set("arena." + arenaName + "." + arenaLocation.getPath() + ".y", location.getBlockY());
                arenas.set("arena." + arenaName + "." + arenaLocation.getPath() + ".z", location.getBlockZ());
                break;
        }

        fileManager.save(FileType.ARENAS);
    }

    public void setDoors(String arenaName, List<Location> doors) {
        int i = 0;

        for (Location location : doors) {
            arenas.set("arena." + arenaName + ".doors." + i + ".world", location.getWorld().getName());
            arenas.set("arena." + arenaName + ".doors." + i + ".x", location.getBlockX());
            arenas.set("arena." + arenaName + ".doors." + i + ".y", location.getBlockY());
            arenas.set("arena." + arenaName + ".doors." + i + ".z", location.getBlockZ());
            i++;
        }

        fileManager.save(FileType.ARENAS);
    }

    public void setArenaGameTime(String arenaName, int seconds) {
        arenas.set("arena." + arenaName + ".game-time", seconds);
        fileManager.save(FileType.ARENAS);
    }

    public void setUseSkins(boolean useSkins) {
        config.set("use-skins", useSkins);
        fileManager.save(FileType.CONFIG);
    }

    public void setBlockType(String gameBlock, Material type){
        config.set("arena.blocks." + gameBlock.toLowerCase(), type.name());
        fileManager.save(FileType.CONFIG);
    }

    public void setWardenSkin(String value, String signature){
        config.set("warden.custom-skin.value", value);
        config.set("warden.custom-skin.signature", signature);
        fileManager.save(FileType.CONFIG);
    }

    public void setSecurityMaleSkin(String value, String signature){
        config.set("security.custom-skin.male.value", value);
        config.set("security.custom-skin.male.signature", signature);
        fileManager.save(FileType.CONFIG);
    }

    public void setSecurityFemaleSkin(String value, String signature){
        config.set("security.custom-skin.female.value", value);
        config.set("security.custom-skin.female.signature", signature);
        fileManager.save(FileType.CONFIG);
    }

    /*
     * METHODS
     */

    public void deleteArena(String arenaName) {
        arenas.set("arena." + arenaName, null);

        fileManager.save(FileType.ARENAS);
    }

    public void reloadAllFiles() {
        for (FileType type : FileType.values()) {
            if (fileManager.getFile(type) == null) {
                Bukkit.getLogger().log(Level.SEVERE,
                        main.consolePrefix + "File " + type.getName() + " was not found!");
                return;
            }

            fileManager.reload(type);
        }

        this.config = fileManager.getConfig(FileType.CONFIG);
        this.arenas = fileManager.getConfig(FileType.ARENAS);
        this.language = fileManager.getConfig(FileType.LANGUAGE);
        this.player_stats = fileManager.getConfig(FileType.PLAYER_STATS);
    }

    public void createPlayerStatisticProfile(UUID uuid){
        player_stats.set(uuid + ".wins", 0);
        player_stats.set(uuid + ".losses", 0);
        player_stats.set(uuid + ".games_played", 0);
        player_stats.set(uuid + ".warden_wins", 0);
        player_stats.set(uuid + ".security_wins", 0);
        fileManager.save(FileType.PLAYER_STATS);
    }

    public void incrementPlayerStatistic(UUID uuid, PlayerStatistic statistic){
        if(!player_stats.contains(uuid.toString())){
            createPlayerStatisticProfile(uuid);
        }

        player_stats.set(uuid + "." + statistic.getName(), getPlayerStatistic(uuid, statistic) + 1);
        fileManager.save(FileType.PLAYER_STATS);
    }

}
