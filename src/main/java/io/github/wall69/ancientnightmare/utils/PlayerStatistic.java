package io.github.wall69.ancientnightmare.utils;

public enum PlayerStatistic {

    WINS("wins"), LOSSES("losses"), GAMES_PLAYED("games_played"), WARDEN_WINS("warden_wins"),
    SECURITY_WINS("security_wins");

    private final String name;

    PlayerStatistic(String name){
        this.name = name;
    }

    public String getName(){
        return name;
    }

}
