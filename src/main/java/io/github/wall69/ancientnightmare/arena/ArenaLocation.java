package io.github.wall69.ancientnightmare.arena;

import io.github.wall69.ancientnightmare.game.GameBlock;

public enum ArenaLocation {

    LOBBY("lobby", "Place the REDSTONE block where the lobby spawn of this arena should be."),

    ARENA_REGION_POS_1("arena-pos1", "Place the RED GLASS block in the first corner of the arena region, " +
            "then again in the second corner of the arena region."),
    ARENA_REGION_POS_2("arena-pos2", "Now place the RED GLASS block in the second corner of the arena region."),

    EXIT_REGION_POS_1("exit-pos1", "Place the LIME GLASS block in the first corner of the exit region, " +
            "then again in the second corner of the exit region."),
    EXIT_REGION_POS_2("exit-pos2", "Now place the LIME GLASS block in the second corner of the exit region."),

    WARDEN_SPAWN("warden-spawn", "Place the SCULK block where the spawn of WARDEN should be."),

    SECURITY_SPAWN("security-spawn", "Place the IRON block where the spawn of SECURITY should be."),

    BATTERY_SUPPLY_BLOCK("battery", "Right click a " + GameBlock.BATTERY_SUPPLY.getType().name() +
            " block which will work as battery supply for SECURITY with the ECHO SHARD."),

    GENERATOR_BLOCK("generator", "Right click a " + GameBlock.GENERATOR.getType().name() +
            " block which will work as generator with the REDSTONE DUST.");

    private final String path, message;

    ArenaLocation(String path, String message){
        this.path = path;
        this.message = message;
    }

    public String getPath(){
        return path;
    }

    public String getMessage(){
        return message;
    }
}
