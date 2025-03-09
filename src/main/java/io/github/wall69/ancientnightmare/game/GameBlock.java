package io.github.wall69.ancientnightmare.game;

import org.bukkit.Material;

public enum GameBlock {

    GENERATOR(Material.COMMAND_BLOCK), BATTERY_SUPPLY(Material.BARREL);

    private Material type;

    GameBlock(Material type){
        this.type = type;
    }

    public Material getType(){
        return type;
    }

    public void setType(Material type){
        this.type = type;
    }

}
